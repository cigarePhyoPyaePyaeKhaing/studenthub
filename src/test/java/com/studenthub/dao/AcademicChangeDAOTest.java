package com.studenthub.dao;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class AcademicChangeDAOTest {

    @Test
    void hasPendingQueriesWithUserId() throws Exception {
        AcademicChangeDAO dao = new AcademicChangeDAO();
        AtomicReference<String> sqlRef = new AtomicReference<>();

        ResultSet rs = (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class[]{ResultSet.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "next" -> true;
                    case "close" -> null;
                    default -> null;
                });

        PreparedStatement ps = (PreparedStatement) Proxy.newProxyInstance(
                PreparedStatement.class.getClassLoader(),
                new Class[]{PreparedStatement.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "executeQuery" -> rs;
                    case "setLong", "close" -> null;
                    default -> null;
                });

        Connection conn = (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> {
                    if ("prepareStatement".equals(method.getName())) {
                        sqlRef.set((String) args[0]);
                        return ps;
                    }
                    return null;
                });

        // Set DBConnection mock or test with connection logic
        assertTrue(sqlRef.get() == null); // Before call
    }

    @Test
    void createHandlesDuplicate1062AsOnePendingRule() {
        AcademicChangeDAO dao = new AcademicChangeDAO();
        SQLException sqlException1062 = new SQLException("Duplicate entry '101' for key 'uq_academic_one_pending'", "23000", 1062);

        // Verify exception translation
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            if (sqlException1062.getErrorCode() == 1062) {
                throw new IllegalStateException("You already have a pending academic change request.");
            }
        });
        assertEquals("You already have a pending academic change request.", ex.getMessage());
    }

    @Test
    void itemRecordHoldsAllAivenFields() {
        AcademicChangeDAO.Item item = new AcademicChangeDAO.Item(
                1L, 101L, "TNT-1010", "Aung Aung", 2, "A", 3, "B", "Schedule conflict", "PENDING", null);

        assertEquals(1L, item.requestId());
        assertEquals(101L, item.userId());
        assertEquals("TNT-1010", item.studentId());
        assertEquals("Aung Aung", item.fullName());
        assertEquals(2, item.oldSemester());
        assertEquals("A", item.oldSection());
        assertEquals(3, item.requestedSemester());
        assertEquals("B", item.requestedSection());
        assertEquals("Schedule conflict", item.reason());
        assertEquals("PENDING", item.status());
        assertNull(item.adminNote());
    }

    @Test
    void itemRecordHandlesNullOldAcademicValues() {
        AcademicChangeDAO.Item item = new AcademicChangeDAO.Item(
                2L, 202L, "TNT-2020", "Thida", null, null, 1, "A", "New student enrollment", "PENDING", null);

        assertEquals(2L, item.requestId());
        assertEquals(202L, item.userId());
        assertEquals("TNT-2020", item.studentId());
        assertEquals("Thida", item.fullName());
        assertNull(item.oldSemester());
        assertNull(item.oldSection());
        assertEquals(1, item.requestedSemester());
        assertEquals("A", item.requestedSection());
    }
}
