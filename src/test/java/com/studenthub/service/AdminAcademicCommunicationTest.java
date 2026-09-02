package com.studenthub.service;

import static org.junit.jupiter.api.Assertions.*;
import com.studenthub.dao.DiscussionDAO;
import com.studenthub.model.DiscussionScope;
import com.studenthub.util.DiscussionTarget;
import java.util.List;
import org.junit.jupiter.api.Test;

class AdminAcademicCommunicationTest {
    @Test void adminLoadsPersistedAcademicRoomWithoutOwnAcademicProfile() throws Exception {
        DiscussionService.RoomView view = new DiscussionService(fixtureDao("ADMIN")).load(99L, "SECTION", 42L);
        assertTrue(view.available());
        assertEquals(DiscussionScope.SECTION, view.scope());
        assertEquals(4, view.semester());
        assertEquals("B", view.sectionName());
    }
    @Test void nonAdminCannotUseAdminRoomIdToCrossAcademicBoundary() throws Exception {
        assertFalse(new DiscussionService(fixtureDao("STUDENT")).load(7L, "SECTION", 42L).available());
        assertFalse(new DiscussionService(fixtureDao("CR")).load(8L, "SECTION", 42L).available());
    }
    @Test void adminModerationDeleteSucceedsButStudentAndCrAreDenied() throws Exception {
        DiscussionService admin = serviceWithoutAttachmentStorage(fixtureDao("ADMIN"));
        assertTrue(admin.delete(99L, "ADMIN", 12L).successful());
        assertEquals("FORBIDDEN", new DiscussionService(fixtureDao("STUDENT")).delete(7L, "STUDENT", 12L).message());
        assertEquals("FORBIDDEN", new DiscussionService(fixtureDao("CR")).delete(8L, "CR", 12L).message());
    }
    @Test void legacyArtificialAdminRoomsAreNoLongerRoleDestinations() {
        for (String role : List.of("STUDENT", "CR", "ADMIN")) {
            assertFalse(com.studenthub.util.DiscussionAccess.roleMayAccess(DiscussionScope.ALL_STUDENTS_ADMIN, role));
            assertFalse(com.studenthub.util.DiscussionAccess.roleMayAccess(DiscussionScope.CR_ADMIN, role));
        }
    }
    private DiscussionDAO fixtureDao(String role) {
        return new DiscussionDAO() {
            @Override public AcademicProfile findAcademicProfile(long userId) { return new AcademicProfile(role, 5L, null, null); }
            @Override public RoomOption findRoom(long roomId) { return roomId == 42L ? new RoomOption(42L, DiscussionScope.SECTION, 5L, 4, "B", "Semester 4 / Section B") : null; }
            @Override public List<com.studenthub.model.DiscussionMessage> findRecent(DiscussionTarget target, int limit) { return List.of(); }
            @Override public List<com.studenthub.model.DiscussionMessage> findRecentForModeration(DiscussionTarget target, int limit) { return List.of(); }
            @Override public MessageRecord findMessage(long messageId) { return new MessageRecord(messageId, 55L, DiscussionScope.ALL, null, null); }
            @Override public int delete(long messageId) { return 1; }
        };
    }
    private DiscussionService serviceWithoutAttachmentStorage(DiscussionDAO dao) {
        return new DiscussionService(dao) {
            @Override String findAttachmentStorageKey(long messageId) { return null; }
        };
    }
}
