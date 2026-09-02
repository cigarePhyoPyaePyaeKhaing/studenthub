package com.studenthub.dao;

import com.studenthub.model.DiscussionMessage;
import com.studenthub.model.DiscussionScope;
import com.studenthub.util.DBConnection;
import com.studenthub.util.DiscussionTarget;
import com.studenthub.util.AcademicGroupPolicy;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiscussionDAO {
    public record AcademicProfile(String role, Long universityId, Integer semester, String sectionName) {}
    public record MessageRecord(long messageId, long senderId, DiscussionScope scope,
                                Integer semester, String sectionName) {}
    public record RoomOption(long roomId, DiscussionScope scope, Long universityId,
                             Integer semester, String sectionName, String roomName) {
        public long getRoomId() { return roomId; }
        public DiscussionScope getScope() { return scope; }
        public Long getUniversityId() { return universityId; }
        public Integer getSemester() { return semester; }
        public String getSectionName() { return sectionName; }
        public String getRoomName() { return roomName; }
    }

    public AcademicProfile findAcademicProfile(long userId) throws SQLException {
        String sql = "SELECT role, university_id, semester, section_name FROM users WHERE user_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) return new AcademicProfile(null, null, null, null);
                int value = results.getInt("semester");
                Integer semester = results.wasNull() ? null : value;
                long universityValue=results.getLong("university_id"); Long universityId=results.wasNull()?null:universityValue;
                return new AcademicProfile(results.getString("role"), universityId, semester,
                        results.getString("section_name"));
            }
        }
    }

    public List<RoomOption> findModerationRooms() throws SQLException {
        String sql = """
                SELECT MIN(source.room_id) AS room_id, source.room_type,
                       MIN(source.university_id) AS university_id,
                       source.semester, source.section_name,
                       CASE WHEN source.room_type = 'SEMESTER'
                            THEN CONCAT('Semester ', source.semester)
                            ELSE CONCAT('Semester ', source.semester, ' / Section ', source.section_name)
                       END AS room_name
                FROM (
                    SELECT room_id, room_type, university_id, semester,
                           CASE WHEN room_type = 'SECTION' THEN UPPER(TRIM(section_name)) ELSE NULL END AS section_name
                    FROM chat_rooms
                    WHERE room_type IN ('SEMESTER','SECTION')
                      AND semester BETWEEN 1 AND 10
                      AND (room_type = 'SEMESTER' OR (section_name IS NOT NULL AND TRIM(section_name) <> ''))
                    UNION ALL
                    SELECT 0, 'SEMESTER', university_id, semester, NULL
                    FROM users
                    WHERE semester BETWEEN 1 AND 10 AND university_id IS NOT NULL
                    UNION ALL
                    SELECT 0, 'SECTION', university_id, semester, UPPER(TRIM(section_name))
                    FROM users
                    WHERE semester BETWEEN 1 AND 10 AND university_id IS NOT NULL
                      AND section_name IS NOT NULL AND TRIM(section_name) <> ''
                ) source
                GROUP BY source.room_type, source.semester, source.section_name
                ORDER BY FIELD(source.room_type,'SEMESTER','SECTION'),
                         source.semester, source.section_name
                """;
        List<RoomOption> rooms = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            while (results.next()) rooms.add(mapRoomOption(results));
        }
        return rooms;
    }

    public Long findModerationUniversityId() throws SQLException {
        String sql = """
                SELECT university_id
                FROM (
                    SELECT university_id, 1 AS priority FROM universities
                    WHERE status = 'APPROVED'
                    UNION ALL
                    SELECT university_id, 2 AS priority FROM users
                    WHERE university_id IS NOT NULL
                ) candidates
                ORDER BY priority, university_id
                LIMIT 1
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            return results.next() ? results.getLong("university_id") : null;
        }
    }

    public RoomOption findRoom(long roomId) throws SQLException {
        String sql = "SELECT room_id, room_type, university_id, semester, section_name, room_name FROM chat_rooms WHERE room_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, roomId);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? mapRoomOption(results) : null;
            }
        }
    }

    private RoomOption mapRoomOption(ResultSet results) throws SQLException {
        long university = results.getLong("university_id");
        Long universityId = results.wasNull() ? null : university;
        return new RoomOption(results.getLong("room_id"),
                DiscussionScope.valueOf(results.getString("room_type")), universityId,
                nullableInteger(results, "semester"), results.getString("section_name"),
                results.getString("room_name"));
    }

    public List<DiscussionMessage> findRecent(DiscussionTarget target, int limit) throws SQLException {
        return findRecent(target, limit, false);
    }

    public List<DiscussionMessage> findRecentForModeration(DiscussionTarget target, int limit) throws SQLException {
        return findRecent(target, limit, true);
    }

    private List<DiscussionMessage> findRecent(DiscussionTarget target, int limit,
                                               boolean acrossEquivalentRooms) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT m.message_id, m.sender_id, m.message, m.created_at,
                       u.full_name, u.role, u.semester AS author_semester,
                       u.section_name AS author_section, u.profile_image AS author_avatar,
                       a.attachment_id,a.original_filename,a.storage_key,a.mime_type,a.file_size
                FROM messages m
                JOIN chat_rooms r ON r.room_id = m.room_id
                JOIN users u ON u.user_id = m.sender_id
                LEFT JOIN attachments a ON a.message_id=m.message_id
                WHERE r.room_type = ?
                """);
        boolean academicScope = target.scope() == DiscussionScope.SEMESTER
                || target.scope() == DiscussionScope.SECTION || target.scope() == DiscussionScope.CR_SEMESTER;
        if (academicScope && !acrossEquivalentRooms) sql.append(" AND r.university_id = ?");
        if (target.scope() == DiscussionScope.SEMESTER || target.scope() == DiscussionScope.CR_SEMESTER) sql.append(" AND r.semester = ? AND r.section_name IS NULL");
        if (target.scope() == DiscussionScope.SECTION) sql.append(" AND r.semester = ? AND r.section_name = ?");
        if (target.scope() == DiscussionScope.ALL || target.scope() == DiscussionScope.CR_ALL
                || target.scope() == DiscussionScope.CR_ADMIN || target.scope() == DiscussionScope.ALL_STUDENTS_ADMIN) sql.append(" AND r.semester IS NULL AND r.section_name IS NULL");
        sql.append(" ORDER BY m.created_at DESC, m.message_id DESC LIMIT ?");

        List<DiscussionMessage> messages = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            int index = 1;
            statement.setString(index++, target.scope().name());
            if (academicScope) {
                if (!acrossEquivalentRooms) statement.setLong(index++, target.universityId());
                statement.setInt(index++, target.semester());
            }
            if (target.scope() == DiscussionScope.SECTION) statement.setString(index++, target.sectionName());
            statement.setInt(index, Math.max(1, Math.min(limit, 100)));
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    messages.add(new DiscussionMessage(results.getLong("message_id"),
                            results.getLong("sender_id"), results.getString("full_name"),
                            results.getString("role"), nullableInteger(results, "author_semester"),
                            results.getString("author_section"), results.getString("message"),
                            results.getTimestamp("created_at").toLocalDateTime(),
                            results.getString("author_avatar"),mapAttachment(results)));
                }
            }
        }
        Collections.reverse(messages);
        return messages;
    }
    private com.studenthub.model.Attachment mapAttachment(ResultSet r)throws SQLException{long id=r.getLong("attachment_id");if(r.wasNull())return null;return new com.studenthub.model.Attachment(id,null,null,r.getLong("message_id"),r.getString("original_filename"),r.getString("storage_key"),r.getString("mime_type"),r.getLong("file_size"));}

    public long insert(Connection connection, DiscussionTarget target, String message) throws SQLException {
        long roomId = findOrCreateRoom(connection, target);
        String sql = "INSERT INTO messages (room_id, sender_id, message) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, roomId);
            statement.setLong(2, target.authorId());
            statement.setString(3, message);
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
        String lockName = "studenthub:room:" + target.universityId() + ":" + target.scope() + ":" + target.semester() + ":" + target.sectionName();
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
                WHERE room_type = ? AND university_id <=> ? AND semester <=> ? AND section_name <=> ?
                ORDER BY room_id LIMIT 1
                """;
        try (PreparedStatement statement = connection.prepareStatement(select)) {
            setRoomScope(statement, target);
            try (ResultSet results = statement.executeQuery()) { if (results.next()) return results.getLong(1); }
        }
        String insert = "INSERT INTO chat_rooms (room_name, room_type, university_id, semester, section_name) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, roomName(target));
            statement.setString(2, target.scope().name());
            if(target.universityId()==null)statement.setNull(3,Types.BIGINT);else statement.setLong(3,target.universityId());
            if (target.semester() == null) statement.setNull(4, Types.INTEGER); else statement.setInt(4, target.semester());
            if (target.sectionName() == null) statement.setNull(5, Types.VARCHAR); else statement.setString(5, target.sectionName());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
            }
        }
        throw new SQLException("Discussion room was created but its identifier was unavailable.");
    }

    private void setRoomScope(PreparedStatement statement, DiscussionTarget target) throws SQLException {
        statement.setString(1, target.scope().name());
        if(target.universityId()==null)statement.setNull(2,Types.BIGINT);else statement.setLong(2,target.universityId());
        if (target.semester() == null) statement.setNull(3, Types.INTEGER); else statement.setInt(3, target.semester());
        if (target.sectionName() == null) statement.setNull(4, Types.VARCHAR); else statement.setString(4, target.sectionName());
    }

    private String roomName(DiscussionTarget target) {
        return switch (target.scope()) {
            case ALL -> "StudentHub All Chat";
            case SEMESTER -> "Semester " + target.semester();
            case SECTION -> "Semester " + target.semester() + " / "
                    + AcademicGroupPolicy.groupLabel(target.semester()) + " " + target.sectionName();
            case CR_SEMESTER -> "CR Semester " + target.semester();
            case CR_ALL -> "CR All Chat";
            case CR_ADMIN -> "CR - Admin Chat";
            case ALL_STUDENTS_ADMIN -> "All Students - Admin Chat";
        };
    }

    private Integer nullableInteger(ResultSet results, String column) throws SQLException {
        int value = results.getInt(column);
        return results.wasNull() ? null : value;
    }
}
