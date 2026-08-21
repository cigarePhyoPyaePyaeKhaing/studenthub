package com.studenthub.dao;

import com.studenthub.model.ProfileUpdate;
import com.studenthub.model.Role;
import com.studenthub.model.UserProfile;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class UserDAOAivenSchemaTest {

    @Test
    void findProfileByIdQueriesAivenColumns() throws Exception {
        UserDAO dao = new UserDAO();
        AtomicReference<String> executedSql = new AtomicReference<>();
        Map<String, Object> row = new HashMap<>();
        row.put("id", 42L);
        row.put("tnt_no", "TNT-0042");
        row.put("name", "Mg Mg");
        row.put("email", "mgmg@uit.edu");
        row.put("role", "STUDENT");
        row.put("is_verified", true);
        row.put("semester", 4);
        row.put("section", "A");
        row.put("major", "CS");
        row.put("phone", "+95900000000");
        row.put("address", "Yangon");
        row.put("bio", "Student bio");
        row.put("avatar_url", "https://img.example.com/avatar.jpg");

        Connection connection = createMockConnection(executedSql, row, true);

        Optional<UserProfile> profile = dao.findProfileById(connection, 42L);

        assertTrue(profile.isPresent());
        UserProfile p = profile.get();
        assertEquals(42L, p.getUserId());
        assertEquals("TNT-0042", p.getStudentId());
        assertEquals("Mg Mg", p.getFullName());
        assertEquals("mgmg@uit.edu", p.getEmail());
        assertEquals(Role.STUDENT, p.getRole());
        assertTrue(p.isEmailVerified());
        assertEquals(4, p.getSemester());
        assertEquals("A", p.getSectionName());
        assertEquals("CS", p.getMajor());
        assertEquals("+95900000000", p.getPhone());
        assertEquals("Yangon", p.getAddress());
        assertEquals("Student bio", p.getBio());
        assertEquals("https://img.example.com/avatar.jpg", p.getAvatarUrl());

        String sql = executedSql.get();
        assertNotNull(sql);
        assertTrue(sql.contains("SELECT id, tnt_no, name, email, role, is_verified"));
        assertTrue(sql.contains("FROM users WHERE id = ?"));
        assertFalse(sql.contains("universities"));
        assertFalse(sql.contains("user_id"));
        assertFalse(sql.contains("full_name"));
    }

    @Test
    void findProfileByIdHandlesNullOptionalValuesSafely() throws Exception {
        UserDAO dao = new UserDAO();
        AtomicReference<String> executedSql = new AtomicReference<>();
        Map<String, Object> row = new HashMap<>();
        row.put("id", 99L);
        row.put("tnt_no", null);
        row.put("name", "New Student");
        row.put("email", "new@uit.edu");
        row.put("role", "STUDENT");
        row.put("is_verified", false);
        row.put("semester", null);
        row.put("section", null);
        row.put("major", null);
        row.put("phone", null);
        row.put("address", null);
        row.put("bio", null);
        row.put("avatar_url", null);

        Connection connection = createMockConnection(executedSql, row, true);

        Optional<UserProfile> profile = dao.findProfileById(connection, 99L);

        assertTrue(profile.isPresent());
        UserProfile p = profile.get();
        assertEquals(99L, p.getUserId());
        assertNull(p.getStudentId());
        assertEquals("New Student", p.getFullName());
        assertEquals("new@uit.edu", p.getEmail());
        assertFalse(p.isEmailVerified());
        assertNull(p.getSemester());
        assertNull(p.getSectionName());
        assertNull(p.getMajor());
        assertNull(p.getPhone());
        assertNull(p.getAddress());
        assertNull(p.getBio());
        assertNull(p.getAvatarUrl());
        assertEquals("N", p.getInitial());
    }

    @Test
    void findProfileByIdReturnsEmptyWhenNotFound() throws Exception {
        UserDAO dao = new UserDAO();
        AtomicReference<String> executedSql = new AtomicReference<>();
        Map<String, Object> row = new HashMap<>();

        Connection connection = createMockConnection(executedSql, row, false);

        Optional<UserProfile> profile = dao.findProfileById(connection, 999L);
        assertTrue(profile.isEmpty());
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
    void updateProfileHandlesNullSemesterAndSection() throws Exception {
        UserDAO dao = new UserDAO();
        AtomicReference<String> executedSql = new AtomicReference<>();
        Map<Integer, Object> boundParams = new HashMap<>();

        Connection connection = createMockUpdateConnection(executedSql, boundParams);

        int rows = dao.updateProfile(connection, 50L, new ProfileUpdate("Name Only", null, null));

        assertEquals(1, rows);
        String sql = executedSql.get();
        assertTrue(sql.contains("UPDATE users SET name=?, semester=?, section=? WHERE id=?"));
        assertEquals("Name Only", boundParams.get(1));
        assertNull(boundParams.get(2));
        assertNull(boundParams.get(3));
        assertEquals(50L, boundParams.get(4));
    }

    private Connection createMockConnection(AtomicReference<String> sqlRef, Map<String, Object> row, boolean hasRow) {
        ResultSet rs = (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class[]{ResultSet.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "next" -> hasRow;
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
}