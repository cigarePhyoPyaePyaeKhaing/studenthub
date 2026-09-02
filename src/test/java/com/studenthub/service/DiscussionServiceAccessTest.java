package com.studenthub.service;

import static org.junit.jupiter.api.Assertions.*;

import com.studenthub.dao.DiscussionDAO;
import com.studenthub.model.DiscussionScope;
import com.studenthub.util.DiscussionTarget;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class DiscussionServiceAccessTest {
    @Test void newStudentCanLoadAllButNotAcademicOrLegacyAdminRooms() throws Exception {
        Fixture fixture = fixture("STUDENT", null, null);
        assertEquals(DiscussionScope.ALL, fixture.service.load(7L, null).scope());
        assertTrue(fixture.service.load(7L, "ALL").available());
        assertThrows(SecurityException.class, () -> fixture.service.load(7L, "ALL_STUDENTS_ADMIN"));
        DiscussionService.RoomView semester = fixture.service.load(7L, "SEMESTER");
        DiscussionService.RoomView section = fixture.service.load(7L, "SECTION");
        assertFalse(semester.available());
        assertFalse(section.available());
        assertFalse(semester.semesterRoomAvailable());
        assertFalse(section.sectionRoomAvailable());
        assertThrows(SecurityException.class, () -> fixture.service.load(7L, "CR_ADMIN"));
        assertThrows(SecurityException.class, () -> fixture.service.load(7L, "CR_SEMESTER"));
        assertThrows(SecurityException.class, () -> fixture.service.load(7L, "CR_ALL"));
        assertEquals(2, fixture.recentCalls.get());
    }

    @Test void studentWithSemesterCanLoadSemesterButNotSection() throws Exception {
        Fixture fixture = fixture("STUDENT", 2, null);
        assertTrue(fixture.service.load(7L, "ALL").available());
        assertThrows(SecurityException.class, () -> fixture.service.load(7L, "ALL_STUDENTS_ADMIN"));
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
        assertThrows(SecurityException.class, () -> fixture.service.load(7L, "ALL_STUDENTS_ADMIN"));
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
        assertThrows(SecurityException.class, () -> withoutSemester.service.load(7L, "CR_ADMIN"));
        assertTrue(withoutSemester.service.load(7L, "ALL").available());
        assertThrows(SecurityException.class, () -> withoutSemester.service.load(7L, "ALL_STUDENTS_ADMIN"));

        Fixture withSemester = fixture("CR", 3, null);
        assertTrue(withSemester.service.load(7L, "CR_ALL").available());
        assertTrue(withSemester.service.load(7L, "CR_SEMESTER").available());
        assertThrows(SecurityException.class, () -> withSemester.service.load(7L, "CR_ADMIN"));
        assertTrue(withSemester.service.load(7L, "ALL").available());
        assertThrows(SecurityException.class, () -> withSemester.service.load(7L, "ALL_STUDENTS_ADMIN"));
    }

    @Test void adminModeratesRealScopesWithoutAnAcademicProfile() throws Exception {
        Fixture admin = fixture("ADMIN", null, null);
        assertEquals(DiscussionScope.ALL, admin.service.load(7L, null).scope());
        assertEquals(DiscussionScope.ALL, admin.service.load(7L, "").scope());
        assertTrue(admin.service.load(7L, "ALL").available());
        assertThrows(SecurityException.class, () -> admin.service.load(7L, "ALL_STUDENTS_ADMIN"));
        assertThrows(SecurityException.class, () -> admin.service.load(7L, "CR_ADMIN"));
        assertFalse(admin.service.load(7L, "SECTION").available());
        assertFalse(admin.service.load(7L, "SEMESTER").available());
        assertFalse(admin.service.load(7L, "CR_SEMESTER").available());
        assertTrue(admin.service.load(7L, "CR_ALL").available());
        assertTrue(admin.service.load(7L, null, null, "all_students").available());
        assertTrue(admin.service.load(7L, null, null, "all_cr").available());
        assertEquals(4, admin.service.load(7L, null, null, "semester:4").semester());
        DiscussionService.RoomView section = admin.service.load(7L, null, null, "section:4:B");
        assertEquals(DiscussionScope.SECTION, section.scope());
        assertEquals("B", section.sectionName());
        assertThrows(IllegalArgumentException.class,
                () -> admin.service.load(7L, null, null, "semester:99"));
        List<DiscussionService.ModerationScopeOption> options = admin.service.moderationRooms(7L);
        assertEquals(10, options.stream().filter(option -> "SEMESTERS".equals(option.group())).count());
        assertEquals(1, options.stream().filter(option -> "SECTIONS".equals(option.group())).count());
        assertEquals(1, options.stream().filter(option -> "all_students".equals(option.key())).count());
        assertEquals(1, options.stream().filter(option -> "all_cr".equals(option.key())).count());
    }

    private Fixture fixture(String role, Integer semester, String section) {
        AtomicInteger recentCalls = new AtomicInteger();
        DiscussionDAO dao = new DiscussionDAO() {
            @Override public AcademicProfile findAcademicProfile(long userId) {
                return new AcademicProfile(role, "ADMIN".equals(role) ? null : 5L, semester, section);
            }
            @Override public List<com.studenthub.model.DiscussionMessage> findRecent(DiscussionTarget target, int limit) {
                recentCalls.incrementAndGet();
                return List.of();
            }
            @Override public List<com.studenthub.model.DiscussionMessage> findRecentForModeration(DiscussionTarget target, int limit) {
                recentCalls.incrementAndGet();
                return List.of();
            }
            @Override public List<RoomOption> findModerationRooms() {
                return List.of(
                        new RoomOption(0L, DiscussionScope.SEMESTER, 5L, 4, null, "Semester 4"),
                        new RoomOption(0L, DiscussionScope.SECTION, 5L, 4, "B", "Semester 4 / Section B"));
            }
            @Override public Long findModerationUniversityId() { return 5L; }
        };
        return new Fixture(new DiscussionService(dao), recentCalls);
    }

    private record Fixture(DiscussionService service, AtomicInteger recentCalls) {}
}
