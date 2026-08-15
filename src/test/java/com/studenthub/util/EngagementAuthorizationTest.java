package com.studenthub.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EngagementAuthorizationTest {
    @Test void unauthenticatedEngagementRejectedByExistingPolicy() {
        assertFalse(Authorization.isAuthenticatedUserId(null));
    }
    @Test void studentDeletesOwnComment() {
        assertTrue(EngagementAuthorization.canDeleteComment("STUDENT", 4, 4));
    }
    @Test void studentCannotDeleteAnotherComment() {
        assertFalse(EngagementAuthorization.canDeleteComment("STUDENT", 4, 5));
    }
    @Test void crCannotDeleteAnotherComment() {
        assertFalse(EngagementAuthorization.canDeleteComment("CR", 4, 5));
    }
    @Test void adminCanDeleteAnyComment() {
        assertTrue(EngagementAuthorization.canDeleteComment("ADMIN", 4, 5));
    }
    @Test void missingRoleCannotDelete() {
        assertFalse(EngagementAuthorization.canDeleteComment(null, 4, 4));
    }
    @Test void missingCommentProducesNotFoundDecision() {
        assertEquals(EngagementAuthorization.DeleteDecision.NOT_FOUND,
                EngagementAuthorization.commentDeleteDecision(false, "ADMIN", 4, 5));
    }
    @Test void unauthorizedCommentProducesForbiddenDecision() {
        assertEquals(EngagementAuthorization.DeleteDecision.FORBIDDEN,
                EngagementAuthorization.commentDeleteDecision(true, "STUDENT", 4, 5));
    }
}
