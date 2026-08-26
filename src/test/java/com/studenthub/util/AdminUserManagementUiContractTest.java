package com.studenthub.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class AdminUserManagementUiContractTest {
    @Test void existingUserDatasetIsGroupedIntoThreeRoleSectionsInTheJsp() throws IOException {
        String jsp = Files.readString(Path.of("src/main/webapp/WEB-INF/views/admin/users.jsp"));
        assertTrue(jsp.contains("data-role-section=\"STUDENT\""));
        assertTrue(jsp.contains("data-role-section=\"CR\""));
        assertTrue(jsp.contains("data-role-section=\"ADMIN\""));
        assertTrue(jsp.contains("items=\"${users}\""));
        assertEquals(0, occurrences(jsp, "items=\"${studentUsers}\""));
        assertEquals(0, occurrences(jsp, "items=\"${crUsers}\""));
        assertEquals(0, occurrences(jsp, "items=\"${adminUsers}\""));
    }

    @Test void searchPaginationAndExistingViewRouteRemainIntact() throws IOException {
        String jsp = Files.readString(Path.of("src/main/webapp/WEB-INF/views/admin/users.jsp"));
        assertTrue(jsp.contains("action=\"${pageContext.request.contextPath}/admin/users\""));
        assertTrue(jsp.contains("name=\"q\""));
        assertTrue(jsp.contains("${pageContext.request.contextPath}/admin/users/view?id=${user.userId}"));
        assertTrue(jsp.contains("currentPage"));
        assertTrue(jsp.contains("totalPages"));
        assertFalse(jsp.contains("/admin/users/students"));
        assertFalse(jsp.contains("/admin/users/cr"));
        assertFalse(jsp.contains("/admin/users/admins"));
    }

    @Test void roleTablesUseResponsivePresentationOnly() throws IOException {
        String css = Files.readString(Path.of("src/main/webapp/assets/css/dashboard.css"));
        assertTrue(css.contains(".role-user-table-wrap"));
        assertTrue(css.contains("overflow-x:auto"));
        assertTrue(css.contains(".role-section-student .admin-role-icon"));
        assertTrue(css.contains(".role-section-cr .admin-role-icon"));
        assertTrue(css.contains(".role-section-admin .admin-role-icon"));
    }

    @Test void roleTabsAreAccessibleEqualWidthAndStudentsAreTheOnlyInitialPanel() throws IOException {
        String jsp=Files.readString(Path.of("src/main/webapp/WEB-INF/views/admin/users.jsp"));
        String css=Files.readString(Path.of("src/main/webapp/assets/css/dashboard.css"));
        String js=Files.readString(Path.of("src/main/webapp/assets/js/admin-users.js"));
        assertTrue(jsp.contains("role=\"tablist\""));
        assertEquals(3,occurrences(jsp,"role=\"tab\""));
        assertEquals(3,occurrences(jsp,"role=\"tabpanel\""));
        assertTrue(jsp.contains("id=\"student-tab\" aria-controls=\"student-panel\" aria-selected=\"true\""));
        assertEquals(2,occurrences(jsp,"data-role-section=\"")-occurrences(jsp,"data-role-section=\"STUDENT\""));
        assertEquals(2,occurrences(jsp," hidden>"));
        assertTrue(css.contains("grid-template-columns:repeat(3,minmax(0,1fr))"));
        assertTrue(js.contains("ArrowRight")); assertTrue(js.contains("ArrowLeft"));
    }

    private static int occurrences(String value, String token) {
        int count = 0;
        for (int index = 0; (index = value.indexOf(token, index)) >= 0; index += token.length()) count++;
        return count;
    }
}
