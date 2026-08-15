package com.studenthub.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthValidationTest {
    @Test void acceptsAndNormalizesUitStudentIds() {
        assertTrue(AuthValidation.isValidStudentId("TNT-0001"));
        assertTrue(AuthValidation.isValidStudentId("TNT-1234"));
        assertEquals("TNT-1234", AuthValidation.normalizeStudentId(" tnt-1234 "));
        assertTrue(AuthValidation.isValidStudentId("tnt-1234"));
    }

    @Test void rejectsInvalidStudentIds() {
        assertFalse(AuthValidation.isValidStudentId("TNT-123"));
        assertFalse(AuthValidation.isValidStudentId("TNT-12345"));
        assertFalse(AuthValidation.isValidStudentId("ABC-1234"));
        assertFalse(AuthValidation.isValidStudentId("1234"));
        assertFalse(AuthValidation.isValidStudentId(""));
    }

    @Test void enforcesPasswordPolicy() {
        assertTrue(AuthValidation.isValidPassword("Secure123"));
        assertTrue(AuthValidation.isValidPassword("Secure123!"));
        assertFalse(AuthValidation.isValidPassword("lowercase1"));
        assertFalse(AuthValidation.isValidPassword("UPPERCASE1"));
        assertFalse(AuthValidation.isValidPassword("NoNumberHere"));
        assertFalse(AuthValidation.isValidPassword("Short1A"));
    }
}
