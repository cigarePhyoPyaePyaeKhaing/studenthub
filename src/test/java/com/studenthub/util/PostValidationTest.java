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
}
