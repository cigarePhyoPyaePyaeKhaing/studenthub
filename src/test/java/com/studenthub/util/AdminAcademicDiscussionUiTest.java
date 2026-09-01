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
    void adminScopeMarkupExposesOnlyAllStudentsAdminAndCrAdmin() throws IOException {
        String jsp = source("src/main/webapp/WEB-INF/views/discussions/index.jsp");

        // The admin branch must exist and contain exactly the two admin communication scopes
        assertTrue(jsp.contains("sessionScope.role eq 'ADMIN'"));
        assertTrue(jsp.contains("room-tabs-two"));
        assertTrue(jsp.contains("scope=ALL_STUDENTS_ADMIN\">All Students – Admin</a>"));
        assertTrue(jsp.contains("scope=CR_ADMIN\">CR – Admin</a>"));

        // Admin branch must not contain Section/Semester/CR_SEMESTER/CR_ALL/scope=ALL inside it
        int adminStart = jsp.indexOf("sessionScope.role eq 'ADMIN'");
        int crStart = jsp.indexOf("sessionScope.role eq 'CR'", adminStart);
        assertTrue(adminStart > 0 && crStart > adminStart);
        String adminBranch = jsp.substring(adminStart, crStart);

        assertTrue(adminBranch.contains("All Students – Admin"));
        assertTrue(adminBranch.contains("CR – Admin"));
        assertFalse(adminBranch.contains("scope=ALL\""));
        assertFalse(adminBranch.contains("scope=SECTION"));
        assertFalse(adminBranch.contains("scope=SEMESTER"));
        assertFalse(adminBranch.contains("scope=CR_SEMESTER"));
        assertFalse(adminBranch.contains("scope=CR_ALL"));
        assertFalse(adminBranch.contains("disabled"));

        // CR branch contains CR – Admin but not All Students – Admin
        int otherwiseStart = jsp.indexOf("<c:otherwise>", crStart);
        String crBranch = jsp.substring(crStart, otherwiseStart);
        assertTrue(crBranch.contains("scope=CR_ADMIN\">CR – Admin</a>"));
        assertFalse(crBranch.contains("scope=ALL_STUDENTS_ADMIN"));

        // Student branch contains All Students – Admin but not CR – Admin
        String studentBranch = jsp.substring(otherwiseStart);
        assertTrue(studentBranch.contains("scope=ALL_STUDENTS_ADMIN\">All Students – Admin</a>"));
        assertFalse(studentBranch.contains("scope=CR_ADMIN"));
    }

    @Test
    void cssProvidesBalancedTwoColumnLayoutForAdminRoomTabs() throws IOException {
        String css = source("src/main/webapp/assets/css/dashboard.css");
        assertTrue(css.contains(".discussions-shell .room-tabs-two{grid-template-columns:repeat(2,minmax(0,1fr))}"));
        assertTrue(css.contains(".discussions-shell .room-tabs-two{grid-template-columns:repeat(2,minmax(0,1fr))!important}"));
    }

    @Test
    void roleAccessMatrixStrictlyEnforced() {
        // Admin: only ALL_STUDENTS_ADMIN and CR_ADMIN
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.ALL_STUDENTS_ADMIN, "ADMIN"));
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.CR_ADMIN, "ADMIN"));
        assertFalse(DiscussionAccess.roleMayAccess(DiscussionScope.ALL, "ADMIN"));
        assertFalse(DiscussionAccess.roleMayAccess(DiscussionScope.SECTION, "ADMIN"));
        assertFalse(DiscussionAccess.roleMayAccess(DiscussionScope.SEMESTER, "ADMIN"));
        assertFalse(DiscussionAccess.roleMayAccess(DiscussionScope.CR_SEMESTER, "ADMIN"));
        assertFalse(DiscussionAccess.roleMayAccess(DiscussionScope.CR_ALL, "ADMIN"));

        // Student: ALL_STUDENTS_ADMIN, SECTION, SEMESTER, ALL; denied CR scopes
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.ALL_STUDENTS_ADMIN, "STUDENT"));
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.SECTION, "STUDENT"));
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.SEMESTER, "STUDENT"));
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.ALL, "STUDENT"));
        assertFalse(DiscussionAccess.roleMayAccess(DiscussionScope.CR_ADMIN, "STUDENT"));
        assertFalse(DiscussionAccess.roleMayAccess(DiscussionScope.CR_SEMESTER, "STUDENT"));
        assertFalse(DiscussionAccess.roleMayAccess(DiscussionScope.CR_ALL, "STUDENT"));

        // CR: CR_ADMIN, SECTION, SEMESTER, ALL, CR_SEMESTER, CR_ALL; denied ALL_STUDENTS_ADMIN
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.SECTION, "CR"));
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.SEMESTER, "CR"));
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.ALL, "CR"));
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.CR_ADMIN, "CR"));
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.CR_SEMESTER, "CR"));
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.CR_ALL, "CR"));
        assertFalse(DiscussionAccess.roleMayAccess(DiscussionScope.ALL_STUDENTS_ADMIN, "CR"));
    }

    @Test
    void adminTargetedRoomsAreGlobalAndIndependentOfAcademicInfo() {
        assertNull(DiscussionAccess.denialReason(DiscussionScope.CR_ADMIN, null, null, null));
        assertNull(DiscussionAccess.denialReason(DiscussionScope.CR_ADMIN, 1L, null, null));
        assertNull(DiscussionAccess.denialReason(DiscussionScope.CR_ADMIN, 1L, 4, "A"));

        assertNull(DiscussionAccess.denialReason(DiscussionScope.ALL_STUDENTS_ADMIN, null, null, null));
        assertNull(DiscussionAccess.denialReason(DiscussionScope.ALL_STUDENTS_ADMIN, 1L, null, null));
        assertNull(DiscussionAccess.denialReason(DiscussionScope.ALL_STUDENTS_ADMIN, 1L, 4, "A"));

        DiscussionTarget crAdminTarget = DiscussionTarget.fromAuthenticatedUser(100L, DiscussionScope.CR_ADMIN, 1L, 4, "A");
        assertEquals(100L, crAdminTarget.authorId());
        assertEquals(DiscussionScope.CR_ADMIN, crAdminTarget.scope());
        assertNull(crAdminTarget.universityId());
        assertNull(crAdminTarget.semester());
        assertNull(crAdminTarget.sectionName());

        DiscussionTarget studentAdminTarget = DiscussionTarget.fromAuthenticatedUser(101L, DiscussionScope.ALL_STUDENTS_ADMIN, 1L, 4, "A");
        assertEquals(101L, studentAdminTarget.authorId());
        assertEquals(DiscussionScope.ALL_STUDENTS_ADMIN, studentAdminTarget.scope());
        assertNull(studentAdminTarget.universityId());
        assertNull(studentAdminTarget.semester());
        assertNull(studentAdminTarget.sectionName());
    }

    @Test
    void privateMessagesFileIntegrityMaintained() throws IOException {
        String jsp = source("src/main/webapp/WEB-INF/views/messages/index.jsp");
        assertTrue(jsp.contains("private-chat-layout"));
        assertTrue(jsp.contains("conversation-list"));
        assertTrue(jsp.contains("private-message-list"));
    }
}