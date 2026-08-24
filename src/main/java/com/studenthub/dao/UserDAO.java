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
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import com.studenthub.model.UserSearchResult;

public class UserDAO {
    public List<UserSearchResult> searchPublicUsers(long currentUserId,String rawQuery,int limit)throws SQLException{
        String query=rawQuery==null?"":rawQuery.trim().toLowerCase(java.util.Locale.ROOT);if(query.length()<2)return List.of();int safeLimit=Math.max(1,Math.min(limit,20));String like="%"+query.replace("\\","\\\\").replace("%","\\%").replace("_","\\_")+"%";
        String sql="SELECT user_id,student_id,full_name,role,profile_image,last_active_at FROM users WHERE user_id<>? AND email_verified=TRUE AND (LOWER(full_name) LIKE ? ESCAPE '\\\\' OR LOWER(COALESCE(username,'')) LIKE ? ESCAPE '\\\\' OR LOWER(COALESCE(student_id,'')) LIKE ? ESCAPE '\\\\') ORDER BY CASE WHEN LOWER(full_name)=? THEN 0 ELSE 1 END,full_name LIMIT ?";
        List<UserSearchResult> out=new ArrayList<>();try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement(sql)){s.setLong(1,currentUserId);s.setString(2,like);s.setString(3,like);s.setString(4,like);s.setString(5,query);s.setInt(6,safeLimit);try(ResultSet r=s.executeQuery()){while(r.next()){var active=r.getTimestamp("last_active_at");out.add(new UserSearchResult(r.getLong("user_id"),r.getString("student_id"),r.getString("full_name"),r.getString("role"),r.getString("profile_image"),active==null?null:active.toLocalDateTime()));}}}return out;
    }
    public long createPendingStudent(Connection connection, String studentId, String fullName,
                                     String email, String passwordHash) throws SQLException {
        String sql = """
                INSERT INTO users
                    (username, student_id, email, password_hash, full_name, role, email_verified)
                VALUES (?, ?, ?, ?, ?, ?, FALSE)
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

    public Optional<User> findByStudentId(String studentId) throws SQLException {
        String sql = "SELECT user_id, student_id, full_name, email, password_hash, role, email_verified, google_sub "
                + "FROM users WHERE student_id = ? LIMIT 1";
        return findSingleUser(sql, studentId);
    }

    public Optional<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT user_id, student_id, full_name, email, password_hash, role, email_verified, google_sub "
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
        try (Connection connection = DBConnection.getConnection()) {
            return findProfileById(connection, userId);
        }
    }

    public Optional<UserProfile> findProfileById(Connection connection, long userId) throws SQLException {
        String sqlWithJoin = """
                SELECT u.user_id, u.student_id, u.full_name, u.email, u.role, u.email_verified,
                       u.semester, u.section_name, u.profile_image, u.university_id,
                       v.name AS university_name, v.short_name AS university_short_name
                FROM users u
                LEFT JOIN universities v ON v.university_id = u.university_id
                WHERE u.user_id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sqlWithJoin)) {
            statement.setLong(1, userId);
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) return Optional.empty();
                int semester = results.getInt("semester");
                Integer nullableSemester = results.wasNull() ? null : semester;
                String sectionName = results.getString("section_name");
                boolean academicInfoLocked = (nullableSemester != null && sectionName != null && !sectionName.isBlank());
                String roleStr = results.getString("role");
                Role role = roleStr != null ? Role.valueOf(roleStr) : Role.STUDENT;
                long univId = results.getLong("university_id");
                Long nullableUnivId = results.wasNull() ? null : univId;
                String univName = results.getString("university_name");
                String univShort = results.getString("university_short_name");
                boolean univLocked = (nullableUnivId != null && nullableUnivId > 0);

                return Optional.of(new UserProfile(
                        results.getLong("user_id"),
                        results.getString("student_id"),
                        results.getString("full_name"),
                        results.getString("email"),
                        role,
                        results.getBoolean("email_verified"),
                        nullableSemester,
                        sectionName,
                        null,
                        null,
                        null,
                        null,
                        results.getString("profile_image"),
                        nullableUnivId,
                        univName,
                        univShort,
                        univLocked,
                        academicInfoLocked));
            }
        } catch (SQLException ex) {
            String fallbackSql = """
                    SELECT user_id, student_id, full_name, email, role, email_verified, semester, section_name
                    FROM users WHERE user_id = ?
                    """;
            try (PreparedStatement fallbackStmt = connection.prepareStatement(fallbackSql)) {
                fallbackStmt.setLong(1, userId);
                try (ResultSet results = fallbackStmt.executeQuery()) {
                    if (!results.next()) return Optional.empty();
                    int semester = results.getInt("semester");
                    Integer nullableSemester = results.wasNull() ? null : semester;
                    String sectionName = results.getString("section_name");
                    boolean academicInfoLocked = (nullableSemester != null && sectionName != null && !sectionName.isBlank());
                    String roleStr = results.getString("role");
                    Role role = roleStr != null ? Role.valueOf(roleStr) : Role.STUDENT;
                    return Optional.of(new UserProfile(
                            results.getLong("user_id"),
                            results.getString("student_id"),
                            results.getString("full_name"),
                            results.getString("email"),
                            role,
                            results.getBoolean("email_verified"),
                            nullableSemester,
                            sectionName,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            false,
                            academicInfoLocked));
                }
            }
        }
    }

    public int touchLastActive(long userId) throws SQLException {
        String sql = "UPDATE users SET last_active_at = CURRENT_TIMESTAMP "
                + "WHERE user_id = ? AND (last_active_at IS NULL OR last_active_at < CURRENT_TIMESTAMP - INTERVAL 1 MINUTE)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            return statement.executeUpdate();
        }
    }

    public Optional<LocalDateTime> findLastActive(long userId) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT last_active_at FROM users WHERE user_id = ?")) {
            statement.setLong(1, userId);
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) return Optional.empty();
                java.sql.Timestamp value = results.getTimestamp("last_active_at");
                return value == null ? Optional.empty() : Optional.of(value.toLocalDateTime());
            }
        }
    }

    public int updateUniversityIfUnset(long userId, long universityId) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            return updateUniversityIfUnset(connection, userId, universityId);
        }
    }

    public int updateUniversityIfUnset(Connection connection, long userId, long universityId) throws SQLException {
        String sql = "UPDATE users SET university_id = ? WHERE user_id = ? AND (university_id IS NULL OR university_id = 0)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, universityId);
            statement.setLong(2, userId);
            return statement.executeUpdate();
        }
    }

    public int updateProfile(long authenticatedUserId, ProfileUpdate update) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            return updateProfile(connection, authenticatedUserId, update);
        }
    }

    public int updateProfile(Connection connection, long authenticatedUserId, ProfileUpdate update) throws SQLException {
        String sql = "UPDATE users SET full_name=?, semester=?, section_name=? WHERE user_id=?";
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
        String sql = "SELECT role FROM users WHERE user_id = ? AND email_verified = TRUE";
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
             PreparedStatement s = c.prepareStatement("UPDATE users SET full_name=? WHERE user_id=?")) {
            s.setString(1, fullName);
            s.setLong(2, userId);
            return s.executeUpdate();
        }
    }

    public int updateProfileImage(long userId, String filename) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "UPDATE users SET profile_image=? WHERE user_id=?")) {
            if (filename == null) statement.setNull(1, java.sql.Types.VARCHAR);
            else statement.setString(1, filename);
            statement.setLong(2, userId);
            return statement.executeUpdate();
        }
    }

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
