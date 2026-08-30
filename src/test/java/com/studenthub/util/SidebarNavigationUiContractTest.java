package com.studenthub.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SidebarNavigationUiContractTest {
    @Test void sharedSidebarUsesPreForwardAdminRouteStateAndAriaCurrent() throws Exception {
        String jsp = Files.readString(Path.of("src/main/webapp/WEB-INF/views/partials/sidebar.jsp"));
        assertTrue(jsp.contains("${activeNav eq 'ADMIN_DASHBOARD'}"));
        assertTrue(jsp.contains("${activeNav eq 'ADMIN_USERS'}"));
        assertTrue(jsp.contains("${activeNav eq 'ADMIN_ACADEMIC_REQUESTS'}"));
        assertTrue(jsp.contains("${adminDashboardActive ? 'aria-current=\"page\"' : ''}"));
        assertTrue(jsp.contains("${adminUsersActive ? 'aria-current=\"page\"' : ''}"));
        assertTrue(jsp.contains("${adminAcademicActive ? 'aria-current=\"page\"' : ''}"));
        assertFalse(jsp.contains("pageContext.request.requestURI"));
        assertFalse(jsp.contains("pageContext.request.servletPath"));
    }
}
