package com.studenthub.service;

import static org.junit.jupiter.api.Assertions.*;

import com.studenthub.dao.DiscussionDAO;
import com.studenthub.model.DiscussionScope;
import com.studenthub.util.DiscussionTarget;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class DiscussionServiceAccessTest {
    @Test void newStudentCanLoadAllButNotAcademicRooms() throws Exception {
        Fixture fixture = fixture("STUDENT", null, null);
        assertEquals(DiscussionScope.ALL, fixture.service.load(7L, null).scope());
        assertTrue(fixture.service.load(7L, "ALL").available());
        DiscussionService.RoomView semester = fixture.service.load(7L, "SEMESTER");
        DiscussionService.RoomView section = fixture.service.load(7L, "SECTION");
        assertFalse(semester.available());
        assertFalse(section.available());
        assertFalse(semester.semesterRoomAvailable());
        assertFalse(section.sectionRoomAvailable());
        assertEquals(2, fixture.recentCalls.get());
    }

    @Test void studentWithSemesterCanLoadSemesterButNotSection() throws Exception {
        Fixture fixture = fixture("STUDENT", 2, null);
        assertTrue(fixture.service.load(7L, "ALL").available());
        DiscussionService.RoomView semester = fixture.service.load(7L, "SEMESTER");
        DiscussionService.RoomView section = fixture.service.load(7L, "SECTION");
        assertTrue(semester.available());
        assertTrue(semester.semesterRoomAvailable());
        assertFalse(section.available());
        assertFalse(section.sectionRoomAvailable());
    }

    @Test void studentWithCompleteAcademicProfileCanLoadMatchingRooms() throws Exception {
        Fixture fixture = fixture("STUDENT", 2, "A");
        assertTrue(fixture.service.load(7L, "ALL").available());
        assertTrue(fixture.service.load(7L, "SEMESTER").available());
        assertTrue(fixture.service.load(7L, "SECTION").available());
        assertThrows(SecurityException.class, () -> fixture.service.load(7L, "CR_SEMESTER"));
        assertThrows(SecurityException.class, () -> fixture.service.load(7L, "CR_ALL"));
        assertThrows(SecurityException.class, () -> fixture.service.load(7L, "CR_ADMIN"));
    }

    @Test void crAllDoesNotRequireSemesterButCrSemesterDoes() throws Exception {
        Fixture withoutSemester = fixture("CR", null, null);
        assertTrue(withoutSemester.service.load(7L, "CR_ALL").available());
        assertFalse(withoutSemester.service.load(7L, "CR_SEMESTER").available());
        assertTrue(withoutSemester.service.load(7L, "CR_ADMIN").available());
        Fixture withSemester = fixture("CR", 3, null);
        assertTrue(withSemester.service.load(7L, "CR_ALL").available());
        assertTrue(withSemester.service.load(7L, "CR_SEMESTER").available());
        assertTrue(withSemester.service.load(7L, "CR_ADMIN").available());
    }

    @Test void adminLoadsOnlyAllAndCrAdminAndDefaultsToAll() throws Exception {
        Fixture admin = fixture("ADMIN", null, null);
        assertEquals(DiscussionScope.ALL, admin.service.load(7L, null).scope());
        assertEquals(DiscussionScope.ALL, admin.service.load(7L, "").scope());
        assertTrue(admin.service.load(7L, "ALL").available());
        assertTrue(admin.service.load(7L, "CR_ADMIN").available());
        assertThrows(SecurityException.class, () -> admin.service.load(7L, "SECTION"));
        assertThrows(SecurityException.class, () -> admin.service.load(7L, "SEMESTER"));
        assertThrows(SecurityException.class, () -> admin.service.load(7L, "CR_SEMESTER"));
        assertThrows(SecurityException.class, () -> admin.service.load(7L, "CR_ALL"));
    }

    private Fixture fixture(String role, Integer semester, String section) {
        AtomicInteger recentCalls = new AtomicInteger();
        DiscussionDAO dao = new DiscussionDAO() {
            @Override public AcademicProfile findAcademicProfile(long userId) {
                return new AcademicProfile(role, 5L, semester, section);
            }
            @Override public List<com.studenthub.model.DiscussionMessage> findRecent(DiscussionTarget target, int limit) {
                recentCalls.incrementAndGet();
                return List.of();
            }
        };
        return new Fixture(new DiscussionService(dao), recentCalls);
    }

    private record Fixture(DiscussionService service, AtomicInteger recentCalls) {}
}
