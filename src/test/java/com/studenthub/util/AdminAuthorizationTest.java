package com.studenthub.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AdminAuthorizationTest {
    @Test void anonymousCannotAccessAdmin() { assertFalse(Authorization.isAuthenticatedUserId(null)); }
    @Test void studentCannotAccessAdmin() { assertFalse(Authorization.isAdmin("STUDENT")); }
    @Test void crCannotAccessAdmin() { assertFalse(Authorization.isAdmin("CR")); }
    @Test void adminCanAccessDashboardAndUsers() { assertTrue(Authorization.isAdmin("ADMIN")); }
    @Test void nonAdminCannotPerformMutation() {
        assertFalse(AdminMutationPolicy.canProceed("STUDENT", true));
        assertFalse(AdminMutationPolicy.canProceed("CR", true));
    }
    @Test void roleMutationRequiresCsrf() { assertFalse(AdminMutationPolicy.canProceed("ADMIN", false)); }
    @Test void adminWithCsrfCanPerformMutation() { assertTrue(AdminMutationPolicy.canProceed("ADMIN", true)); }
}
