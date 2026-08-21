package com.studenthub.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PostValidationTest {
    @Test void crCompatibleValidPostInputIsAccepted() {
        assertNull(PostValidation.validate("Exam reminder", "The exam begins at 9 AM.", 1L, "ALL"));
    }

    @Test void invalidPostInputIsRejected() {
        assertNotNull(PostValidation.validate("", "Content", 1L, "ALL"));
        assertNotNull(PostValidation.validate("Title", "   ", 1L, "ALL"));
        assertNotNull(PostValidation.validate("x".repeat(201), "Content", 1L, "ALL"));
        assertNotNull(PostValidation.validate("Title", "x".repeat(10_001), 1L, "ALL"));
        assertNotNull(PostValidation.validate("Title", "Content", null, "ALL"));
        assertNotNull(PostValidation.validate("Title", "Content", 1L, "PRIVATE"));
        assertNull(PostValidation.validate("Title", "Content", 1L, "SEMESTER"));
        assertNull(PostValidation.validate("Title", "Content", 1L, "SECTION"));
    }

    @Test void assignmentMandatesValidDeadline() {
        assertNotNull(PostValidation.validate("Database Assignment", "Submit ex 5", 1L, "Assignment", "ALL", null));
        assertNotNull(PostValidation.validate("Database Assignment", "Submit ex 5", 1L, "Assignment", "ALL", "   "));
        assertNotNull(PostValidation.validate("Database Assignment", "Submit ex 5", 1L, "Assignment", "ALL", "invalid-date"));
        assertNull(PostValidation.validate("Database Assignment", "Submit ex 5", 1L, "Assignment", "ALL", "2026-09-01T23:59"));
    }

    @Test void tutorialAndExamMandateValidDeadline() {
        assertNotNull(PostValidation.validate("Tutorial 1", "Complete sheet", 2L, "Tutorial", "SEMESTER", ""));
        assertNull(PostValidation.validate("Tutorial 1", "Complete sheet", 2L, "Tutorial", "SEMESTER", "2026-09-10T14:00"));

        assertNotNull(PostValidation.validate("Midterm Exam", "Room 301", 3L, "Exam", "SECTION", null));
        assertNull(PostValidation.validate("Midterm Exam", "Room 301", 3L, "Exam", "SECTION", "2026-10-05T09:00"));
    }

    @Test void generalNewsAndOtherCategoriesDoNotMandateDeadline() {
        assertNull(PostValidation.validate("Holiday Notice", "Campus closed", 4L, "General News", "ALL", null));
        assertNull(PostValidation.validate("Holiday Notice", "Campus closed", 4L, "General News", "ALL", ""));
        assertNull(PostValidation.validate("Tech Event", "Join webinar", 5L, "Event", "ALL", ""));
        assertNull(PostValidation.validate("Lecture 4 Slides", "Available now", 6L, "Lecture Material", "SEMESTER", null));
    }

    @Test void generalNewsWithValidOptionalDeadlineToBeAccepted() {
        assertNull(PostValidation.validate("Registration Window", "Register before date", 4L, "General News", "ALL", "2026-09-15T18:00"));
        assertNotNull(PostValidation.validate("Registration Window", "Register before date", 4L, "General News", "ALL", "bad-date"));
    }
}
