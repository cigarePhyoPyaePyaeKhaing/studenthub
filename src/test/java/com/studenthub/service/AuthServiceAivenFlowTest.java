package com.studenthub.service;

import com.studenthub.dao.OtpDAO;
import com.studenthub.dao.UserDAO;
import com.studenthub.model.OtpPurpose;
import com.studenthub.model.Role;
import com.studenthub.model.User;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceAivenFlowTest {

    @Test
    void fullRegistrationEmailVerificationAndLoginFlow() throws Exception {
        FakeAivenUserDAO userDAO = new FakeAivenUserDAO();
        FakeAivenOtpDAO otpDAO = new FakeAivenOtpDAO();
        FakeEmailService emailService = new FakeEmailService();
        Connection mockConn = (Connection) java.lang.reflect.Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> null);
        AuthService authService = new AuthService(userDAO, new OtpServiceTestAdapter(otpDAO), emailService, () -> mockConn);

        // 1. Register
        AuthService.RegistrationResult regResult = authService.register(
                "TNT-1234", "Min Min", "minmin@uit.edu", "Password123!", "Password123!");

        assertTrue(regResult.successful());
        long createdUserId = regResult.userId();
        assertTrue(createdUserId > 0);
        assertNotNull(emailService.lastOtp);
        assertEquals(6, emailService.lastOtp.length());

        // Account is not verified yet
        assertFalse(userDAO.users.get(createdUserId).emailVerified());

        // 2. Login before verification should report EMAIL_NOT_VERIFIED
        AuthService.LoginResult unverifiedLogin = authService.login("TNT-1234", "Password123!");
        assertEquals(AuthService.LoginStatus.EMAIL_NOT_VERIFIED, unverifiedLogin.status());

        // 3. Verify Email with correct OTP
        OtpService.VerificationResult verifyResult = authService.verifyEmail(createdUserId, emailService.lastOtp);
        assertEquals(OtpService.VerificationResult.SUCCESS, verifyResult);
        assertTrue(userDAO.users.get(createdUserId).emailVerified());

        // 4. Login after verification should succeed
        AuthService.LoginResult verifiedLogin = authService.login("minmin@uit.edu", "Password123!");
        assertEquals(AuthService.LoginStatus.SUCCESS, verifiedLogin.status());
        assertNotNull(verifiedLogin.user());
        assertEquals("Min Min", verifiedLogin.user().fullName());
        assertEquals("TNT-1234", verifiedLogin.user().studentId());
    }

    @Test
    void passwordResetFlowAgainstAivenSchema() throws Exception {
        FakeAivenUserDAO userDAO = new FakeAivenUserDAO();
        FakeAivenOtpDAO otpDAO = new FakeAivenOtpDAO();
        FakeEmailService emailService = new FakeEmailService();
        Connection mockConn = (Connection) java.lang.reflect.Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> null);
        AuthService authService = new AuthService(userDAO, new OtpServiceTestAdapter(otpDAO), emailService, () -> mockConn);

        // Pre-populate verified user in Aiven schema
        User verifiedUser = new User(10L, "TNT-5678", "Hla Hla", "hlahla@uit.edu",
                com.studenthub.util.PasswordUtil.hash("OldPassword1!"), Role.STUDENT, true, null);
        userDAO.users.put(10L, verifiedUser);

        // Request Password Reset
        AuthService.PasswordResetRequestResult resetReq = authService.requestPasswordReset("TNT-5678");
        assertEquals(AuthService.PasswordResetStatus.DELIVERY_ACCEPTED, resetReq.status());
        assertNotNull(emailService.lastOtp);

        // Verify Reset OTP
        OtpService.VerificationResult verifyReset = authService.verifyPasswordReset(10L, emailService.lastOtp);
        assertEquals(OtpService.VerificationResult.SUCCESS, verifyReset);

        // Reset Password
        String resetError = authService.resetPassword(10L, "NewPassword999!", "NewPassword999!");
        assertNull(resetError);

        // Login with new password
        AuthService.LoginResult newLogin = authService.login("hlahla@uit.edu", "NewPassword999!");
        assertEquals(AuthService.LoginStatus.SUCCESS, newLogin.status());
    }

    private static class FakeAivenUserDAO extends UserDAO {
        final Map<Long, User> users = new HashMap<>();
        long nextId = 1;

        @Override
        public long createPendingStudent(Connection connection, String studentId, String fullName,
                                         String email, String passwordHash) {
            long id = nextId++;
            users.put(id, new User(id, studentId, fullName, email, passwordHash, Role.STUDENT, false, null));
            return id;
        }

        @Override
        public Optional<User> findByLogin(String normalizedLogin) {
            return users.values().stream()
                    .filter(u -> normalizedLogin.equalsIgnoreCase(u.studentId()) || normalizedLogin.equalsIgnoreCase(u.email()))
                    .findFirst();
        }

        @Override
        public Optional<User> findByStudentId(String studentId) {
            return users.values().stream()
                    .filter(u -> studentId.equalsIgnoreCase(u.studentId()))
                    .findFirst();
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return users.values().stream()
                    .filter(u -> email.equalsIgnoreCase(u.email()))
                    .findFirst();
        }

        @Override
        public Optional<User> findById(long userId) {
            return Optional.ofNullable(users.get(userId));
        }

        @Override
        public void markEmailVerified(Connection connection, long userId) {
            User existing = users.get(userId);
            if (existing != null) {
                users.put(userId, new User(existing.userId(), existing.studentId(), existing.fullName(),
                        existing.email(), existing.passwordHash(), existing.role(), true, existing.googleSub()));
            }
        }

        @Override
        public void updatePassword(Connection connection, long userId, String passwordHash) {
            User existing = users.get(userId);
            if (existing != null) {
                users.put(userId, new User(existing.userId(), existing.studentId(), existing.fullName(),
                        existing.email(), passwordHash, existing.role(), existing.emailVerified(), existing.googleSub()));
            }
        }
    }

    private static class FakeAivenOtpDAO extends OtpDAO {
        String activeCodeHash;
        int attempts = 0;
        boolean active = false;

        @Override
        public void create(Connection connection, long userId, String email, OtpPurpose purpose,
                           String codeHash, java.time.Instant expiresAt) {
            this.activeCodeHash = codeHash;
            this.attempts = 0;
            this.active = true;
        }

        @Override
        public Optional<ActiveCode> lockActive(Connection connection, long userId, OtpPurpose purpose) {
            if (!active) return Optional.empty();
            return Optional.of(new ActiveCode(1L, activeCodeHash, attempts, java.time.Instant.now().plusSeconds(600)));
        }

        @Override
        public void markUsed(Connection connection, long codeId) {
            this.active = false;
        }

        @Override
        public void incrementAttempts(Connection connection, long codeId) {
            this.attempts++;
        }

        @Override
        public boolean activeSentWithin(Connection connection, long userId, OtpPurpose purpose, int seconds) {
            return false;
        }

        @Override
        public void invalidateActive(Connection connection, long userId, OtpPurpose purpose) {
            this.active = false;
        }
    }

    private static class OtpServiceTestAdapter extends OtpService {
        private final FakeAivenOtpDAO fakeOtpDAO;

        OtpServiceTestAdapter(FakeAivenOtpDAO fakeOtpDAO) {
            this.fakeOtpDAO = fakeOtpDAO;
        }

        @Override
        public String issue(Connection connection, long userId, String email, OtpPurpose purpose) {
            String code = "123456";
            fakeOtpDAO.create(connection, userId, email, purpose, com.studenthub.util.PasswordUtil.hash(code), java.time.Instant.now().plusSeconds(600));
            return code;
        }

        @Override
        public VerificationResult verify(Connection connection, long userId, OtpPurpose purpose, String code) {
            Optional<OtpDAO.ActiveCode> active = fakeOtpDAO.lockActive(connection, userId, purpose);
            if (active.isEmpty()) return VerificationResult.INVALID;
            if (com.studenthub.util.PasswordUtil.matches(code, active.get().codeHash())) {
                fakeOtpDAO.markUsed(connection, 1L);
                return VerificationResult.SUCCESS;
            }
            fakeOtpDAO.incrementAttempts(connection, 1L);
            return VerificationResult.INVALID;
        }
    }

    private static class FakeEmailService implements EmailService {
        String lastOtp;

        @Override
        public void sendVerificationOtp(String recipient, String fullName, String otp) {
            this.lastOtp = otp;
        }

        @Override
        public void sendPasswordResetOtp(String recipient, String fullName, String otp) {
            this.lastOtp = otp;
        }
    }
}
