package com.studenthub.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {
    @Test void hashesAndVerifiesWithoutKeepingPlaintext() {
        String password = "StrongPassword123!";
        String hash = PasswordUtil.hash(password);
        assertNotEquals(password, hash);
        assertTrue(PasswordUtil.matches(password, hash));
        assertFalse(PasswordUtil.matches("WrongPassword123!", hash));
    }
}
