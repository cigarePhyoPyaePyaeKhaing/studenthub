package com.studenthub.service;

import com.studenthub.model.DiscussionScope;
import com.studenthub.util.DiscussionAccess;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AttachmentAuthorizationTest {

    @Test
    void testDiscussionAttachmentAccessAllowsScopeMembers() {
        // Section room: member of semester 3, section A can access
        assertTrue(DiscussionAccess.canAccess("STUDENT", 3, "A", DiscussionScope.SECTION, 3, "A"));

        // Section room: member of semester 3, section B is denied
        assertFalse(DiscussionAccess.canAccess("STUDENT", 3, "B", DiscussionScope.SECTION, 3, "A"));

        // Section room: member of semester 4, section A is denied
        assertFalse(DiscussionAccess.canAccess("STUDENT", 4, "A", DiscussionScope.SECTION, 3, "A"));
    }

    @Test
    void testDiscussionAttachmentAccessEnforcesCrOnly() {
        // CR room: student is denied even if in same semester
        assertFalse(DiscussionAccess.canAccess("STUDENT", 3, "A", DiscussionScope.CR_SEMESTER, 3, null));

        // CR room: CR in same semester is allowed
        assertTrue(DiscussionAccess.canAccess("CR", 3, "A", DiscussionScope.CR_SEMESTER, 3, null));

        // CR room: Admin is allowed
        assertTrue(DiscussionAccess.canAccess("ADMIN", null, null, DiscussionScope.CR_SEMESTER, 3, null));
    }

    @Test
    void testDiscussionAttachmentAccessAllRoomAllowsAllStudents() {
        assertTrue(DiscussionAccess.canAccess("STUDENT", 1, "A", DiscussionScope.ALL, null, null));
        assertTrue(DiscussionAccess.canAccess("CR", 5, "B", DiscussionScope.ALL, null, null));
        assertTrue(DiscussionAccess.canAccess("ADMIN", null, null, DiscussionScope.ALL, null, null));
    }
}
