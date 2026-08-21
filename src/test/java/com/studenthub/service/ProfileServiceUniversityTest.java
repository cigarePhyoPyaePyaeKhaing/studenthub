package com.studenthub.service;

import com.studenthub.dao.UniversityDAO;
import com.studenthub.dao.UserDAO;
import com.studenthub.model.Role;
import com.studenthub.model.University;
import com.studenthub.model.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ProfileServiceUniversityTest {

    private University uit;
    private University unapprovedUniv;

    @BeforeEach
    void setUp() {
        uit = new University(1L, "University of Information Technology", "UIT", "APPROVED");
        unapprovedUniv = new University(2L, "Pending Tech Institute", "PTI", "PENDING");
    }

    @Test
    void listAvailableUniversitiesReturnsApprovedUniversities() throws Exception {
        UniversityDAO mockUnivDAO = new UniversityDAO() {
            @Override
            public List<University> listApprovedUniversities() {
                return List.of(uit);
            }
        };
        ProfileService service = new ProfileService(new UserDAO(), mockUnivDAO);
        List<University> list = service.listAvailableUniversities();
        assertEquals(1, list.size());
        assertEquals("University of Information Technology (UIT)", list.get(0).getDisplayName());
    }

    @Test
    void firstTimeUniversitySelectionSucceedsAndSetsUniversity() throws Exception {
        AtomicLong assignedUnivId = new AtomicLong(0);
        AtomicReference<UserProfile> state = new AtomicReference<>(new UserProfile(
                10L, "TNT-1010", "Aung Aung", "aung@uit.edu", Role.STUDENT, true,
                4, "C", null, null, null, null, null,
                null, null, null, false, true
        ));

        UserDAO mockUserDAO = new UserDAO() {
            @Override
            public Optional<UserProfile> findProfileById(long userId) {
                return Optional.ofNullable(state.get());
            }

            @Override
            public int updateFullName(long userId, String fullName) {
                state.set(new UserProfile(
                        10L, "TNT-1010", fullName, "aung@uit.edu", Role.STUDENT, true,
                        4, "C", null, null, null, null, null,
                        assignedUnivId.get() > 0 ? assignedUnivId.get() : null,
                        assignedUnivId.get() > 0 ? "University of Information Technology" : null,
                        assignedUnivId.get() > 0 ? "UIT" : null,
                        assignedUnivId.get() > 0, true
                ));
                return 1;
            }

            @Override
            public int updateUniversityIfUnset(long userId, long universityId) {
                if (assignedUnivId.get() == 0) {
                    assignedUnivId.set(universityId);
                    return 1;
                }
                return 0;
            }
        };

        UniversityDAO mockUnivDAO = new UniversityDAO() {
            @Override
            public Optional<University> findApprovedById(long universityId) {
                if (universityId == 1L) return Optional.of(uit);
                return Optional.empty();
            }
        };

        ProfileService service = new ProfileService(mockUserDAO, mockUnivDAO);

        ProfileService.UpdateResult result = service.updateOwnProfile(10L, "Aung Aung Updated", "4", "C", "1");
        assertTrue(result.successful());
        assertEquals(1L, assignedUnivId.get());
        assertNotNull(result.profile());
        assertEquals(1L, result.profile().getUniversityId());
        assertTrue(result.profile().isUniversityLocked());
    }

    @Test
    void cannotChangeAlreadyLockedUniversityViaCraftedRequest() throws Exception {
        AtomicLong assignedUnivId = new AtomicLong(1L);
        UserProfile initialLockedProfile = new UserProfile(
                10L, "TNT-1010", "Aung Aung", "aung@uit.edu", Role.STUDENT, true,
                4, "C", null, null, null, null, null,
                1L, "University of Information Technology", "UIT", true, true
        );

        UserDAO mockUserDAO = new UserDAO() {
            @Override
            public Optional<UserProfile> findProfileById(long userId) {
                return Optional.of(initialLockedProfile);
            }

            @Override
            public int updateFullName(long userId, String fullName) {
                return 1;
            }

            @Override
            public int updateUniversityIfUnset(long userId, long universityId) {
                fail("updateUniversityIfUnset should not be called when university is already locked");
                return 0;
            }
        };

        UniversityDAO mockUnivDAO = new UniversityDAO() {
            @Override
            public Optional<University> findApprovedById(long universityId) {
                if (universityId == 99L) return Optional.of(new University(99L, "Other University", "OU", "APPROVED"));
                return Optional.empty();
            }
        };

        ProfileService service = new ProfileService(mockUserDAO, mockUnivDAO);

        ProfileService.UpdateResult result = service.updateOwnProfile(10L, "Aung Aung", "4", "C", "99");
        assertTrue(result.successful());
        assertEquals(1L, assignedUnivId.get());
    }

    @Test
    void invalidOrUnapprovedUniversityIsSafelyRejected() throws Exception {
        UserProfile initialProfile = new UserProfile(
                10L, "TNT-1010", "Aung Aung", "aung@uit.edu", Role.STUDENT, true,
                null, null, null, null, null, null, null,
                null, null, null, false, false
        );

        UserDAO mockUserDAO = new UserDAO() {
            @Override
            public Optional<UserProfile> findProfileById(long userId) {
                return Optional.of(initialProfile);
            }
        };

        UniversityDAO mockUnivDAO = new UniversityDAO() {
            @Override
            public Optional<University> findApprovedById(long universityId) {
                return Optional.empty();
            }
        };

        ProfileService service = new ProfileService(mockUserDAO, mockUnivDAO);

        ProfileService.UpdateResult resultInvalidNumber = service.updateOwnProfile(10L, "Aung Aung", "4", "C", "abc");
        assertFalse(resultInvalidNumber.successful());
        assertEquals("Invalid university identifier.", resultInvalidNumber.message());

        ProfileService.UpdateResult resultNonExistent = service.updateOwnProfile(10L, "Aung Aung", "4", "C", "999");
        assertFalse(resultNonExistent.successful());
        assertEquals("Selected university is not valid or not approved.", resultNonExistent.message());
    }
}
