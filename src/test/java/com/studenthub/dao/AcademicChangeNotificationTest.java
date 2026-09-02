package com.studenthub.dao;

import com.studenthub.model.Role;
import com.studenthub.model.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class AcademicChangeNotificationTest {

    private List<Map<String, Object>> createdNotifications;

    @BeforeEach
    void setUp() {
        createdNotifications = new ArrayList<>();
    }

    @Test
    void studentSubmittingAcademicChangeCreatesAdminNotification() throws Exception {
        NotificationDAO mockNotificationDAO = new NotificationDAO() {
            @Override
            public void createForAdminRole(Connection connection, long actorId, String type, String title, String message, String linkUrl) {
                Map<String, Object> notif = new HashMap<>();
                notif.put("actorId", actorId);
                notif.put("type", type);
                notif.put("title", title);
                notif.put("message", message);
                notif.put("linkUrl", linkUrl);
                createdNotifications.add(notif);
            }
        };

        AcademicChangeDAO dao = new AcademicChangeDAO(mockNotificationDAO);

        AtomicReference<String> insertSql = new AtomicReference<>();
        Connection mockConn = createMockCreateConnection(insertSql, "Shin Thant Hnin", "TNT-2395", 4, "B");

        dao.create(mockConn, 55L, 4, "A", "Change section to A");

        assertNotNull(insertSql.get());
        assertEquals(1, createdNotifications.size());
        Map<String, Object> adminNotif = createdNotifications.get(0);
        assertEquals(55L, adminNotif.get("actorId"));
        assertEquals("ACADEMIC_CHANGE_REQUEST", adminNotif.get("type"));
        assertEquals("Academic Change Request", adminNotif.get("title"));
        assertEquals("/admin/academic-changes?status=PENDING", adminNotif.get("linkUrl"));
        String msg = (String) adminNotif.get("message");
        assertTrue(msg.contains("Shin Thant Hnin (TNT-2395)"));
        assertTrue(msg.contains("Semester 4 / Section B"));
        assertTrue(msg.contains("Semester 4 / Section A"));
    }

    @Test
    void studentSubmittingDuplicatePendingRequestFailsWithoutNotification() throws Exception {
        NotificationDAO mockNotificationDAO = new NotificationDAO() {
            @Override
            public void createForAdminRole(Connection connection, long actorId, String type, String title, String message, String linkUrl) {
                createdNotifications.add(Map.of("type", type));
            }
        };

        AcademicChangeDAO dao = new AcademicChangeDAO(mockNotificationDAO) {
            @Override
            public boolean hasPending(Connection c, long userId) {
                return true; // already pending
            }
        };

        Connection mockConn = (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> null);

        assertThrows(IllegalStateException.class, () -> dao.create(mockConn, 55L, 4, "A", "Reason here"));
        assertEquals(0, createdNotifications.size());
    }

    @Test
    void adminApprovalCreatesStudentApprovalNotification() throws Exception {
        NotificationDAO mockNotificationDAO = new NotificationDAO() {
            @Override
            public void createDirect(Connection connection, long targetUserId, long actorId, String type, String title, String message, String linkUrl) {
                Map<String, Object> notif = new HashMap<>();
                notif.put("targetUserId", targetUserId);
                notif.put("actorId", actorId);
                notif.put("type", type);
                notif.put("title", title);
                notif.put("message", message);
                notif.put("linkUrl", linkUrl);
                createdNotifications.add(notif);
            }
        };

        AcademicChangeDAO dao = new AcademicChangeDAO(mockNotificationDAO);

        AtomicReference<String> userUpdateSql = new AtomicReference<>();
        AtomicReference<String> requestUpdateSql = new AtomicReference<>();
        Map<Integer, Object> userParams = new HashMap<>();
        Map<Integer, Object> requestParams = new HashMap<>();
        AtomicBoolean committed = new AtomicBoolean(false);

        Connection connection = createMockReviewConnection(
                55L, 4, "A",
                userUpdateSql, userParams,
                requestUpdateSql, requestParams,
                committed);

        boolean result = dao.review(connection, 1L, 999L, true, "Approved note");

        assertTrue(result);
        assertTrue(committed.get());
        assertEquals(1, createdNotifications.size());

        Map<String, Object> studentNotif = createdNotifications.get(0);
        assertEquals(55L, studentNotif.get("targetUserId"));
        assertEquals(999L, studentNotif.get("actorId"));
        assertEquals("ACADEMIC_CHANGE_APPROVED", studentNotif.get("type"));
        assertEquals("Academic Change Request Approved", studentNotif.get("title"));
        assertEquals("/profile", studentNotif.get("linkUrl"));
        String msg = (String) studentNotif.get("message");
        assertTrue(msg.contains("approved"));
        assertTrue(msg.contains("semester and section have been updated"));
    }

    @Test
    void adminRejectionCreatesStudentRejectionNotificationWithAdminNote() throws Exception {
        NotificationDAO mockNotificationDAO = new NotificationDAO() {
            @Override
            public void createDirect(Connection connection, long targetUserId, long actorId, String type, String title, String message, String linkUrl) {
                Map<String, Object> notif = new HashMap<>();
                notif.put("targetUserId", targetUserId);
                notif.put("actorId", actorId);
                notif.put("type", type);
                notif.put("title", title);
                notif.put("message", message);
                notif.put("linkUrl", linkUrl);
                createdNotifications.add(notif);
            }
        };

        AcademicChangeDAO dao = new AcademicChangeDAO(mockNotificationDAO);

        AtomicReference<String> userUpdateSql = new AtomicReference<>();
        AtomicReference<String> requestUpdateSql = new AtomicReference<>();
        Map<Integer, Object> userParams = new HashMap<>();
        Map<Integer, Object> requestParams = new HashMap<>();
        AtomicBoolean committed = new AtomicBoolean(false);

        Connection connection = createMockReviewConnection(
                55L, 4, "A",
                userUpdateSql, userParams,
                requestUpdateSql, requestParams,
                committed);

        boolean result = dao.review(connection, 1L, 999L, false, "Section A is currently full");

        assertTrue(result);
        assertTrue(committed.get());
        assertNull(userUpdateSql.get()); // User semester/section NOT changed!

        assertEquals(1, createdNotifications.size());
        Map<String, Object> studentNotif = createdNotifications.get(0);
        assertEquals(55L, studentNotif.get("targetUserId"));
        assertEquals(999L, studentNotif.get("actorId"));
        assertEquals("ACADEMIC_CHANGE_REJECTED", studentNotif.get("type"));
        assertEquals("Academic Change Request Rejected", studentNotif.get("title"));
        assertEquals("/profile", studentNotif.get("linkUrl"));
        String msg = (String) studentNotif.get("message");
        assertTrue(msg.contains("rejected"));
        assertTrue(msg.contains("Admin note: Section A is currently full"));
    }

    private Connection createMockCreateConnection(
            AtomicReference<String> insertSql,
            String fullName, String studentId, Integer semester, String section) {

        ResultSet selectUserRs = (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class[]{ResultSet.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "next" -> true;
                    case "getString" -> {
                        String col = (String) args[0];
                        if ("full_name".equals(col)) yield fullName;
                        if ("student_id".equals(col)) yield studentId;
                        if ("section_name".equals(col)) yield section;
                        yield null;
                    }
                    case "getInt" -> semester != null ? semester : 0;
                    case "wasNull" -> semester == null;
                    case "close" -> null;
                    default -> null;
                });

        ResultSet pendingRs = (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class[]{ResultSet.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "next" -> false; // no pending request
                    case "close" -> null;
                    default -> null;
                });

        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "prepareStatement" -> {
                        String sql = (String) args[0];
                        if (sql.contains("SELECT 1 FROM academic_change_requests")) {
                            yield createMockPreparedStatement(pendingRs, null);
                        } else if (sql.contains("INSERT INTO academic_change_requests")) {
                            insertSql.set(sql);
                            yield createMockPreparedStatement(null, null);
                        } else if (sql.contains("SELECT full_name, student_id")) {
                            yield createMockPreparedStatement(selectUserRs, null);
                        }
                        yield createMockPreparedStatement(null, null);
                    }
                    default -> null;
                });
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
                            yield createMockPreparedStatement(selectRs, null);
                        } else if (sql.contains("UPDATE users SET semester")) {
                            userUpdateSql.set(sql);
                            yield createMockPreparedStatement(null, userParams);
                        } else if (sql.contains("UPDATE academic_change_requests SET status")) {
                            requestUpdateSql.set(sql);
                            yield createMockPreparedStatement(null, requestParams);
                        }
                        yield createMockPreparedStatement(null, null);
                    }
                    default -> null;
                });
    }

    private PreparedStatement createMockPreparedStatement(ResultSet rs, Map<Integer, Object> boundParams) {
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
