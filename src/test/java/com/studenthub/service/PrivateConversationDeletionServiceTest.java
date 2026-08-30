package com.studenthub.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.studenthub.dao.PrivateMessageDAO;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;

class PrivateConversationDeletionServiceTest {
    @Test void alreadyHiddenConversationIsIdempotentSuccess() throws Exception {
        PrivateConversationDeletionService.DeleteResult result = new PrivateConversationDeletionService(
                daoReturning(new PrivateMessageDAO.HideResult(true, 0))).deleteForUser(9L, 4L);
        assertTrue(result.participant());
        assertEquals("DELETE_OK", result.code());
        assertEquals(0, result.affectedRows());
    }

    @Test void nonParticipantRemainsForbidden() throws Exception {
        PrivateConversationDeletionService.DeleteResult result = new PrivateConversationDeletionService(
                daoReturning(new PrivateMessageDAO.HideResult(false, 0))).deleteForUser(9L, 4L);
        assertEquals("DELETE_FORBIDDEN", result.code());
        assertFalse(result.participant());
    }

    @Test void anonymizedPeerDoesNotChangeParticipantHideSemantics() throws Exception {
        PrivateConversationDeletionService.DeleteResult result = new PrivateConversationDeletionService(
                daoReturning(new PrivateMessageDAO.HideResult(true, 1))).deleteForUser(15L, 7L);
        assertEquals("DELETE_OK", result.code());
        assertTrue(result.participant());
        assertEquals(1, result.affectedRows());
    }

    @Test void sqlFailurePropagatesToServletBoundary() {
        PrivateMessageDAO dao = new PrivateMessageDAO() {
            @Override public HideResult hideWithDiagnostics(long conversation, long user) throws SQLException {
                throw new SQLException("database unavailable");
            }
        };
        assertThrows(SQLException.class,
                () -> new PrivateConversationDeletionService(dao).deleteForUser(9L, 4L));
    }

    private static PrivateMessageDAO daoReturning(PrivateMessageDAO.HideResult result) {
        return new PrivateMessageDAO() {
            @Override public HideResult hideWithDiagnostics(long conversation, long user) { return result; }
        };
    }
}
