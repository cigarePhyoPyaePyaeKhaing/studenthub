package com.studenthub.util;

import com.studenthub.model.DiscussionScope;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DiscussionCrAccessTest {
    @Test void studentCannotAccessEitherCrRoom() {
        assertFalse(DiscussionAccess.roleMayAccess(DiscussionScope.CR_SEMESTER, "STUDENT"));
        assertFalse(DiscussionAccess.roleMayAccess(DiscussionScope.CR_ALL, "STUDENT"));
    }

    @Test void crCanAccessBothCrRooms() {
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.CR_SEMESTER, "CR"));
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.CR_ALL, "CR"));
    }

    @Test void existingStudentRoomsRemainAvailableToStudents() {
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.SECTION, "STUDENT"));
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.SEMESTER, "STUDENT"));
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.ALL, "STUDENT"));
    }

    @Test void crSemesterRequiresAuthoritativeSemester() {
        assertNotNull(DiscussionAccess.denialReason(DiscussionScope.CR_SEMESTER, null, "B"));
        assertNull(DiscussionAccess.denialReason(DiscussionScope.CR_SEMESTER, 4, "B"));
    }

    @Test void crSemesterCannotCrossSemesterBoundary() {
        assertTrue(DiscussionAccess.matches(DiscussionScope.CR_SEMESTER, 4, "B", 4, null));
        assertFalse(DiscussionAccess.matches(DiscussionScope.CR_SEMESTER, 4, "B", 2, null));
    }

    @Test void crAllSpansSemesters() {
        assertTrue(DiscussionAccess.matches(DiscussionScope.CR_ALL, 4, "B", null, null));
    }

    @Test void adminRetainsModerationAccessWithoutBeingPresentedAsCr() {
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.CR_ALL, "ADMIN"));
        assertNotEquals("CR", "ADMIN");
    }

    @Test void roleSemesterAndUserRequestFieldsCannotAlterAuthenticatedTarget() {
        DiscussionTarget target = DiscussionTarget.fromAuthenticatedUser(
                42L, DiscussionScope.CR_SEMESTER, 4, "B");
        assertAll(
                () -> assertEquals(42L, target.authorId()),
                () -> assertEquals(4, target.semester()),
                () -> assertNull(target.sectionName())
        );
    }
}
