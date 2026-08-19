package com.studenthub.service;

import com.studenthub.dao.UserDAO;
import com.studenthub.model.OtpPurpose;
import com.studenthub.model.Role;
import com.studenthub.model.User;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AuthServicePasswordResetTest {
    private static final User VERIFIED = new User(2419L, "TNT-2419", "Test Student",
            "registered@gmail.com", "hash", Role.STUDENT, true, null);

    @Test void studentIdResolvesStoredEmailAndSendsPasswordResetOtp() {
        FakeUserDAO users = new FakeUserDAO(VERIFIED);
        FakeOtpService otps = new FakeOtpService();
        CapturingEmailService email = new CapturingEmailService();
        AuthService.PasswordResetRequestResult result = service(users, otps, email).requestPasswordReset(" tnt-2419 ");

        assertTrue(result.delivered());
        assertEquals("TNT-2419", users.studentIdLookup);
        assertNull(users.emailLookup);
        assertEquals("registered@gmail.com", otps.recipient);
        assertEquals(OtpPurpose.PASSWORD_RESET, otps.purpose);
        assertEquals("registered@gmail.com", email.recipient);
        assertEquals("654321", email.otp);
    }

    @Test void emailLookupResolvesTheSameAccount() {
        FakeUserDAO users = new FakeUserDAO(VERIFIED);
        CapturingEmailService email = new CapturingEmailService();
        assertTrue(service(users, new FakeOtpService(), email)
                .requestPasswordReset(" REGISTERED@GMAIL.COM ").delivered());
        assertEquals("registered@gmail.com", users.emailLookup);
        assertEquals("registered@gmail.com", email.recipient);
    }

    @Test void unknownIdentifierDoesNotStoreOrSendOtp() {
        FakeUserDAO users = new FakeUserDAO(null);
        FakeOtpService otps = new FakeOtpService();
        CapturingEmailService email = new CapturingEmailService();
        AuthService.PasswordResetRequestResult result = service(users, otps, email).requestPasswordReset("TNT-9999");
        assertEquals(AuthService.PasswordResetStatus.ACCOUNT_NOT_FOUND, result.status());
        assertNull(otps.recipient);
        assertNull(email.recipient);
    }

    @Test void unverifiedAccountIsNotEligible() {
        User unverified = new User(2419L, "TNT-2419", "Test Student", "registered@gmail.com",
                "hash", Role.STUDENT, false, null);
        CapturingEmailService email = new CapturingEmailService();
        AuthService.PasswordResetRequestResult result = service(new FakeUserDAO(unverified), new FakeOtpService(), email)
                .requestPasswordReset("TNT-2419");
        assertEquals(AuthService.PasswordResetStatus.ACCOUNT_NOT_ELIGIBLE, result.status());
        assertNull(email.recipient);
    }

    @Test void otpPersistenceFailureDoesNotReportDelivery() {
        FakeOtpService otps = new FakeOtpService();
        otps.storageFailure = true;
        CapturingEmailService email = new CapturingEmailService();
        AuthService.PasswordResetRequestResult result = service(new FakeUserDAO(VERIFIED), otps, email)
                .requestPasswordReset("TNT-2419");
        assertEquals(AuthService.PasswordResetStatus.OTP_STORAGE_FAILED, result.status());
        assertNull(email.recipient);
    }

    @Test void providerFailureDoesNotReportDeliveryAndInvalidatesOtp() {
        FakeOtpService otps = new FakeOtpService();
        CapturingEmailService email = new CapturingEmailService();
        email.failure = new EmailServiceException("Brevo email API rejected the request with HTTP 401.", null,
                EmailServiceException.Reason.UNAUTHORIZED, 401);
        AuthService.PasswordResetRequestResult result = service(new FakeUserDAO(VERIFIED), otps, email)
                .requestPasswordReset("TNT-2419");
        assertEquals(AuthService.PasswordResetStatus.BREVO_UNAUTHORIZED, result.status());
        assertTrue(otps.invalidated);
    }

    private AuthService service(UserDAO users, OtpService otps, EmailService email) {
        return new AuthService(users, otps, email, AuthServicePasswordResetTest::connection);
    }

    private static Connection connection() {
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class[]{Connection.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getAutoCommit" -> true;
                    case "isClosed" -> false;
                    case "isWrapperFor" -> false;
                    case "unwrap" -> null;
                    default -> method.getReturnType().isPrimitive() ? primitiveDefault(method.getReturnType()) : null;
                });
    }

    private static Object primitiveDefault(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0F;
        if (type == double.class) return 0D;
        if (type == char.class) return '\0';
        return null;
    }

    private static class FakeUserDAO extends UserDAO {
        private final User user;
        String studentIdLookup;
        String emailLookup;
        FakeUserDAO(User user) { this.user = user; }
        @Override public Optional<User> findByStudentId(String value) { studentIdLookup = value; return Optional.ofNullable(user); }
        @Override public Optional<User> findByEmail(String value) { emailLookup = value; return Optional.ofNullable(user); }
        @Override public Optional<User> findById(long value) { return Optional.ofNullable(user); }
    }

    private static class FakeOtpService extends OtpService {
        String recipient;
        OtpPurpose purpose;
        boolean storageFailure;
        boolean invalidated;
        @Override public String issue(Connection connection, long userId, String email, OtpPurpose purpose) throws SQLException {
            if (storageFailure) throw new SQLException("simulated");
            this.recipient = email;
            this.purpose = purpose;
            return "654321";
        }
        @Override public void invalidate(Connection connection, long userId, OtpPurpose purpose) { invalidated = true; }
    }

    private static class CapturingEmailService implements EmailService {
        String recipient;
        String otp;
        EmailServiceException failure;
        @Override public void sendVerificationOtp(String recipient, String fullName, String otp) { fail("Registration email must not be used."); }
        @Override public void sendPasswordResetOtp(String recipient, String fullName, String otp) throws EmailServiceException {
            this.recipient = recipient;
            this.otp = otp;
            if (failure != null) throw failure;
        }
    }
}
