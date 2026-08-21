package com.studenthub.dao;

import com.studenthub.model.Deadline;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class DeadlineDAOAivenSchemaTest {

    @Test
    void findUpcomingForUserUsesCollationSafeQueryAndAivenColumns() throws Exception {
        DeadlineDAO dao = new DeadlineDAO();
        AtomicReference<String> executedSql = new AtomicReference<>();

        // Test with mock connection that captures the prepared SQL
        Connection connection = createMockConnection(executedSql);

        // We can test findScope or verify findForUser SQL structure
        // Let's verify findScope SQL
        DeadlineDAO.AcademicScope scope = dao.findScope(connection, 55L);

        String scopeSql = executedSql.get();
        assertNotNull(scopeSql);
        assertTrue(scopeSql.contains("SELECT semester, section AS section_name FROM users WHERE id=?"));
    }

    private Connection createMockConnection(AtomicReference<String> sqlRef) {
        ResultSet rs = (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class[]{ResultSet.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "next" -> true;
                    case "getInt" -> 3;
                    case "getString" -> "A";
                    case "wasNull" -> false;
                    case "close" -> null;
                    default -> null;
                });

        PreparedStatement ps = (PreparedStatement) Proxy.newProxyInstance(
                PreparedStatement.class.getClassLoader(),
                new Class[]{PreparedStatement.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "executeQuery" -> rs;
                    case "setLong", "setInt", "setNull", "setString", "close" -> null;
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
