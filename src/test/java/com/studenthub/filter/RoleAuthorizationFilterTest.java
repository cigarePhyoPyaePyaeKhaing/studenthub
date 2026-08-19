package com.studenthub.filter;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RoleAuthorizationFilterTest {
    @Test void exactAdminRootRequiresAdminClassification() {
        assertTrue(RoleAuthorizationFilter.isAdminPath("/studenthub", "/studenthub/admin"));
    }

    @Test void nestedAdminRoutesRequireAdminClassification() {
        assertTrue(RoleAuthorizationFilter.isAdminPath("/studenthub", "/studenthub/admin/users"));
    }

    @Test void similarPrefixIsNotAnAdminRoute() {
        assertFalse(RoleAuthorizationFilter.isAdminPath("/studenthub", "/studenthub/administrator"));
    }

    @Test void unreadCountIsSkippedForMutationsAndNonSidebarForms() {
        assertFalse(AuthenticationFilter.needsUnreadCount("POST", "/studenthub", "/studenthub/discussions/messages"));
        assertFalse(AuthenticationFilter.needsUnreadCount("GET", "/studenthub", "/studenthub/posts/create"));
    }

    @Test void unreadCountIsLoadedOnceForSidebarPages() {
        assertTrue(AuthenticationFilter.needsUnreadCount("GET", "/studenthub", "/studenthub/home"));
        assertTrue(AuthenticationFilter.needsUnreadCount("GET", "/studenthub", "/studenthub/admin/users"));
    }
}
