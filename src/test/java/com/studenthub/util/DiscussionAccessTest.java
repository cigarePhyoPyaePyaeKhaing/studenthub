package com.studenthub.util;

import com.studenthub.model.DiscussionScope;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DiscussionAccessTest {
    @Test void allRoomIsAvailableWithoutAcademicProfile() {
        assertNull(DiscussionAccess.denialReason(DiscussionScope.ALL, null, null));
    }

    @Test void sectionRequiresSemester() {
        assertNotNull(DiscussionAccess.denialReason(DiscussionScope.SECTION, null, "B"));
    }

    @Test void sectionRequiresSection() {
        assertNotNull(DiscussionAccess.denialReason(DiscussionScope.SECTION, 4, null));
    }

    @Test void semesterRoomRequiresSemester() {
        assertNotNull(DiscussionAccess.denialReason(DiscussionScope.SEMESTER, null, null));
    }

    @Test void sectionDoesNotCrossSectionBoundary() {
        assertFalse(DiscussionAccess.matches(DiscussionScope.SECTION, 4, "B", 4, "A"));
    }

    @Test void sectionDoesNotCrossSemesterBoundary() {
        assertFalse(DiscussionAccess.matches(DiscussionScope.SECTION, 4, "B", 3, "B"));
    }

    @Test void semesterDoesNotCrossSemesterBoundary() {
        assertFalse(DiscussionAccess.matches(DiscussionScope.SEMESTER, 4, "B", 3, null));
    }

    @Test void matchingAcademicRoomsAreAllowed() {
        assertTrue(DiscussionAccess.matches(DiscussionScope.SECTION, 4, "B", 4, "B"));
        assertTrue(DiscussionAccess.matches(DiscussionScope.SEMESTER, 4, "B", 4, null));
    }
}
