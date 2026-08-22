package com.studenthub.dao;

import com.studenthub.model.DiscussionMessage;
import com.studenthub.model.DiscussionScope;
import com.studenthub.util.DBConnection;
import com.studenthub.util.DiscussionTarget;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiscussionDAO {
    public record AcademicProfile(String role, Integer semester, String sectionName) {}
    public record MessageRecord(long messageId, long senderId, DiscussionScope scope,
                                Integer semester, String sectionName) {}

    public AcademicProfile findAcademicProfile(long userId) throws SQLException {
        String sql = "SELECT role, semester, section_name FROM users WHERE user_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) return new AcademicProfile(null, null, null);
                int value = results.getInt("semester");
                Integer semester = results.wasNull() ? null : value;
                return new AcademicProfile(results.getString("role"), semester,
                        results.getString("section_name"));
            }
        }
    }

    public List<DiscussionMessage> findRecent(DiscussionTarget target, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT m.message_id, m.sender_id, m.message, m.created_at,
                       m.attachment_name, m.attachment_stored_name, m.attachment_mime_type, m.attachment_size,
                       u.full_name, u.role, u.semester AS author_semester,
                       u.section_name AS author_section
                FROM messages m
                JOIN chat_rooms r ON r.room_id = m.room_id
                JOIN users u ON u.user_id = m.sender_id
                WHERE r.room_type = ?
                """);
        if (target.scope() == DiscussionScope.SEMESTER || target.scope() == DiscussionScope.CR_SEMESTER) sql.append(" AND r.semester = ? AND r.section_name IS NULL");
        if (target.scope() == DiscussionScope.SECTION) sql.append(" AND r.semester = ? AND r.section_name = ?");
        if (target.scope() == DiscussionScope.ALL || target.scope() == DiscussionScope.CR_ALL) sql.append(" AND r.semester IS NULL AND r.section_name IS NULL");
        sql.append(" ORDER BY m.created_at DESC, m.message_id DESC LIMIT ?");

        List<DiscussionMessage> messages = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int index = 1;
            statement.setString(index++, target.scope().name());
            if (target.scope() != DiscussionScope.ALL && target.scope() != DiscussionScope.CR_ALL) statement.setInt(index++, target.semester());
            if (target.scope() == DiscussionScope.SECTION) statement.setString(index++, target.sectionName());
            statement.setInt(index, Math.max(1, Math.min(limit, 100)));
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    messages.add(new DiscussionMessage(results.getLong("message_id"),
                            results.getLong("sender_id"), results.getString("full_name"),
                            results.getString("role"), nullableInteger(results, "author_semester"),
                            results.getString("author_section"), results.getString("message"),
                            results.getTimestamp("created_at").toLocalDateTime(),
                            mapAttachment(results)));
                }
            }
        }
        Collections.reverse(messages);
        return messages;
    }

    public long insert(Connection connection, DiscussionTarget target, String message) throws SQLException {
        return insert(connection, target, message, null);
    }

    public long insert(Connection connection, DiscussionTarget target, String message,
                       com.studenthub.model.Attachment attachment) throws SQLException {
        long roomId = findOrCreateRoom(connection, target);
        String sql = "INSERT INTO messages (room_id, sender_id, message, attachment_name, attachment_stored_name, attachment_mime_type, attachment_size) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, roomId);
            statement.setLong(2, target.authorId());
            statement.setString(3, message);
            setAttachment(statement, 4, attachment);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) { return keys.next() ? keys.getLong(1) : 0; }
        }
    }

    public MessageRecord findMessage(long messageId) throws SQLException {
        String sql = """
                SELECT m.message_id, m.sender_id, r.room_type, r.semester, r.section_name
                FROM messages m JOIN chat_rooms r ON r.room_id = m.room_id
                WHERE m.message_id = ?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, messageId);
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) return null;
                int semester = results.getInt("semester");
                boolean semesterWasNull = results.wasNull();
                return new MessageRecord(messageId, results.getLong("sender_id"),
                        DiscussionScope.valueOf(results.getString("room_type")),
                        semesterWasNull ? null : semester, results.getString("section_name"));
            }
        }
    }

    public int delete(long messageId) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM messages WHERE message_id = ?")) {
            statement.setLong(1, messageId);
            return statement.executeUpdate();
        }
    }

    private long findOrCreateRoom(Connection connection, DiscussionTarget target) throws SQLException {
        String lockName = "studenthub:room:" + target.scope() + ":" + target.semester() + ":" + target.sectionName();
        try (PreparedStatement lock = connection.prepareStatement("SELECT GET_LOCK(?, 5)")) {
            lock.setString(1, lockName);
            try (ResultSet result = lock.executeQuery()) {
                if (!result.next() || result.getInt(1) != 1) throw new SQLException("Could not acquire discussion room lock.");
            }
        }
        // Keep the connection-scoped lock through the caller's commit. DBConnection returns a
        // physical DriverManager connection, so closing it immediately after commit releases the lock.
        return findOrCreateRoomWhileLocked(connection, target);
    }

    private long findOrCreateRoomWhileLocked(Connection connection, DiscussionTarget target) throws SQLException {
        String select = """
                SELECT room_id FROM chat_rooms
                WHERE room_type = ? AND semester <=> ? AND section_name <=> ?
                ORDER BY room_id LIMIT 1
                """;
        try (PreparedStatement statement = connection.prepareStatement(select)) {
            setRoomScope(statement, target);
            try (ResultSet results = statement.executeQuery()) { if (results.next()) return results.getLong(1); }
        }
        String insert = "INSERT INTO chat_rooms (room_name, room_type, semester, section_name) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, roomName(target));
            statement.setString(2, target.scope().name());
            if (target.semester() == null) statement.setNull(3, Types.INTEGER); else statement.setInt(3, target.semester());
            if (target.sectionName() == null) statement.setNull(4, Types.VARCHAR); else statement.setString(4, target.sectionName());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
        }
        throw new SQLException("Discussion room was created but its identifier was unavailable.");
    }

    private void setRoomScope(PreparedStatement statement, DiscussionTarget target) throws SQLException {
        statement.setString(1, target.scope().name());
        if (target.semester() == null) statement.setNull(2, Types.INTEGER); else statement.setInt(2, target.semester());
        if (target.sectionName() == null) statement.setNull(3, Types.VARCHAR); else statement.setString(3, target.sectionName());
    }

    private String roomName(DiscussionTarget target) {
        return switch (target.scope()) {
            case ALL -> "StudentHub All Chat";
            case SEMESTER -> "Semester " + target.semester();
            case SECTION -> "Semester " + target.semester() + " / Section " + target.sectionName();
            case CR_SEMESTER -> "CR Semester " + target.semester();
            case CR_ALL -> "CR All Chat";
        };
    }

    private Integer nullableInteger(ResultSet results, String column) throws SQLException {
        int value = results.getInt(column);
        return results.wasNull() ? null : value;
    }

    private com.studenthub.model.Attachment mapAttachment(ResultSet results) throws SQLException {
        String stored = results.getString("attachment_stored_name");
        return stored == null ? null : new com.studenthub.model.Attachment(results.getString("attachment_name"),
                stored, results.getString("attachment_mime_type"), results.getLong("attachment_size"));
    }

    private void setAttachment(PreparedStatement statement, int index, com.studenthub.model.Attachment attachment) throws SQLException {
        if (attachment == null) {
            statement.setNull(index, Types.VARCHAR); statement.setNull(index+1, Types.VARCHAR);
            statement.setNull(index+2, Types.VARCHAR); statement.setNull(index+3, Types.BIGINT);
        } else {
            statement.setString(index, attachment.originalName()); statement.setString(index+1, attachment.storedName());
            statement.setString(index+2, attachment.mimeType()); statement.setLong(index+3, attachment.size());
        }
    }
}
