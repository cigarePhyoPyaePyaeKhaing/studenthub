package com.studenthub.service;

import static org.junit.jupiter.api.Assertions.*;

import com.studenthub.dao.UserDAO;
import com.studenthub.model.OtpPurpose;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;

class AuthServiceAcademicRegistrationTest {
    @Test void semesterEightRegistrationStoresCanonicalSection() throws Exception {
        CapturingUserDAO users = new CapturingUserDAO();
        CapturingEmailService email = new CapturingEmailService();
        AuthService service = service(users, email);

        AuthService.RegistrationResult result = service.register("TNT-1234", "Test Student",
                "student@example.com", "Password1", "Password1", "8", " e ");

        assertTrue(result.successful());
        assertEquals(8, users.semester);
        assertEquals("E", users.sectionName);
        assertEquals("student@example.com", email.recipient);
    }

    @Test void semesterEightRejectsInvalidSectionsBeforeInsert() throws Exception {
        CapturingUserDAO users = new CapturingUserDAO();
        AuthService service = service(users, new CapturingEmailService());

        assertFalse(service.register("TNT-1234", "Test Student", "student@example.com",
                "Password1", "Password1", "8", "SE").successful());
        assertFalse(service.register("TNT-1234", "Test Student", "student@example.com",
                "Password1", "Password1", "8", "F").successful());
        assertEquals(0, users.insertCount);
    }

    @Test void semesterSevenStoresCanonicalSection() throws Exception {
        CapturingUserDAO users = new CapturingUserDAO();
        AuthService.RegistrationResult result = service(users, new CapturingEmailService()).register(
                "TNT-1234", "Test Student", "student@example.com", "Password1", "Password1", "7", " c ");

        assertTrue(result.successful());
        assertEquals(7, users.semester);
        assertEquals("C", users.sectionName);
    }

    @Test void semesterSevenRejectsNonSectionValuesBeforeInsert() throws Exception {
        CapturingUserDAO users = new CapturingUserDAO();
        AuthService service = service(users, new CapturingEmailService());

        assertFalse(service.register("TNT-1234", "Test Student", "student@example.com",
                "Password1", "Password1", "7", "CSec").successful());
        assertFalse(service.register("TNT-1234", "Test Student", "student@example.com",
                "Password1", "Password1", "7", "F").successful());
        assertEquals(0, users.insertCount);
    }

    @Test void semesterSixStoresCanonicalSection() throws Exception {
        CapturingUserDAO users = new CapturingUserDAO();
        AuthService.RegistrationResult result = service(users, new CapturingEmailService()).register(
                "TNT-1234", "Test Student", "student@example.com", "Password1", "Password1", "6", " b ");

        assertTrue(result.successful());
        assertEquals(6, users.semester);
        assertEquals("B", users.sectionName);
    }

    private AuthService service(CapturingUserDAO users, CapturingEmailService email) {
        OtpService otp = new OtpService() {
            @Override public String issue(Connection connection, long userId, String recipient,
                                          OtpPurpose purpose) {
                return "123456";
            }
        };
        Connection connection = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
                new Class[]{Connection.class}, (proxy, method, args) -> switch (method.getName()) {
                    case "getAutoCommit" -> false;
                    case "isClosed" -> false;
                    case "close", "setAutoCommit", "commit", "rollback" -> null;
                    default -> null;
                });
        return new AuthService(users, otp, email, () -> connection);
    }

    private static final class CapturingUserDAO extends UserDAO {
        int insertCount;
        int semester;
        String sectionName;

        @Override public long createPendingStudent(Connection connection, String studentId, String fullName,
                                                   String email, String passwordHash, int semester,
                                                   String sectionName) throws SQLException {
            insertCount++;
            this.semester = semester;
            this.sectionName = sectionName;
            return 42L;
        }
    }

    private static final class CapturingEmailService implements EmailService {
        String recipient;
        @Override public void sendVerificationOtp(String recipient, String fullName, String otp) {
            this.recipient = recipient;
        }
        @Override public void sendPasswordResetOtp(String recipient, String fullName, String otp) {}
    }
}
