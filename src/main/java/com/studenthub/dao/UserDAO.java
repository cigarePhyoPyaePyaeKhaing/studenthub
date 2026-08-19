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
                    (username, student_id, email, password_hash, full_name, role, email_verified,
                     university_id, university_locked, semester, section_name, academic_info_locked)
                VALUES (?, ?, ?, ?, ?, ?, FALSE,
                        (SELECT university_id FROM universities WHERE short_name='UIT' AND status='APPROVED' LIMIT 1),
                        TRUE, NULL, NULL, FALSE)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, studentId);
            statement.setString(2, studentId);
            statement.setString(3, email);
            statement.setString(4, passwordHash);
            statement.setString(5, fullName);
            statement.setString(6, RegistrationPolicy.initialRole().name());
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
                SELECT user_id, student_id, full_name, email, password_hash, role, email_verified, google_sub
                FROM users WHERE student_id = ? OR email = ? LIMIT 1
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

    public Optional<User> findById(long userId) throws SQLException {
        String sql = """
                SELECT user_id, student_id, full_name, email, password_hash, role, email_verified, google_sub
                FROM users WHERE user_id = ?
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
        String sql = """
                SELECT u.user_id,u.student_id,u.full_name,u.email,u.role,u.email_verified,
                       u.semester,u.section_name,u.university_id,u.university_locked,u.academic_info_locked,
                       v.name university_name,v.short_name university_short_name
                FROM users u LEFT JOIN universities v ON v.university_id=u.university_id WHERE u.user_id = ?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) return Optional.empty();
                int semester = results.getInt("semester");
                Integer nullableSemester = results.wasNull() ? null : semester;
                return Optional.of(new UserProfile(results.getLong("user_id"),
                        results.getString("student_id"), results.getString("full_name"),
                        results.getString("email"), Role.valueOf(results.getString("role")),
                        results.getBoolean("email_verified"), nullableSemester,
                        results.getString("section_name"),nullableLong(results,"university_id"),
                        results.getString("university_name"),results.getString("university_short_name"),
                        results.getBoolean("university_locked"),results.getBoolean("academic_info_locked")));
            }
        }
    }

    public int updateProfile(long authenticatedUserId, ProfileUpdate update) throws SQLException {
        String sql = "UPDATE users SET full_name=?,semester=?,section_name=?,"
                + "academic_info_locked=(? IS NOT NULL AND ? IS NOT NULL) "
                + "WHERE user_id=? AND academic_info_locked=FALSE";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, update.fullName());
            if (update.semester() == null) statement.setNull(2, java.sql.Types.INTEGER);
            else statement.setInt(2, update.semester());
            if (update.sectionName() == null) statement.setNull(3, java.sql.Types.VARCHAR);
            else statement.setString(3, update.sectionName());
            if(update.semester()==null)statement.setNull(4,java.sql.Types.INTEGER);else statement.setInt(4,update.semester());
            if(update.sectionName()==null)statement.setNull(5,java.sql.Types.VARCHAR);else statement.setString(5,update.sectionName());
            statement.setLong(6, authenticatedUserId);
            return statement.executeUpdate();
        }
    }

    public Optional<Role> findVerifiedRoleById(long userId) throws SQLException {
        String sql = "SELECT role FROM users WHERE user_id = ? AND email_verified = TRUE";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? Optional.of(Role.valueOf(results.getString("role"))) : Optional.empty();
            }
        }
    }
    public int updateFullName(long userId,String fullName)throws SQLException{try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement("UPDATE users SET full_name=? WHERE user_id=?")){s.setString(1,fullName);s.setLong(2,userId);return s.executeUpdate();}}
    private Long nullableLong(ResultSet r,String column)throws SQLException{long value=r.getLong(column);return r.wasNull()?null:value;}

    public void markEmailVerified(Connection connection, long userId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE users SET email_verified = TRUE WHERE user_id = ?")) {
            statement.setLong(1, userId);
            statement.executeUpdate();
        }
    }

    public void updatePassword(Connection connection, long userId, String passwordHash) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE users SET password_hash = ? WHERE user_id = ?")) {
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
