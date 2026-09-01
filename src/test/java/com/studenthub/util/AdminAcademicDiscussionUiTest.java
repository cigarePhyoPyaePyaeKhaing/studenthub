package com.studenthub.util;

import static org.junit.jupiter.api.Assertions.*;

import com.studenthub.model.DiscussionScope;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class AdminAcademicDiscussionUiTest {
    private static String source(String relativePath) throws IOException {
        return Files.readString(Path.of(relativePath));
    }

    @Test
    void adminScopeMarkupExposesOnlyAllStudentsAndCrAdmin() throws IOException {
        String jsp = source("src/main/webapp/WEB-INF/views/discussions/index.jsp");

        // The admin branch must exist and contain exactly the two scopes
        assertTrue(jsp.contains("sessionScope.role eq 'ADMIN'"));
        assertTrue(jsp.contains("room-tabs-two"));
        assertTrue(jsp.contains("scope=ALL\">All Students</a>"));
        assertTrue(jsp.contains("scope=CR_ADMIN\">CR - Admin</a>"));

        // Admin branch must not contain Section/Semester/CR_SEMESTER/CR_ALL inside it
        int adminStart = jsp.indexOf("sessionScope.role eq 'ADMIN'");
        int otherwiseStart = jsp.indexOf("<c:otherwise>", adminStart);
        assertTrue(adminStart > 0 && otherwiseStart > adminStart);
        String adminBranch = jsp.substring(adminStart, otherwiseStart);

        assertTrue(adminBranch.contains("All Students"));
        assertTrue(adminBranch.contains("CR - Admin"));
        assertFalse(adminBranch.contains("scope=SECTION"));
        assertFalse(adminBranch.contains("scope=SEMESTER"));
        assertFalse(adminBranch.contains("scope=CR_SEMESTER"));
        assertFalse(adminBranch.contains("scope=CR_ALL"));
        assertFalse(adminBranch.contains("disabled"));
    }

    @Test
    void cssProvidesBalancedTwoColumnLayoutForAdminRoomTabs() throws IOException {
        String css = source("src/main/webapp/assets/css/dashboard.css");
        assertTrue(css.contains(".discussions-shell .room-tabs-two{grid-template-columns:repeat(2,minmax(0,1fr))}"));
        assertTrue(css.contains(".discussions-shell .room-tabs-two{grid-template-columns:repeat(2,minmax(0,1fr))!important}"));
    }

    @Test
    void roleAccessMatrixStrictlyEnforced() {
        // Admin: only ALL and CR_ADMIN
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.ALL, "ADMIN"));
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.CR_ADMIN, "ADMIN"));
        assertFalse(DiscussionAccess.roleMayAccess(DiscussionScope.SECTION, "ADMIN"));
        assertFalse(DiscussionAccess.roleMayAccess(DiscussionScope.SEMESTER, "ADMIN"));
        assertFalse(DiscussionAccess.roleMayAccess(DiscussionScope.CR_SEMESTER, "ADMIN"));
        assertFalse(DiscussionAccess.roleMayAccess(DiscussionScope.CR_ALL, "ADMIN"));

        // Student: SECTION, SEMESTER, ALL; denied CR scopes
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.SECTION, "STUDENT"));
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.SEMESTER, "STUDENT"));
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.ALL, "STUDENT"));
        assertFalse(DiscussionAccess.roleMayAccess(DiscussionScope.CR_ADMIN, "STUDENT"));
        assertFalse(DiscussionAccess.roleMayAccess(DiscussionScope.CR_SEMESTER, "STUDENT"));
        assertFalse(DiscussionAccess.roleMayAccess(DiscussionScope.CR_ALL, "STUDENT"));

        // CR: all scopes allowed
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.SECTION, "CR"));
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.SEMESTER, "CR"));
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.ALL, "CR"));
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.CR_ADMIN, "CR"));
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.CR_SEMESTER, "CR"));
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.CR_ALL, "CR"));
    }

    @Test
    void crAdminIsGlobalAndIndependentOfAcademicInfo() {
        assertNull(DiscussionAccess.denialReason(DiscussionScope.CR_ADMIN, null, null, null));
        assertNull(DiscussionAccess.denialReason(DiscussionScope.CR_ADMIN, 1L, null, null));
        assertNull(DiscussionAccess.denialReason(DiscussionScope.CR_ADMIN, 1L, 4, "A"));

        DiscussionTarget target = DiscussionTarget.fromAuthenticatedUser(100L, DiscussionScope.CR_ADMIN, 1L, 4, "A");
        assertEquals(100L, target.authorId());
        assertEquals(DiscussionScope.CR_ADMIN, target.scope());
        assertNull(target.universityId());
        assertNull(target.semester());
        assertNull(target.sectionName());
    }

    @Test
    void privateMessagesFileIntegrityMaintained() throws IOException {
        String jsp = source("src/main/webapp/WEB-INF/views/messages/index.jsp");
        assertTrue(jsp.contains("private-chat-layout"));
        assertTrue(jsp.contains("conversation-list"));
        assertTrue(jsp.contains("private-message-list"));
    }
}