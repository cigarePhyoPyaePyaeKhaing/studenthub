package com.studenthub.dao;

import com.studenthub.model.ProfileUpdate;
import com.studenthub.model.Role;
import com.studenthub.model.User;
import com.studenthub.model.UserProfile;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class UserDAOAivenSchemaTest {

    @Test
    void findProfileByIdWithCompleteAivenRow() throws Exception {
        UserDAO dao = new UserDAO();
        Map<String, Object> aivenRow = Map.of(
                "user_id", 101L,
                "student_id", "TNT-0101",
                "full_name", "Aung Aung",
                "email", "aungaung@uit.edu",
                "role", "STUDENT",
                "email_verified", true,
                "semester", 3,
                "section_name", "A"
        );

        AtomicReference<String> executedSql = new AtomicReference<>();
        Connection connection = createMockConnection(executedSql, aivenRow);

        Optional<UserProfile> profile = dao.findProfileById(connection, 101L);

        assertTrue(profile.isPresent());
        UserProfile p = profile.get();
        assertEquals(101L, p.getUserId());
        assertEquals("TNT-0101", p.getStudentId());
        assertEquals("Aung Aung", p.getFullName());
        assertEquals("aungaung@uit.edu", p.getEmail());
        assertEquals(Role.STUDENT, p.getRole());
        assertTrue(p.isEmailVerified());
        assertEquals(3, p.getSemester());
        assertEquals("A", p.getSectionName());
        assertNull(p.getUniversityId());
        assertNull(p.getUniversityName());
        assertFalse(p.isUniversityLocked());
        assertFalse(p.isAcademicInfoLocked());
        assertEquals("A", p.getInitial());

        // Verify the SQL uses exact Aiven column names and aliases
        String sql = executedSql.get();
        assertTrue(sql.contains("id AS user_id"));
        assertTrue(sql.contains("tnt_no AS student_id"));
        assertTrue(sql.contains("name AS full_name"));
        assertTrue(sql.contains("is_verified AS email_verified"));
        assertTrue(sql.contains("section AS section_name"));
        assertTrue(sql.contains("FROM users WHERE id = ?"));
    }

    @Test
    void findProfileByIdWithNullOptionalAivenColumns() throws Exception {
        UserDAO dao = new UserDAO();
        Map<String, Object> aivenRow = new HashMap<>();
        aivenRow.put("user_id", 202L);
        aivenRow.put("student_id", null);
        aivenRow.put("full_name", "New Student");
        aivenRow.put("email", "newstudent@uit.edu");
        aivenRow.put("role", "STUDENT");
        aivenRow.put("email_verified", false);
        aivenRow.put("semester", null);
        aivenRow.put("section_name", null);

        AtomicReference<String> executedSql = new AtomicReference<>();
        Connection connection = createMockConnection(executedSql, aivenRow);

        Optional<UserProfile> profile = dao.findProfileById(connection, 202L);

        assertTrue(profile.isPresent());
        UserProfile p = profile.get();
        assertEquals(202L, p.getUserId());
        assertNull(p.getStudentId());
        assertEquals("New Student", p.getFullName());
        assertEquals("newstudent@uit.edu", p.getEmail());
        assertEquals(Role.STUDENT, p.getRole());
        assertFalse(p.isEmailVerified());
        assertNull(p.getSemester());
        assertNull(p.getSectionName());
        assertNull(p.getUniversityId());
        assertNull(p.getUniversityName());
        assertFalse(p.isUniversityLocked());
        assertFalse(p.isAcademicInfoLocked());
        assertEquals("N", p.getInitial());
    }

    @Test
    void findProfileByIdForAdminWithNoAcademicInfo() throws Exception {
        UserDAO dao = new UserDAO();
        Map<String, Object> aivenRow = new HashMap<>();
        aivenRow.put("user_id", 1L);
        aivenRow.put("student_id", "ADMIN-01");
        aivenRow.put("full_name", "Administrator");
        aivenRow.put("email", "admin@uit.edu");
        aivenRow.put("role", "ADMIN");
        aivenRow.put("email_verified", true);
        aivenRow.put("semester", null);
        aivenRow.put("section_name", null);

        AtomicReference<String> executedSql = new AtomicReference<>();
        Connection connection = createMockConnection(executedSql, aivenRow);

        Optional<UserProfile> profile = dao.findProfileById(connection, 1L);

        assertTrue(profile.isPresent());
        UserProfile p = profile.get();
        assertEquals(1L, p.getUserId());
        assertEquals(Role.ADMIN, p.getRole());
        assertNull(p.getSemester());
        assertNull(p.getSectionName());
    }

    @Test
    void updateProfileTargetsAivenColumns() throws Exception {
        UserDAO dao = new UserDAO();
        AtomicReference<String> executedSql = new AtomicReference<>();
        Map<Integer, Object> boundParams = new HashMap<>();

        Connection connection = createMockUpdateConnection(executedSql, boundParams);

        int rows = dao.updateProfile(connection, 50L, new ProfileUpdate("Updated Name", 4, "B"));

        assertEquals(1, rows);
        String sql = executedSql.get();
        assertTrue(sql.contains("UPDATE users SET name=?, semester=?, section=? WHERE id=?"));
        assertEquals("Updated Name", boundParams.get(1));
        assertEquals(4, boundParams.get(2));
        assertEquals("B", boundParams.get(3));
        assertEquals(50L, boundParams.get(4));
    }

    @Test
    void createPendingStudentTargetsAivenColumns() throws Exception {
        UserDAO dao = new UserDAO();
        AtomicReference<String> executedSql = new AtomicReference<>();
        Map<Integer, Object> boundParams = new HashMap<>();

        Connection connection = createMockInsertConnection(executedSql, boundParams, 999L);

        long createdId = dao.createPendingStudent(connection, "TNT-9999", "Kyaw Kyaw", "kyaw@uit.edu", "hash123");

        assertEquals(999L, createdId);
        String sql = executedSql.get();
        assertTrue(sql.contains("INSERT INTO users"));
        assertTrue(sql.contains("tnt_no, name, email, password, role, is_verified"));
        assertEquals("TNT-9999", boundParams.get(1));
        assertEquals("Kyaw Kyaw", boundParams.get(2));
        assertEquals("kyaw@uit.edu", boundParams.get(3));
        assertEquals("hash123", boundParams.get(4));
        assertEquals("STUDENT", boundParams.get(5));
    }

    @Test
    void markEmailVerifiedUpdatesIsVerifiedColumn() throws Exception {
        UserDAO dao = new UserDAO();
        AtomicReference<String> executedSql = new AtomicReference<>();
        Map<Integer, Object> boundParams = new HashMap<>();

        Connection connection = createMockUpdateConnection(executedSql, boundParams);

        dao.markEmailVerified(connection, 77L);

        String sql = executedSql.get();
        assertTrue(sql.contains("UPDATE users SET is_verified = TRUE WHERE id = ?"));
        assertEquals(77L, boundParams.get(1));
    }

    @Test
    void updatePasswordUpdatesPasswordColumn() throws Exception {
        UserDAO dao = new UserDAO();
        AtomicReference<String> executedSql = new AtomicReference<>();
        Map<Integer, Object> boundParams = new HashMap<>();

        Connection connection = createMockUpdateConnection(executedSql, boundParams);

        dao.updatePassword(connection, 88L, "newPasswordHash");

        String sql = executedSql.get();
        assertTrue(sql.contains("UPDATE users SET password = ? WHERE id = ?"));
        assertEquals("newPasswordHash", boundParams.get(1));
        assertEquals(88L, boundParams.get(2));
    }

    private Connection createMockConnection(AtomicReference<String> sqlRef, Map<String, Object> row) {
        ResultSet rs = (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class[]{ResultSet.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "next" -> true;
                    case "getLong" -> {
                        String col = (String) args[0];
                        Object val = row.get(col);
                        yield val instanceof Number n ? n.longValue() : 0L;
                    }
                    case "getString" -> {
                        String col = (String) args[0];
                        yield (String) row.get(col);
                    }
                    case "getInt" -> {
                        String col = (String) args[0];
                        Object val = row.get(col);
                        yield val instanceof Number n ? n.intValue() : 0;
                    }
                    case "getBoolean" -> {
                        String col = (String) args[0];
                        Object val = row.get(col);
                        yield Boolean.TRUE.equals(val);
                    }
                    case "wasNull" -> {
                        // In UserDAO findProfileById, semester wasNull is checked
                        yield row.get("semester") == null;
                    }
                    case "close" -> null;
                    default -> null;
                });

        PreparedStatement ps = (PreparedStatement) Proxy.newProxyInstance(
                PreparedStatement.class.getClassLoader(),
                new Class[]{PreparedStatement.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "executeQuery" -> rs;
                    case "setLong", "setString", "setInt", "setNull", "close" -> null;
                    default -> null;
                });

        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> {
                    if ("prepareStatement".equals(method.getName())) {
                        sqlRef.set((String) args[0]);
                        return ps;
                    }
                    return null;
                });
    }

    private Connection createMockUpdateConnection(AtomicReference<String> sqlRef, Map<Integer, Object> boundParams) {
        PreparedStatement ps = (PreparedStatement) Proxy.newProxyInstance(
                PreparedStatement.class.getClassLoader(),
                new Class[]{PreparedStatement.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "executeUpdate" -> 1;
                    case "setString", "setInt", "setLong" -> {
                        boundParams.put((Integer) args[0], args[1]);
                        yield null;
                    }
                    case "setNull" -> {
                        boundParams.put((Integer) args[0], null);
                        yield null;
                    }
                    case "close" -> null;
                    default -> null;
                });

        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> {
                    if ("prepareStatement".equals(method.getName())) {
                        sqlRef.set((String) args[0]);
                        return ps;
                    }
                    return null;
                });
    }

    private Connection createMockInsertConnection(AtomicReference<String> sqlRef, Map<Integer, Object> boundParams, long generatedKey) {
        ResultSet keysRs = (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class[]{ResultSet.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "next" -> true;
                    case "getLong" -> generatedKey;
                    case "close" -> null;
                    default -> null;
                });

        PreparedStatement ps = (PreparedStatement) Proxy.newProxyInstance(
                PreparedStatement.class.getClassLoader(),
                new Class[]{PreparedStatement.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "executeUpdate" -> 1;
                    case "getGeneratedKeys" -> keysRs;
                    case "setString", "setInt", "setLong" -> {
                        boundParams.put((Integer) args[0], args[1]);
                        yield null;
                    }
                    case "setNull" -> {
                        boundParams.put((Integer) args[0], null);
                        yield null;
                    }
                    case "close" -> null;
                    default -> null;
                });

        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> {
                    if ("prepareStatement".equals(method.getName())) {
                        sqlRef.set((String) args[0]);
                        return ps;
                    }
                    return null;
                });
    }
}
