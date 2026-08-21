package com.studenthub.dao;

import com.studenthub.model.Role;
import com.studenthub.model.User;
import com.studenthub.model.UserProfile;
import com.studenthub.model.ProfileUpdate;
import com.studenthub.util.DBConnection;
import com.studenthub.util.RegistrationPolicy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class UserDAO {
    public long createPendingStudent(Connection connection, String studentId, String fullName,
                                     String email, String passwordHash) throws SQLException {
        String sql = """
                INSERT INTO users
                    (tnt_no, name, email, password, role, is_verified)
                VALUES (?, ?, ?, ?, ?, FALSE)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, studentId);
            statement.setString(2, fullName);
            statement.setString(3, email);
            statement.setString(4, passwordHash);
            statement.setString(5, RegistrationPolicy.initialRole().name());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        try (PreparedStatement statement = connection.prepareStatement("SELECT LAST_INSERT_ID()");
             ResultSet result = statement.executeQuery()) {
            if (result.next() && result.getLong(1) > 0) {
                return result.getLong(1);
            }
        }
        throw new SQLException("Account insert completed but the generated user identifier was unavailable.");
    }

    public Optional<User> findByLogin(String normalizedLogin) throws SQLException {
        String sql = """
                SELECT id AS user_id, tnt_no AS student_id, name AS full_name, email,
                       password AS password_hash, role, is_verified AS email_verified, provider_id AS google_sub
                FROM users WHERE tnt_no = ? OR email = ? LIMIT 1
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, normalizedLogin);
            statement.setString(2, normalizedLogin);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? Optional.of(map(results)) : Optional.empty();
            }
        }
    }

    public Optional<User> findByStudentId(String studentId) throws SQLException {
        String sql = "SELECT id AS user_id, tnt_no AS student_id, name AS full_name, email, "
                + "password AS password_hash, role, is_verified AS email_verified, provider_id AS google_sub "
                + "FROM users WHERE tnt_no = ? LIMIT 1";
        return findSingleUser(sql, studentId);
    }

    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT id AS user_id, tnt_no AS student_id, name AS full_name, email, "
                + "password AS password_hash, role, is_verified AS email_verified, provider_id AS google_sub "
                + "FROM users WHERE email = ? LIMIT 1";
        return findSingleUser(sql, email);
    }

    private Optional<User> findSingleUser(String sql, String value) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? Optional.of(map(results)) : Optional.empty();
            }
        }
    }

    public Optional<User> findById(long userId) throws SQLException {
        String sql = """
                SELECT id AS user_id, tnt_no AS student_id, name AS full_name, email,
                       password AS password_hash, role, is_verified AS email_verified, provider_id AS google_sub
                FROM users WHERE id = ?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? Optional.of(map(results)) : Optional.empty();
            }
        }
    }

    public Optional<UserProfile> findProfileById(long userId) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            return findProfileById(connection, userId);
        }
    }

    public Optional<UserProfile> findProfileById(Connection connection, long userId) throws SQLException {
        String sql = """
                SELECT id AS user_id, tnt_no AS student_id, name AS full_name, email,
                       role, is_verified AS email_verified, semester, section AS section_name
                FROM users WHERE id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) return Optional.empty();
                int semester = results.getInt("semester");
                Integer nullableSemester = results.wasNull() ? null : semester;
                return Optional.of(new UserProfile(
                        results.getLong("user_id"),
                        results.getString("student_id"),
                        results.getString("full_name"),
                        results.getString("email"),
                        Role.valueOf(results.getString("role")),
                        results.getBoolean("email_verified"),
                        nullableSemester,
                        results.getString("section_name"),
                        null, null, null, false, false));
            }
        }
    }

    public int updateProfile(long authenticatedUserId, ProfileUpdate update) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            return updateProfile(connection, authenticatedUserId, update);
        }
    }

    public int updateProfile(Connection connection, long authenticatedUserId, ProfileUpdate update) throws SQLException {
        String sql = "UPDATE users SET name=?, semester=?, section=? WHERE id=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, update.fullName());
            if (update.semester() == null) statement.setNull(2, java.sql.Types.INTEGER);
            else statement.setInt(2, update.semester());
            if (update.sectionName() == null) statement.setNull(3, java.sql.Types.VARCHAR);
            else statement.setString(3, update.sectionName());
            statement.setLong(4, authenticatedUserId);
            return statement.executeUpdate();
        }
    }

    public Optional<Role> findVerifiedRoleById(long userId) throws SQLException {
        String sql = "SELECT role FROM users WHERE id = ? AND is_verified = TRUE";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? Optional.of(Role.valueOf(results.getString("role"))) : Optional.empty();
            }
        }
    }

    public int updateFullName(long userId, String fullName) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement s = c.prepareStatement("UPDATE users SET name=? WHERE id=?")) {
            s.setString(1, fullName);
            s.setLong(2, userId);
            return s.executeUpdate();
        }
    }

    public void markEmailVerified(Connection connection, long userId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE users SET is_verified = TRUE WHERE id = ?")) {
            statement.setLong(1, userId);
            statement.executeUpdate();
        }
    }

    public void updatePassword(Connection connection, long userId, String passwordHash) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE users SET password = ? WHERE id = ?")) {
            statement.setString(1, passwordHash);
            statement.setLong(2, userId);
            statement.executeUpdate();
        }
    }

    private User map(ResultSet results) throws SQLException {
        return new User(results.getLong("user_id"), results.getString("student_id"),
                results.getString("full_name"), results.getString("email"),
                results.getString("password_hash"), Role.valueOf(results.getString("role")),
                results.getBoolean("email_verified"), results.getString("google_sub"));
    }
}
