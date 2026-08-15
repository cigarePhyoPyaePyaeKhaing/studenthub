package com.studenthub.util;

import com.studenthub.model.DiscussionScope;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DiscussionSecurityTest {
    @Test void unauthenticatedUserIdIsRejected() {
        assertFalse(Authorization.isAuthenticatedUserId(null));
        assertFalse(Authorization.isAuthenticatedUserId(0L));
    }

    @Test void studentCanDeleteOwnMessage() {
        assertTrue(DiscussionAuthorization.canDelete("STUDENT", 7, 7));
    }

    @Test void studentCannotDeleteAnotherMessage() {
        assertFalse(DiscussionAuthorization.canDelete("STUDENT", 7, 8));
    }

    @Test void crCannotDeleteAnotherMessage() {
        assertFalse(DiscussionAuthorization.canDelete("CR", 7, 8));
    }

    @Test void adminCanDeleteAnyMessage() {
        assertTrue(DiscussionAuthorization.canDelete("ADMIN", 7, 8));
    }

    @Test void messageAuthorComesOnlyFromAuthenticatedIdentity() {
        DiscussionTarget target = DiscussionTarget.fromAuthenticatedUser(42, DiscussionScope.ALL, 9, "Spoof");
        assertEquals(42, target.authorId());
    }

    @Test void semesterAndSectionAreDerivedFromAuthenticatedProfile() {
        DiscussionTarget target = DiscussionTarget.fromAuthenticatedUser(42, DiscussionScope.SECTION, 4, "B");
        assertEquals(4, target.semester());
        assertEquals("B", target.sectionName());
        DiscussionTarget semester = DiscussionTarget.fromAuthenticatedUser(42, DiscussionScope.SEMESTER, 4, "B");
        assertNull(semester.sectionName());
    }

    @Test void invalidScopeFallsBackSafely() {
        assertEquals(DiscussionScope.SECTION, DiscussionScope.fromRequest("ADMIN_ROOM"));
        assertEquals(DiscussionScope.SECTION, DiscussionScope.fromRequest(null));
    }
}
