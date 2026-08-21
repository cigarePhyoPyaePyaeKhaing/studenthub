package com.studenthub.dao;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class AcademicChangeDAOTest {

    @Test
    void reviewApproveUpdatesUserAndMarksRequestApproved() throws Exception {
        AcademicChangeDAO dao = new AcademicChangeDAO();
        AtomicReference<String> userUpdateSql = new AtomicReference<>();
        AtomicReference<String> requestUpdateSql = new AtomicReference<>();
        Map<Integer, Object> userParams = new HashMap<>();
        Map<Integer, Object> requestParams = new HashMap<>();
        AtomicBoolean committed = new AtomicBoolean(false);

        Connection connection = createMockReviewConnection(
                101L, 4, "B",
                userUpdateSql, userParams,
                requestUpdateSql, requestParams,
                committed);

        boolean result = dao.review(connection, 1L, 999L, true, "Approved by admin");

        assertTrue(result);
        assertTrue(committed.get());

        assertNotNull(userUpdateSql.get());
        assertTrue(userUpdateSql.get().contains("UPDATE users SET semester = ?, section_name = ? WHERE user_id = ?"));
        assertFalse(userUpdateSql.get().contains("academic_info_locked"));
        assertEquals(4, userParams.get(1));
        assertEquals("B", userParams.get(2));
        assertEquals(101L, userParams.get(3));

        assertNotNull(requestUpdateSql.get());
        assertTrue(requestUpdateSql.get().contains("UPDATE academic_change_requests SET status = ?, reviewed_by = ?, reviewed_at = UTC_TIMESTAMP(), admin_note = ? WHERE request_id = ?"));
        assertEquals("APPROVED", requestParams.get(1));
        assertEquals(999L, requestParams.get(2));
        assertEquals("Approved by admin", requestParams.get(3));
        assertEquals(1L, requestParams.get(4));
    }

    @Test
    void reviewRejectDoesNotUpdateUserAndMarksRequestRejected() throws Exception {
        AcademicChangeDAO dao = new AcademicChangeDAO();
        AtomicReference<String> userUpdateSql = new AtomicReference<>();
        AtomicReference<String> requestUpdateSql = new AtomicReference<>();
        Map<Integer, Object> userParams = new HashMap<>();
        Map<Integer, Object> requestParams = new HashMap<>();
        AtomicBoolean committed = new AtomicBoolean(false);

        Connection connection = createMockReviewConnection(
                101L, 4, "B",
                userUpdateSql, userParams,
                requestUpdateSql, requestParams,
                committed);

        boolean result = dao.review(connection, 1L, 999L, false, "Rejected: invalid section");

        assertTrue(result);
        assertTrue(committed.get());
        assertNull(userUpdateSql.get()); // User table must NOT be updated on reject!

        assertNotNull(requestUpdateSql.get());
        assertEquals("REJECTED", requestParams.get(1));
        assertEquals(999L, requestParams.get(2));
        assertEquals("Rejected: invalid section", requestParams.get(3));
        assertEquals(1L, requestParams.get(4));
    }

    @Test
    void reviewReturnsFalseWhenRequestNotFoundOrNotPending() throws Exception {
        AcademicChangeDAO dao = new AcademicChangeDAO();
        AtomicBoolean committed = new AtomicBoolean(false);
        AtomicBoolean rolledBack = new AtomicBoolean(false);

        Connection connection = createMockEmptyReviewConnection(committed, rolledBack);

        boolean result = dao.review(connection, 999L, 1L, true, "Note");

        assertFalse(result);
        assertFalse(committed.get());
        assertTrue(rolledBack.get());
    }

    private Connection createMockReviewConnection(
            long userId, int requestedSemester, String requestedSection,
            AtomicReference<String> userUpdateSql, Map<Integer, Object> userParams,
            AtomicReference<String> requestUpdateSql, Map<Integer, Object> requestParams,
            AtomicBoolean committed) {

        ResultSet selectRs = (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class[]{ResultSet.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "next" -> true;
                    case "getLong" -> userId;
                    case "getInt" -> requestedSemester;
                    case "getString" -> requestedSection;
                    case "close" -> null;
                    default -> null;
                });

        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "setAutoCommit" -> null;
                    case "commit" -> {
                        committed.set(true);
                        yield null;
                    }
                    case "rollback" -> null;
                    case "prepareStatement" -> {
                        String sql = (String) args[0];
                        if (sql.contains("SELECT user_id, requested_semester")) {
                            yield createMockPreparedStatement(selectRs, null, null);
                        } else if (sql.contains("UPDATE users SET semester")) {
                            userUpdateSql.set(sql);
                            yield createMockPreparedStatement(null, userParams, null);
                        } else if (sql.contains("UPDATE academic_change_requests SET status")) {
                            requestUpdateSql.set(sql);
                            yield createMockPreparedStatement(null, requestParams, null);
                        }
                        yield null;
                    }
                    default -> null;
                });
    }

    private Connection createMockEmptyReviewConnection(AtomicBoolean committed, AtomicBoolean rolledBack) {
        ResultSet emptyRs = (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class[]{ResultSet.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "next" -> false;
                    case "close" -> null;
                    default -> null;
                });

        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "setAutoCommit" -> null;
                    case "commit" -> {
                        committed.set(true);
                        yield null;
                    }
                    case "rollback" -> {
                        rolledBack.set(true);
                        yield null;
                    }
                    case "prepareStatement" -> createMockPreparedStatement(emptyRs, null, null);
                    default -> null;
                });
    }

    private PreparedStatement createMockPreparedStatement(ResultSet rs, Map<Integer, Object> boundParams, AtomicReference<String> capturedSql) {
        return (PreparedStatement) Proxy.newProxyInstance(
                PreparedStatement.class.getClassLoader(),
                new Class[]{PreparedStatement.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "executeQuery" -> rs;
                    case "executeUpdate" -> 1;
                    case "setInt", "setLong", "setString" -> {
                        if (boundParams != null) {
                            boundParams.put((Integer) args[0], args[1]);
                        }
                        yield null;
                    }
                    case "setNull", "close" -> null;
                    default -> null;
                });
    }
}