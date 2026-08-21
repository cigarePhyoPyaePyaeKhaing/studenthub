package com.studenthub.service;

import com.studenthub.dao.UserDAO;
import com.studenthub.model.OtpPurpose;
import com.studenthub.model.Role;
import com.studenthub.model.User;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class RegistrationEnhancementTest {

    @Test
    void testRegistrationRejectsInvalidSemester() throws Exception {
        FakeUserDAO userDAO = new FakeUserDAO();
        FakeOtpService otpService = new FakeOtpService();
        FakeEmailService emailService = new FakeEmailService();

        AuthService authService = new AuthService(userDAO, otpService, emailService, RegistrationEnhancementTest::connection);

        // Semester 0 (invalid)
        AuthService.RegistrationResult result0 = authService.register(
                "TNT-1234", "Valid Name", "student@uit.edu.mm", "Password123!", "Password123!", 1L, 0, "A");
        assertFalse(result0.successful());
        assertEquals("Semester must be between 1 and 10.", result0.message());

        // Semester 11 (invalid)
        AuthService.RegistrationResult result11 = authService.register(
                "TNT-1234", "Valid Name", "student@uit.edu.mm", "Password123!", "Password123!", 1L, 11, "A");
        assertFalse(result11.successful());
        assertEquals("Semester must be between 1 and 10.", result11.message());
    }

    @Test
    void testRegistrationAcceptsValidSemesterAndSection() throws Exception {
        FakeUserDAO userDAO = new FakeUserDAO();
        FakeOtpService otpService = new FakeOtpService();
        FakeEmailService emailService = new FakeEmailService();

        AuthService authService = new AuthService(userDAO, otpService, emailService, RegistrationEnhancementTest::connection);

        AuthService.RegistrationResult result = authService.register(
                "TNT-1234", "Valid Name", "student@uit.edu.mm", "Password123!", "Password123!", 1L, 5, "A");

        assertTrue(result.successful());
        assertEquals(99L, result.userId());
        assertEquals("TNT-1234", userDAO.lastStudentId);
        assertEquals(1L, userDAO.lastUniversityId);
        assertEquals(5, userDAO.lastSemester);
        assertEquals("A", userDAO.lastSection);
        assertEquals("student@uit.edu.mm", emailService.sentTo);
        assertEquals("123456", emailService.sentOtp);
    }

    @Test
    void testCleanupExpiredUnverifiedRegistrationsDeletesExpiredOnly() throws Exception {
        FakeUserDAO userDAO = new FakeUserDAO();
        FakeOtpService otpService = new FakeOtpService();
        FakeEmailService emailService = new FakeEmailService();

        AuthService authService = new AuthService(userDAO, otpService, emailService, RegistrationEnhancementTest::connection);
        int cleaned = authService.cleanupExpiredUnverifiedRegistrations(Duration.ofDays(2));

        assertEquals(3, cleaned);
        assertNotNull(userDAO.lastCutoff);
    }

    private static Connection connection() {
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class[]{Connection.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getAutoCommit" -> true;
                    case "isClosed" -> false;
                    case "isWrapperFor" -> false;
                    case "unwrap" -> null;
                    default -> null;
                });
    }

    private static class FakeUserDAO extends UserDAO {
        String lastStudentId;
        Long lastUniversityId;
        Integer lastSemester;
        String lastSection;
        Instant lastCutoff;

        @Override
        public long createPendingStudent(Connection connection, String studentId, String fullName,
                                         String email, String passwordHash, Long universityId,
                                         Integer semester, String sectionName) {
            this.lastStudentId = studentId;
            this.lastUniversityId = universityId;
            this.lastSemester = semester;
            this.lastSection = sectionName;
            return 99L;
        }

        @Override
        public int deleteExpiredUnverifiedUsers(Connection connection, Instant cutoff) {
            this.lastCutoff = cutoff;
            return 3;
        }
    }

    private static class FakeOtpService extends OtpService {
        @Override
        public String issue(Connection connection, long userId, String email, OtpPurpose purpose) {
            return "123456";
        }
    }

    private static class FakeEmailService implements EmailService {
        String sentTo;
        String sentOtp;

        @Override
        public void sendVerificationOtp(String recipient, String fullName, String otp) {
            this.sentTo = recipient;
            this.sentOtp = otp;
        }

        @Override
        public void sendPasswordResetOtp(String recipient, String fullName, String otp) {
        }
    }
}
