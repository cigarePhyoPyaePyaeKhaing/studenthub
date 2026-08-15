package com.studenthub.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DeadlineAuthorizationTest {
    @Test void studentCannotCreateEditOrDelete() {
        assertFalse(Authorization.canManageDeadlines("STUDENT"));
        assertFalse(Authorization.canManageDeadline("STUDENT", 1L, 1L));
    }
    @Test void crCanCreateAndManageOnlyOwnDeadline() {
        assertTrue(Authorization.canManageDeadlines("CR"));
        assertTrue(Authorization.canManageDeadline("CR", 2L, 2L));
        assertFalse(Authorization.canManageDeadline("CR", 2L, 3L));
    }
    @Test void adminCanCreateAndManageAnyDeadline() {
        assertTrue(Authorization.canManageDeadlines("ADMIN"));
        assertTrue(Authorization.canManageDeadline("ADMIN", 9L, 2L));
    }
    @Test void unauthenticatedIdentityIsRejected() {
        assertFalse(Authorization.isAuthenticatedUserId(null));
    }
}
