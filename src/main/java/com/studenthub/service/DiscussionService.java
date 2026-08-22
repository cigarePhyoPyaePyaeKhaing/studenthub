package com.studenthub.service;

import com.studenthub.dao.DiscussionDAO;
import com.studenthub.model.DiscussionMessage;
import com.studenthub.model.DiscussionScope;
import com.studenthub.util.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class DiscussionService {
    public record RoomView(DiscussionScope scope, Integer semester, String sectionName, boolean crRoomsVisible,
                           String denialReason, List<DiscussionMessage> messages) {
        public boolean available() { return denialReason == null; }
        public boolean isAvailable() { return available(); }
        public DiscussionScope getScope() { return scope; }
        public Integer getSemester() { return semester; }
        public String getSectionName() { return sectionName; }
        public boolean isCrRoomsVisible() { return crRoomsVisible; }
        public String getDenialReason() { return denialReason; }
        public List<DiscussionMessage> getMessages() { return messages; }
        public String scopeLabel() {
            if (scope == DiscussionScope.ALL) return "All StudentHub users";
            if (scope == DiscussionScope.CR_ALL) return "CRs across all semesters";
            if (scope == DiscussionScope.CR_SEMESTER) return "CRs in Semester " + semester;
            if (scope == DiscussionScope.SEMESTER) return "Semester " + semester;
            return "Semester " + semester + " / Section " + sectionName;
        }
        public String getScopeLabel() { return scopeLabel(); }
    }
    public record OperationResult(boolean successful, String message) {}

    private final DiscussionDAO dao = new DiscussionDAO();

    public RoomView load(long userId, String requestedScope) throws SQLException {
        DiscussionScope scope = DiscussionScope.fromRequest(requestedScope);
        DiscussionDAO.AcademicProfile profile = dao.findAcademicProfile(userId);
        if (!DiscussionAccess.roleMayAccess(scope, profile.role())) throw new SecurityException("FORBIDDEN");
        String denial = DiscussionAccess.denialReason(scope, profile.semester(), profile.sectionName());
        DiscussionTarget target = DiscussionTarget.fromAuthenticatedUser(userId, scope,
                profile.semester(), profile.sectionName());
        return new RoomView(scope, profile.semester(), profile.sectionName(),
                DiscussionAccess.roleMayAccess(DiscussionScope.CR_ALL, profile.role()), denial,
                denial == null ? dao.findRecent(target, 50) : List.of());
    }

    public OperationResult send(long userId, String requestedScope, String rawMessage) throws SQLException {
        return send(userId, requestedScope, rawMessage, null);
    }

    public OperationResult send(long userId, String requestedScope, String rawMessage,
                                com.studenthub.model.Attachment attachment) throws SQLException {
        DiscussionScope scope = DiscussionScope.fromRequest(requestedScope);
        String messageInput = rawMessage == null ? "" : rawMessage;
        String validation = attachment != null && messageInput.isBlank() ? null : DiscussionValidation.validate(messageInput);
        if (validation != null) return new OperationResult(false, validation);
        DiscussionDAO.AcademicProfile profile = dao.findAcademicProfile(userId);
        if (!DiscussionAccess.roleMayAccess(scope, profile.role())) throw new SecurityException("FORBIDDEN");
        String denial = DiscussionAccess.denialReason(scope, profile.semester(), profile.sectionName());
        if (denial != null) return new OperationResult(false, denial);
        DiscussionTarget target = DiscussionTarget.fromAuthenticatedUser(userId, scope,
                profile.semester(), profile.sectionName());
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                long id = dao.insert(connection, target,
                        messageInput.isBlank() ? "" : DiscussionValidation.normalize(messageInput), attachment);
                if (id <= 0) throw new SQLException("Message identifier was unavailable after insertion.");
                connection.commit();
                return new OperationResult(true, "Message sent.");
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }

    public OperationResult delete(long userId, Object role, long messageId) throws SQLException {
        DiscussionDAO.AcademicProfile profile = dao.findAcademicProfile(userId);
        Object currentRole = profile.role();
        DiscussionDAO.MessageRecord message = dao.findMessage(messageId);
        if (message == null) return new OperationResult(false, "NOT_FOUND");
        if (!DiscussionAuthorization.canDelete(currentRole, userId, message.senderId())) {
            return new OperationResult(false, "FORBIDDEN");
        }
        if (!"ADMIN".equals(String.valueOf(currentRole))) {
            if (!DiscussionAccess.roleMayAccess(message.scope(), profile.role())) {
                return new OperationResult(false, "FORBIDDEN");
            }
            if (!DiscussionAccess.matches(message.scope(), profile.semester(), profile.sectionName(),
                    message.semester(), message.sectionName())) return new OperationResult(false, "FORBIDDEN");
        }
        return dao.delete(messageId) == 1
                ? new OperationResult(true, "Message deleted.")
                : new OperationResult(false, "NOT_FOUND");
    }
}
