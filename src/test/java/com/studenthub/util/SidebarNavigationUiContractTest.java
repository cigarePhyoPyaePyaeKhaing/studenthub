package com.studenthub.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SidebarNavigationUiContractTest {
    @Test void sharedSidebarUsesQueryIndependentAdminRouteStateAndAriaCurrent() throws Exception {
        String jsp = Files.readString(Path.of("src/main/webapp/WEB-INF/views/partials/sidebar.jsp"));
        assertTrue(jsp.contains("${pageContext.request.servletPath}"));
        assertTrue(jsp.contains("currentPath eq '/admin'"));
        assertTrue(jsp.contains("currentPath eq '/admin/users' or currentPath.startsWith('/admin/users/')"));
        assertTrue(jsp.contains("currentPath eq '/admin/academic-changes' or currentPath.startsWith('/admin/academic-changes/')"));
        assertTrue(jsp.contains("${adminDashboardActive ? 'aria-current=\"page\"' : ''}"));
        assertTrue(jsp.contains("${adminUsersActive ? 'aria-current=\"page\"' : ''}"));
        assertTrue(jsp.contains("${adminAcademicActive ? 'aria-current=\"page\"' : ''}"));
        assertFalse(jsp.contains("pageContext.request.requestURI"));
    }
}
