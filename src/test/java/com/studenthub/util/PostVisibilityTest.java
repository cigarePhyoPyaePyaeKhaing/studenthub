package com.studenthub.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PostVisibilityTest {
    @Test void allPostVisibleToAuthenticatedAcademicProfiles() {
        assertTrue(PostVisibility.canView("ALL", null, null, null, null));
    }
    @Test void sameSemesterPostVisible() {
        assertTrue(PostVisibility.canView("SEMESTER", 4, "A", 4, "B"));
    }
    @Test void differentSemesterPostInvisible() {
        assertFalse(PostVisibility.canView("SEMESTER", 3, "A", 4, "A"));
    }
    @Test void missingSemesterCannotSeeSemesterPost() {
        assertFalse(PostVisibility.canView("SEMESTER", 4, "A", null, "A"));
    }
    @Test void sameSemesterAndSectionPostVisible() {
        assertTrue(PostVisibility.canView("SECTION", 4, "B", 4, "B"));
    }
    @Test void differentSectionPostInvisible() {
        assertFalse(PostVisibility.canView("SECTION", 4, "A", 4, "B"));
    }
    @Test void sameSectionDifferentSemesterPostInvisible() {
        assertFalse(PostVisibility.canView("SECTION", 3, "B", 4, "B"));
    }
}
