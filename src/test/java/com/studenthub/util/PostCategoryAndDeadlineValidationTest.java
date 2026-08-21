package com.studenthub.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PostCategoryAndDeadlineValidationTest {

    @Test
    void testAssignmentRequiresDeadline() {
        String errorNoDeadline = PostValidation.validate("HW1", "Do homework 1", 2L, "Assignment", "ALL", null);
        assertNotNull(errorNoDeadline);
        assertTrue(errorNoDeadline.contains("A deadline date and time is required for Assignment announcements."));

        String errorWithDeadline = PostValidation.validate("HW1", "Do homework 1", 2L, "Assignment", "ALL", LocalDateTime.now().plusDays(3));
        assertNull(errorWithDeadline);
    }

    @Test
    void testTutorialRequiresDeadline() {
        String errorNoDeadline = PostValidation.validate("Tutorial 1", "Prepare tutorial", 3L, "Tutorial", "SEMESTER", null);
        assertNotNull(errorNoDeadline);
        assertTrue(errorNoDeadline.contains("A deadline date and time is required for Tutorial announcements."));

        String errorWithDeadline = PostValidation.validate("Tutorial 1", "Prepare tutorial", 3L, "Tutorial", "SEMESTER", LocalDateTime.now().plusDays(2));
        assertNull(errorWithDeadline);
    }

    @Test
    void testExamRequiresDeadline() {
        String errorNoDeadline = PostValidation.validate("Midterm Exam", "Midterm exam details", 4L, "Exam", "SECTION", null);
        assertNotNull(errorNoDeadline);
        assertTrue(errorNoDeadline.contains("A deadline date and time is required for Exam announcements."));

        String errorWithDeadline = PostValidation.validate("Midterm Exam", "Midterm exam details", 4L, "Exam", "SECTION", LocalDateTime.now().plusDays(7));
        assertNull(errorWithDeadline);
    }

    @Test
    void testGeneralAnnouncementDeadlineIsOptional() {
        String withoutDeadline = PostValidation.validate("Welcome", "Welcome to semester", 1L, "General", "ALL", null);
        assertNull(withoutDeadline);

        String withDeadline = PostValidation.validate("Welcome", "Welcome to semester", 1L, "General", "ALL", LocalDateTime.now().plusDays(10));
        assertNull(withDeadline);
    }

    @Test
    void testIsDeadlineRequiredHelper() {
        assertTrue(PostValidation.isDeadlineRequired("Assignment"));
        assertTrue(PostValidation.isDeadlineRequired("assignment"));
        assertTrue(PostValidation.isDeadlineRequired("Tutorial"));
        assertTrue(PostValidation.isDeadlineRequired("Exam"));
        assertFalse(PostValidation.isDeadlineRequired("General"));
        assertFalse(PostValidation.isDeadlineRequired(null));
    }
}
