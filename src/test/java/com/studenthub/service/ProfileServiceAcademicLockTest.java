package com.studenthub.service;

import com.studenthub.dao.UserDAO;
import com.studenthub.model.ProfileUpdate;
import com.studenthub.model.Role;
import com.studenthub.model.UserProfile;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ProfileServiceAcademicLockTest {

    @Test
    void adminUpdateNeverWritesStudentAcademicFields() throws Exception {
        UserProfile admin = new UserProfile(30L, "ADM-0030", "Admin User", "admin@uit.edu",
                Role.ADMIN, true, null, null);
        AtomicReference<String> updatedFullName = new AtomicReference<>();
        AtomicReference<ProfileUpdate> forbiddenAcademicUpdate = new AtomicReference<>();
        UserDAO mockDao = new UserDAO() {
            @Override public Optional<UserProfile> findProfileById(long userId) { return Optional.of(admin); }
            @Override public int updateFullName(long userId, String fullName) { updatedFullName.set(fullName); return 1; }
            @Override public int updateProfile(long userId, ProfileUpdate update) { forbiddenAcademicUpdate.set(update); return 1; }
        };

        ProfileService.UpdateResult result = new ProfileService(mockDao)
                .updateOwnProfile(30L, "Renamed Admin", "8", "Z");

        assertTrue(result.successful());
        assertEquals("Renamed Admin", updatedFullName.get());
        assertNull(forbiddenAcademicUpdate.get());
    }

    @Test
    void unlockedUserCanSetSemesterAndSectionOnce() throws Exception {
        UserProfile unlockedProfile = new UserProfile(
                10L, "TNT-0010", "New Student", "new@uit.edu",
                Role.STUDENT, true, null, null,
                null, null, null, null, null,
                null, null, null, false, false);

        AtomicReference<ProfileUpdate> savedUpdate = new AtomicReference<>();
        UserProfile lockedProfileAfterSave = new UserProfile(
                10L, "TNT-0010", "New Student Updated", "new@uit.edu",
                Role.STUDENT, true, 3, "A",
                null, null, null, null, null,
                null, null, null, false, true);

        UserDAO mockDao = new UserDAO() {
            private boolean updated = false;

            @Override
            public Optional<UserProfile> findProfileById(long userId) {
                return Optional.of(updated ? lockedProfileAfterSave : unlockedProfile);
            }

            @Override
            public int updateProfile(long authenticatedUserId, ProfileUpdate update) {
                savedUpdate.set(update);
                updated = true;
                return 1;
            }
        };

        ProfileService service = new ProfileService(mockDao);

        ProfileService.UpdateResult result = service.updateOwnProfile(10L, "New Student Updated", "3", "A");

        assertTrue(result.successful());
        assertNotNull(savedUpdate.get());
        assertEquals("New Student Updated", savedUpdate.get().fullName());
        assertEquals(3, savedUpdate.get().semester());
        assertEquals("A", savedUpdate.get().sectionName());
        assertTrue(result.profile().isAcademicInfoLocked());
    }

    @Test
    void lockedUserDirectEditIsBlockedAndOnlyFullNameIsUpdated() throws Exception {
        UserProfile lockedProfile = new UserProfile(
                20L, "TNT-0020", "Locked Student", "locked@uit.edu",
                Role.STUDENT, true, 4, "B",
                null, null, null, null, null,
                null, null, null, false, true);

        AtomicReference<String> updatedFullName = new AtomicReference<>();
        AtomicReference<ProfileUpdate> forbiddenFullUpdate = new AtomicReference<>();

        UserProfile updatedProfile = new UserProfile(
                20L, "TNT-0020", "Renamed Locked Student", "locked@uit.edu",
                Role.STUDENT, true, 4, "B",
                null, null, null, null, null,
                null, null, null, false, true);

        UserDAO mockDao = new UserDAO() {
            @Override
            public Optional<UserProfile> findProfileById(long userId) {
                return Optional.of(updatedProfile);
            }

            @Override
            public int updateFullName(long userId, String fullName) {
                updatedFullName.set(fullName);
                return 1;
            }

            @Override
            public int updateProfile(long authenticatedUserId, ProfileUpdate update) {
                forbiddenFullUpdate.set(update);
                return 1;
            }
        };

        ProfileService service = new ProfileService(mockDao);

        // Crafted request trying to change semester to 8 and section to Z
        ProfileService.UpdateResult result = service.updateOwnProfile(20L, "Renamed Locked Student", "8", "Z");

        assertTrue(result.successful());
        assertEquals("Profile name updated. Academic information remains locked.", result.message());
        assertEquals("Renamed Locked Student", updatedFullName.get());
        assertNull(forbiddenFullUpdate.get()); // updateProfile must NOT be called for locked users
        assertEquals(4, result.profile().getSemester()); // Semester must remain 4
        assertEquals("B", result.profile().getSectionName()); // Section must remain B
    }
}
