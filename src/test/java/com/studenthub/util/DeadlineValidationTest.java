package com.studenthub.util;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class DeadlineValidationTest {
    private String future() { return LocalDateTime.now().plusDays(2).withSecond(0).withNano(0).toString(); }

    @Test void validDeadlineIsAccepted() {
        assertNull(DeadlineValidation.validate("Database assignment", "Database Systems", future(), "SEMESTER"));
    }

    @Test void invalidFieldsAreRejected() {
        assertNotNull(DeadlineValidation.validate("", "Subject", future(), "SEMESTER"));
        assertNotNull(DeadlineValidation.validate("Title", "", future(), "SEMESTER"));
        assertNotNull(DeadlineValidation.validate("Title", "Subject", "not-a-date", "SEMESTER"));
        assertNotNull(DeadlineValidation.validate("Title", "Subject", LocalDateTime.now().minusDays(1).toString(), "SEMESTER"));
        assertNotNull(DeadlineValidation.validate("Title", "Subject", future(), "OTHER"));
    }

    @Test void academicScopeComesOnlyFromAuthenticatedProfile() {
        DeadlineScope.Resolved semester = DeadlineScope.resolve("SEMESTER", 4, "A");
        assertEquals(4, semester.semester()); assertNull(semester.sectionName());
        DeadlineScope.Resolved section = DeadlineScope.resolve("SECTION", 4, "A");
        assertEquals(4, section.semester()); assertEquals("A", section.sectionName());
        assertNull(DeadlineScope.resolve("SECTION", null, "Spoofed").semester());
    }
}
