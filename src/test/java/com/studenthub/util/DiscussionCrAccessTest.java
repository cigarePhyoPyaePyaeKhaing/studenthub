package com.studenthub.util;

import static org.junit.jupiter.api.Assertions.*;
import com.studenthub.model.DiscussionScope;
import org.junit.jupiter.api.Test;

class DiscussionCrAccessTest {
    @Test void crCanAccessStudentAndCrScopes() {
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.ALL, "CR"));
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.SEMESTER, "CR"));
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.SECTION, "CR"));
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.CR_SEMESTER, "CR"));
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.CR_ALL, "CR"));
        assertFalse(DiscussionAccess.roleMayAccess(DiscussionScope.CR_ADMIN, "CR"));
    }
    @Test void crSemesterStillRequiresConfiguredSemester() {
        assertNotNull(DiscussionAccess.denialReason(DiscussionScope.CR_SEMESTER, 5L, null, null));
        assertNull(DiscussionAccess.denialReason(DiscussionScope.CR_SEMESTER, 5L, 4, null));
    }
    @Test void crAcademicMatchingDoesNotCrossSemester() {
        assertTrue(DiscussionAccess.matches(DiscussionScope.CR_SEMESTER, 4, "B", 4, null));
        assertFalse(DiscussionAccess.matches(DiscussionScope.CR_SEMESTER, 4, "B", 3, null));
    }
}
