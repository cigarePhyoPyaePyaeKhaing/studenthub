package com.studenthub.service;

import com.studenthub.dao.DiscussionDAO;
import com.studenthub.model.DiscussionMessage;
import com.studenthub.model.DiscussionScope;
import com.studenthub.util.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import com.studenthub.dao.AttachmentDAO;

public class DiscussionService {
    public record ModerationScopeOption(String key, String group, String label,
                                        DiscussionScope scope, Long universityId,
                                        Integer semester, String sectionName) {
        public String getKey() { return key; }
        public String getGroup() { return group; }
        public String getLabel() { return label; }
        public Integer getSemester() { return semester; }
        public String getSectionName() { return sectionName; }
    }
    public record RoomView(DiscussionScope scope, Integer semester, String sectionName, boolean crRoomsVisible,
                           boolean semesterRoomAvailable, boolean sectionRoomAvailable,
                           boolean crSemesterRoomAvailable, String denialReason, List<DiscussionMessage> messages) {
        public boolean available() { return denialReason == null; }
        public boolean isAvailable() { return available(); }
        public DiscussionScope getScope() { return scope; }
        public Integer getSemester() { return semester; }
        public String getSectionName() { return sectionName; }
        public boolean isCrRoomsVisible() { return crRoomsVisible; }
        public boolean isSemesterRoomAvailable() { return semesterRoomAvailable; }
        public boolean isSectionRoomAvailable() { return sectionRoomAvailable; }
        public boolean isCrSemesterRoomAvailable() { return crSemesterRoomAvailable; }
        public String getDenialReason() { return denialReason; }
        public List<DiscussionMessage> getMessages() { return messages; }
        public String scopeLabel() {
            if (scope == DiscussionScope.ALL) return "All Students";
            if (scope == DiscussionScope.ALL_STUDENTS_ADMIN) return "All Students – Admin";
            if (scope == DiscussionScope.CR_ALL) return "CRs across all semesters";
            if (scope == DiscussionScope.CR_SEMESTER) return "CRs in Semester " + semester;
            if (scope == DiscussionScope.CR_ADMIN) return "CR – Admin";
            if (scope == DiscussionScope.SEMESTER) return "Semester " + semester;
            return "Semester " + semester + " / Section " + sectionName;
        }
        public String getScopeLabel() { return scopeLabel(); }
    }
    public record OperationResult(boolean successful, String message) {}

    @FunctionalInterface
    public interface ConnectionSupplier {
        Connection get() throws SQLException;
    }

    private final DiscussionDAO dao;
    private final ConnectionSupplier connectionSupplier;

    public DiscussionService() { this(new DiscussionDAO(), DBConnection::getConnection); }
    public DiscussionService(DiscussionDAO dao) { this(dao, DBConnection::getConnection); }
    public DiscussionService(DiscussionDAO dao, ConnectionSupplier connectionSupplier) {
        this.dao = dao;
        this.connectionSupplier = connectionSupplier != null ? connectionSupplier : DBConnection::getConnection;
    }

    public RoomView load(long userId, String requestedScope) throws SQLException {
        return load(userId, requestedScope, null);
    }

    public RoomView load(long userId, String requestedScope, Long requestedRoomId) throws SQLException {
        return load(userId, requestedScope, requestedRoomId, null);
    }

    public RoomView load(long userId, String requestedScope, Long requestedRoomId,
                         String moderationScopeKey) throws SQLException {
        DiscussionScope scope = DiscussionScope.fromRequest(requestedScope);
        DiscussionDAO.AcademicProfile profile = dao.findAcademicProfile(userId);
        DiscussionTarget target;
        if ("ADMIN".equals(profile.role()) && moderationScopeKey != null && !moderationScopeKey.isBlank()) {
            ModerationScopeOption option = resolveModerationScope(userId, moderationScopeKey);
            target = new DiscussionTarget(userId, option.scope(), option.universityId(),
                    option.semester(), option.sectionName());
            return roomView(option.scope(), option.semester(), option.sectionName(), profile,
                    null, target, true);
        }
        if ("ADMIN".equals(profile.role()) && requestedRoomId != null) {
            DiscussionDAO.RoomOption room = dao.findRoom(requestedRoomId);
            if (room == null || !DiscussionAccess.roleMayAccess(room.scope(), profile.role())) {
                throw new SecurityException("FORBIDDEN");
            }
            scope = room.scope();
            target = new DiscussionTarget(userId, scope, room.universityId(), room.semester(), room.sectionName());
            return roomView(scope, room.semester(), room.sectionName(), profile, null, target, true);
        }
        if (requestedScope == null || requestedScope.isBlank()) {
            if ("ADMIN".equals(profile.role())) {
                scope = DiscussionScope.ALL;
            } else if (DiscussionAccess.denialReason(DiscussionScope.SECTION, profile.universityId(), profile.semester(), profile.sectionName()) != null) {
                scope = DiscussionScope.ALL;
            }
        }
        if (!DiscussionAccess.roleMayAccess(scope, profile.role())) throw new SecurityException("FORBIDDEN");
        String denial = DiscussionAccess.denialReason(scope, profile.universityId(), profile.semester(), profile.sectionName());
        target = DiscussionTarget.fromAuthenticatedUser(userId, scope, profile.universityId(),
                profile.semester(), profile.sectionName());
        return roomView(scope, profile.semester(), profile.sectionName(), profile, denial, target, false);
    }

    private RoomView roomView(DiscussionScope scope, Integer semester, String sectionName,
                              DiscussionDAO.AcademicProfile profile, String denial, DiscussionTarget target,
                              boolean moderationView) throws SQLException {
        boolean crRoomsVisible = DiscussionAccess.roleMayAccess(DiscussionScope.CR_ALL, profile.role());
        return new RoomView(scope, semester, sectionName, crRoomsVisible,
                DiscussionAccess.denialReason(DiscussionScope.SEMESTER, profile.universityId(), profile.semester(), profile.sectionName()) == null,
                DiscussionAccess.denialReason(DiscussionScope.SECTION, profile.universityId(), profile.semester(), profile.sectionName()) == null,
                crRoomsVisible && DiscussionAccess.denialReason(DiscussionScope.CR_SEMESTER, profile.universityId(), profile.semester(), profile.sectionName()) == null,
                denial,
                denial == null ? (moderationView
                        ? dao.findRecentForModeration(target, 50) : dao.findRecent(target, 50)) : List.of());
    }

    public List<ModerationScopeOption> moderationRooms(long userId) throws SQLException {
        DiscussionDAO.AcademicProfile profile = dao.findAcademicProfile(userId);
        if (!"ADMIN".equals(profile.role())) throw new SecurityException("FORBIDDEN");
        List<ModerationScopeOption> options = new java.util.ArrayList<>();
        options.add(new ModerationScopeOption("all_students", "ALL", "All Students",
                DiscussionScope.ALL, null, null, null));
        options.add(new ModerationScopeOption("all_cr", "ALL", "All CRs",
                DiscussionScope.CR_ALL, null, null, null));
        List<DiscussionDAO.RoomOption> rooms = dao.findModerationRooms();
        java.util.Map<Integer, Long> semesterUniversities = new java.util.TreeMap<>();
        Long defaultUniversityId = dao.findModerationUniversityId();
        for (int semester = ProfileValidation.MIN_SEMESTER;
             semester <= ProfileValidation.MAX_SEMESTER; semester++) {
            semesterUniversities.put(semester, defaultUniversityId);
        }
        for (DiscussionDAO.RoomOption room : rooms) {
            if (room.scope() == DiscussionScope.SEMESTER && room.universityId() != null) {
                semesterUniversities.put(room.semester(), room.universityId());
            }
        }
        for (java.util.Map.Entry<Integer, Long> semester : semesterUniversities.entrySet()) {
            options.add(new ModerationScopeOption("semester:" + semester.getKey(), "SEMESTERS",
                    "Semester " + semester.getKey(), DiscussionScope.SEMESTER,
                    semester.getValue(), semester.getKey(), null));
        }
        for (DiscussionDAO.RoomOption room : rooms) {
            if (room.scope() == DiscussionScope.SEMESTER) {
                continue;
            } else if (room.scope() == DiscussionScope.SECTION) {
                String sectionName = room.sectionName() == null ? "" : room.sectionName().trim().toUpperCase(java.util.Locale.ROOT);
                if (sectionName.matches("[A-E]")) {
                    options.add(new ModerationScopeOption("section:" + room.semester() + ":" + sectionName,
                            "SECTIONS", "Semester " + room.semester() + " / Section " + sectionName,
                            room.scope(), room.universityId(), room.semester(), sectionName));
                }
            }
        }
        return List.copyOf(options);
    }

    private ModerationScopeOption resolveModerationScope(long userId, String key) throws SQLException {
        String normalized = key == null ? "" : key.trim();
        return moderationRooms(userId).stream().filter(option -> option.key().equalsIgnoreCase(normalized))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("INVALID_MODERATION_SCOPE"));
    }

    public OperationResult send(long userId, String requestedScope, String rawMessage) throws SQLException {
        return send(userId,requestedScope,rawMessage,null);
    }
    public OperationResult send(long userId,String requestedScope,String rawMessage,AttachmentUpload attachment)throws SQLException{
        return send(userId, requestedScope, rawMessage, attachment, null);
    }
    public OperationResult send(long userId,String requestedScope,String rawMessage,AttachmentUpload attachment,Long requestedRoomId)throws SQLException{
        DiscussionScope scope = DiscussionScope.fromRequest(requestedScope);
        String validation = DiscussionValidation.validate(rawMessage,attachment!=null);
        if (validation != null) return new OperationResult(false, validation);
        DiscussionDAO.AcademicProfile profile = dao.findAcademicProfile(userId);
        DiscussionTarget target;
        if ("ADMIN".equals(profile.role()) && requestedRoomId != null) {
            DiscussionDAO.RoomOption room = dao.findRoom(requestedRoomId);
            if (room == null || !DiscussionAccess.roleMayAccess(room.scope(), profile.role())) throw new SecurityException("FORBIDDEN");
            scope = room.scope();
            target = new DiscussionTarget(userId, scope, room.universityId(), room.semester(), room.sectionName());
        } else {
        if (!DiscussionAccess.roleMayAccess(scope, profile.role())) throw new SecurityException("FORBIDDEN");
        String denial = DiscussionAccess.denialReason(scope, profile.universityId(), profile.semester(), profile.sectionName());
        if (denial != null) return new OperationResult(false, denial);
        target = DiscussionTarget.fromAuthenticatedUser(userId, scope, profile.universityId(),
                profile.semester(), profile.sectionName());
        }
        return insertMessage(target, rawMessage, attachment);
    }

    public OperationResult sendModerated(long userId, String moderationScopeKey,
                                         String rawMessage, AttachmentUpload attachment) throws SQLException {
        String validation = DiscussionValidation.validate(rawMessage, attachment != null);
        if (validation != null) return new OperationResult(false, validation);
        DiscussionDAO.AcademicProfile profile = dao.findAcademicProfile(userId);
        if (!"ADMIN".equals(profile.role())) throw new SecurityException("FORBIDDEN");
        ModerationScopeOption option = resolveModerationScope(userId, moderationScopeKey);
        DiscussionTarget target = new DiscussionTarget(userId, option.scope(), option.universityId(),
                option.semester(), option.sectionName());
        return insertMessage(target, rawMessage, attachment);
    }

    private OperationResult insertMessage(DiscussionTarget target, String rawMessage,
                                          AttachmentUpload attachment) throws SQLException {
        try (Connection connection = connectionSupplier.get()) {
            connection.setAutoCommit(false);
            try {
                long id = dao.insert(connection, target, DiscussionValidation.normalize(rawMessage));
                if (id <= 0) throw new SQLException("Message identifier was unavailable after insertion.");
                if(attachment!=null)new AttachmentDAO().insert(connection,"MESSAGE",id,attachment);
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
        String attachmentKey=findAttachmentStorageKey(messageId);
        if(dao.delete(messageId)==1){if(attachmentKey!=null)deleteStoredAttachment(attachmentKey);return new OperationResult(true,"Message deleted.");}
        return new OperationResult(false,"NOT_FOUND");
    }

    String findAttachmentStorageKey(long messageId) throws SQLException {
        return new AttachmentDAO().findStorageKey("MESSAGE", messageId);
    }

    void deleteStoredAttachment(String storageKey) {
        new AttachmentStorage().delete(storageKey);
    }
}
