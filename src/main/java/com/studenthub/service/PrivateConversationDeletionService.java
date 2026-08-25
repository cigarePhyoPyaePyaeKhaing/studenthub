package com.studenthub.service;

import com.studenthub.dao.PrivateMessageDAO;
import java.sql.SQLException;

public class PrivateConversationDeletionService {
    private final PrivateMessageDAO dao;

    public PrivateConversationDeletionService() {
        this(new PrivateMessageDAO());
    }

    public PrivateConversationDeletionService(PrivateMessageDAO dao) {
        this.dao = dao;
    }

    public DeleteResult deleteForUser(long conversationId, long userId) throws SQLException {
        PrivateMessageDAO.HideResult result = dao.hideWithDiagnostics(conversationId, userId);
        if (!result.participant()) return new DeleteResult("DELETE_FORBIDDEN", false, 0);
        if (result.affectedRows() < 1) return new DeleteResult("DELETE_NOT_FOUND", true, 0);
        return new DeleteResult("DELETE_OK", true, result.affectedRows());
    }

    public record DeleteResult(String code, boolean participant, int affectedRows) {}
}
