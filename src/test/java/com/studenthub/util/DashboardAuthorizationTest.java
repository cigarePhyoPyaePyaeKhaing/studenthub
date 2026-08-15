package com.studenthub.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DashboardAuthorizationTest {
    @Test void unauthenticatedUserCannotAccessProtectedFunctionality() {
        assertFalse(Authorization.isAuthenticatedUserId(null));
        assertFalse(Authorization.isAuthenticatedUserId("1"));
        assertFalse(Authorization.isAuthenticatedUserId(0L));
    }

    @Test void studentCannotManagePosts() {
        assertFalse(Authorization.canManagePosts("STUDENT"));
        assertFalse(Authorization.canManagePosts(null));
    }

    @Test void crAndAdminCanManagePosts() {
        assertTrue(Authorization.canManagePosts("CR"));
        assertTrue(Authorization.canManagePosts("ADMIN"));
    }

    @Test void studentCannotEditOrDeletePosts() {
        assertFalse(Authorization.canManagePost("STUDENT", 10L, 10L));
    }

    @Test void crCanOnlyManageOwnPost() {
        assertTrue(Authorization.canManagePost("CR", 10L, 10L));
        assertFalse(Authorization.canManagePost("CR", 10L, 11L));
    }

    @Test void adminCanManageAnyPost() {
        assertTrue(Authorization.canManagePost("ADMIN", 99L, 10L));
    }
}
