package com.studenthub.dao;

import com.studenthub.model.*;
import com.studenthub.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdminDAO {
    private static final String ACTIVE_ACCOUNT = "email NOT LIKE 'deleted-%@invalid.studenthub'";
    public AdminDashboardStats loadStats() throws SQLException {
        String sql = """
                SELECT
                  (SELECT COUNT(*) FROM users WHERE email NOT LIKE 'deleted-%@invalid.studenthub') total_users,
                  (SELECT COUNT(*) FROM users WHERE role='STUDENT' AND email NOT LIKE 'deleted-%@invalid.studenthub') students,
                  (SELECT COUNT(*) FROM users WHERE role='CR' AND email NOT LIKE 'deleted-%@invalid.studenthub') crs,
                  (SELECT COUNT(*) FROM users WHERE role='ADMIN' AND email NOT LIKE 'deleted-%@invalid.studenthub') admins,
                  (SELECT COUNT(*) FROM posts) announcements,
                  (SELECT COUNT(*) FROM deadlines) deadlines,
                  (SELECT COUNT(*) FROM messages) discussion_messages,
                  (SELECT COUNT(*) FROM comments) comments,
                  (SELECT COUNT(*) FROM reactions) reactions,
                  (SELECT COUNT(*) FROM users WHERE email_verified=TRUE AND email NOT LIKE 'deleted-%@invalid.studenthub') verified_users,
                  (SELECT COUNT(*) FROM users WHERE email_verified=FALSE AND email NOT LIKE 'deleted-%@invalid.studenthub') unverified_users
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            result.next();
            return new AdminDashboardStats(result.getLong("total_users"), result.getLong("students"),
                    result.getLong("crs"), result.getLong("admins"), result.getLong("announcements"),
                    result.getLong("deadlines"), result.getLong("discussion_messages"),
                    result.getLong("comments"), result.getLong("reactions"),
                    result.getLong("verified_users"), result.getLong("unverified_users"));
        }
    }

    public List<AdminUserSummary> findRecentUsers(int limit) throws SQLException {
        String sql = baseUserSelect() + " ORDER BY created_at DESC,user_id DESC LIMIT ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Math.max(1, Math.min(limit, 10)));
            return readUsers(statement);
        }
    }

    public List<AdminUserSummary> findUsers(String search, int limit, int offset) throws SQLException {
        String sql = baseUserSelect() + searchClause(search) + " ORDER BY created_at DESC,user_id DESC LIMIT ? OFFSET ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            int index = bindSearch(statement, search);
            statement.setInt(index++, limit); statement.setInt(index, offset);
            return readUsers(statement);
        }
    }

    public long countUsers(String search) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE " + ACTIVE_ACCOUNT + searchClause(search);
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindSearch(statement, search);
            try (ResultSet result = statement.executeQuery()) { result.next(); return result.getLong(1); }
        }
    }

    public Optional<AdminUserSummary> findUser(long userId) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(baseUserSelect() + " AND user_id=?")) {
            statement.setLong(1, userId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(map(result)) : Optional.empty();
            }
        }
    }

    public List<Long> lockAdminIds(Connection connection) throws SQLException {
        List<Long> ids = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT user_id FROM users WHERE role='ADMIN' ORDER BY user_id FOR UPDATE");
             ResultSet result = statement.executeQuery()) {
            while (result.next()) ids.add(result.getLong(1));
        }
        return ids;
    }

    public Role lockUserRole(Connection connection, long userId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT role FROM users WHERE user_id=? FOR UPDATE")) {
            statement.setLong(1, userId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Role.valueOf(result.getString(1)) : null;
            }
        }
    }

    public int updateRole(Connection connection, long userId, Role role) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("UPDATE users SET role=? WHERE user_id=?")) {
            statement.setString(1, role.name()); statement.setLong(2, userId);
            return statement.executeUpdate();
        }
    }

    private String baseUserSelect() {
        return "SELECT user_id,student_id,full_name,email,role,email_verified,semester,section_name,created_at FROM users WHERE " + ACTIVE_ACCOUNT;
    }
    private String searchClause(String search) {
        return search == null ? "" : " AND (student_id LIKE ? OR full_name LIKE ? OR email LIKE ?)";
    }
    private int bindSearch(PreparedStatement statement, String search) throws SQLException {
        if (search == null) return 1;
        String pattern = "%" + search + "%";
        statement.setString(1, pattern); statement.setString(2, pattern); statement.setString(3, pattern);
        return 4;
    }
    private List<AdminUserSummary> readUsers(PreparedStatement statement) throws SQLException {
        List<AdminUserSummary> users = new ArrayList<>();
        try (ResultSet result = statement.executeQuery()) { while (result.next()) users.add(map(result)); }
        return users;
    }
    private AdminUserSummary map(ResultSet result) throws SQLException {
        int semesterValue = result.getInt("semester"); Integer semester = result.wasNull() ? null : semesterValue;
        Timestamp created = result.getTimestamp("created_at");
        return new AdminUserSummary(result.getLong("user_id"), result.getString("student_id"),
                result.getString("full_name"), result.getString("email"), Role.valueOf(result.getString("role")),
                result.getBoolean("email_verified"), semester, result.getString("section_name"),
                created == null ? null : created.toLocalDateTime());
    }
}
