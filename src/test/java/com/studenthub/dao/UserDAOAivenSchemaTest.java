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
    void findProfileByIdQueriesProductionColumns() throws Exception {
        UserDAO dao = new UserDAO();
        AtomicReference<String> executedSql = new AtomicReference<>();
        Map<String, Object> row = new HashMap<>();
        row.put("user_id", 42L);
        row.put("student_id", "TNT-0042");
        row.put("full_name", "Mg Mg");
        row.put("email", "mgmg@uit.edu");
        row.put("role", "STUDENT");
        row.put("email_verified", true);
        row.put("semester", 4);
        row.put("section_name", "A");

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

        String sql = executedSql.get();
        assertNotNull(sql);
        assertTrue(sql.contains("SELECT user_id, student_id, full_name, email, role, email_verified, semester, section_name"));
        assertTrue(sql.contains("FROM users WHERE user_id = ?"));
        assertFalse(sql.contains("universities"));
    }

    @Test
    void findProfileByIdHandlesNullOptionalValuesSafely() throws Exception {
        UserDAO dao = new UserDAO();
        AtomicReference<String> executedSql = new AtomicReference<>();
        Map<String, Object> row = new HashMap<>();
        row.put("user_id", 99L);
        row.put("student_id", null);
        row.put("full_name", "New Student");
        row.put("email", "new@uit.edu");
        row.put("role", "STUDENT");
        row.put("email_verified", false);
        row.put("semester", null);
        row.put("section_name", null);

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
    void updateProfileTargetsProductionColumns() throws Exception {
        UserDAO dao = new UserDAO();
        AtomicReference<String> executedSql = new AtomicReference<>();
        Map<Integer, Object> boundParams = new HashMap<>();

        Connection connection = createMockUpdateConnection(executedSql, boundParams);

        int rows = dao.updateProfile(connection, 50L, new ProfileUpdate("Updated Name", 4, "B"));

        assertEquals(1, rows);
        String sql = executedSql.get();
        assertTrue(sql.contains("UPDATE users SET full_name=?, semester=?, section_name=? WHERE user_id=?"));
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
        assertTrue(sql.contains("UPDATE users SET full_name=?, semester=?, section_name=? WHERE user_id=?"));
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