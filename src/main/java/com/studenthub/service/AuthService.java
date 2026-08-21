package com.studenthub.service;

import com.studenthub.dao.UserDAO;
import com.studenthub.model.OtpPurpose;
import com.studenthub.model.User;
import com.studenthub.util.AuthValidation;
import com.studenthub.util.DBConnection;
import com.studenthub.util.PasswordUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Logger;

public class AuthService {
    private static final Logger LOGGER = Logger.getLogger(AuthService.class.getName());
    public enum PasswordResetStatus { DELIVERY_ACCEPTED, ACCOUNT_NOT_FOUND, ACCOUNT_NOT_ELIGIBLE, EMAIL_MISSING, OTP_STORAGE_FAILED, BREVO_CONFIGURATION_MISSING, BREVO_UNAUTHORIZED, BREVO_PROVIDER_ERROR, NETWORK_ERROR, INTERRUPTED_REQUEST, THROTTLED }
    public record PasswordResetRequestResult(PasswordResetStatus status, User user) {
        public boolean delivered() { return status == PasswordResetStatus.DELIVERY_ACCEPTED; }
    }
    public record RegistrationResult(boolean successful, long userId, String message) {
    }

    public enum LoginStatus { SUCCESS, INVALID_CREDENTIALS, EMAIL_NOT_VERIFIED }

    public record LoginResult(LoginStatus status, User user) {
    }

    interface ConnectionProvider { Connection get() throws SQLException; }
    private final UserDAO userDAO;
    private final OtpService otpService;
    private final EmailService emailService;
    private final ConnectionProvider connectionProvider;

    public AuthService() { this(new UserDAO(), new OtpService(), new BrevoEmailService(), DBConnection::getConnection); }

    AuthService(UserDAO userDAO, OtpService otpService, EmailService emailService,
                ConnectionProvider connectionProvider) {
        this.userDAO = userDAO;
        this.otpService = otpService;
        this.emailService = emailService;
        this.connectionProvider = connectionProvider;
    }

    public RegistrationResult register(String studentIdInput, String fullNameInput, String emailInput,
                                       String password, String confirmation)
            throws SQLException, EmailServiceException {
        String studentId = AuthValidation.normalizeStudentId(studentIdInput);
        String email = AuthValidation.normalizeEmail(emailInput);
        String fullName = fullNameInput == null ? "" : fullNameInput.trim();

        if (!AuthValidation.isValidStudentId(studentId)) {
            return new RegistrationResult(false, 0, "Student ID must use the format TNT-0000.");
        }
        if (fullName.length() < 2 || fullName.length() > 100) {
            return new RegistrationResult(false, 0, "Enter a full name between 2 and 100 characters.");
        }
        if (!AuthValidation.isValidEmail(email)) {
            return new RegistrationResult(false, 0, "Enter a valid email address.");
        }
        if (!AuthValidation.isValidPassword(password)) {
            return new RegistrationResult(false, 0,
                    "Password must have at least 8 characters, uppercase, lowercase, and a number.");
        }
        if (!password.equals(confirmation)) {
            return new RegistrationResult(false, 0, "Password confirmation does not match.");
        }

        long userId;
        String otp;
        try (Connection connection = connectionProvider.get()) {
            connection.setAutoCommit(false);
            try {
                userId = userDAO.createPendingStudent(connection, studentId, fullName, email,
                        PasswordUtil.hash(password));
                otp = otpService.issue(connection, userId, email, OtpPurpose.EMAIL_VERIFICATION);
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                if (exception.getErrorCode() == 1062) {
                    return new RegistrationResult(false, 0, "That student ID or email is already registered.");
                }
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
        emailService.sendVerificationOtp(email, fullName, otp);
        return new RegistrationResult(true, userId, "Enter the verification code sent to your email.");
    }

    public OtpService.VerificationResult verifyEmail(long userId, String code) throws SQLException {
        try (Connection connection = connectionProvider.get()) {
            connection.setAutoCommit(false);
            try {
                OtpService.VerificationResult result = otpService.verify(
                        connection, userId, OtpPurpose.EMAIL_VERIFICATION, code);
                if (result == OtpService.VerificationResult.SUCCESS) {
                    userDAO.markEmailVerified(connection, userId);
                }
                connection.commit();
                return result;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public void resendVerification(long userId) throws SQLException, EmailServiceException {
        Optional<User> found = userDAO.findById(userId);
        if (found.isEmpty() || found.get().emailVerified()) {
            return;
        }
        User user = found.get();
        String otp;
        try (Connection connection = connectionProvider.get()) {
            connection.setAutoCommit(false);
            try {
                otp = otpService.issue(connection, userId, user.email(), OtpPurpose.EMAIL_VERIFICATION);
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
        emailService.sendVerificationOtp(user.email(), user.fullName(), otp);
    }

    public LoginResult login(String loginInput, String password) throws SQLException {
        String login = loginInput != null && loginInput.trim().toUpperCase().startsWith("TNT-")
                ? AuthValidation.normalizeStudentId(loginInput) : AuthValidation.normalizeEmail(loginInput);
        Optional<User> found = userDAO.findByLogin(login);
        if (found.isEmpty() || !PasswordUtil.matches(password, found.get().passwordHash())) {
            return new LoginResult(LoginStatus.INVALID_CREDENTIALS, null);
        }
        if (!found.get().emailVerified()) {
            return new LoginResult(LoginStatus.EMAIL_NOT_VERIFIED, found.get());
        }
        return new LoginResult(LoginStatus.SUCCESS, found.get());
    }

    public PasswordResetRequestResult requestPasswordReset(String loginInput) {
        boolean emailIdentifier = loginInput != null && loginInput.contains("@");
        String identifierType = emailIdentifier ? "EMAIL" : "STUDENT_ID";
        String normalized = emailIdentifier ? AuthValidation.normalizeEmail(loginInput)
                : AuthValidation.normalizeStudentId(loginInput);
        Optional<User> found;
        try {
            found = emailIdentifier ? userDAO.findByEmail(normalized) : userDAO.findByStudentId(normalized);
        } catch (SQLException exception) {
            logSqlFailure("ACCOUNT_LOOKUP_FAILED", exception);
            return new PasswordResetRequestResult(PasswordResetStatus.OTP_STORAGE_FAILED, null);
        }
        if (found.isEmpty()) {
            LOGGER.info("Password reset ACCOUNT_NOT_FOUND for identifier type=" + identifierType);
            return new PasswordResetRequestResult(PasswordResetStatus.ACCOUNT_NOT_FOUND, null);
        }
        User user = found.get();
        LOGGER.info("Password reset account resolved for identifier type=" + identifierType);
        return deliverPasswordReset(user);
    }

    private PasswordResetRequestResult deliverPasswordReset(User user) {
        if (user.email() == null || user.email().isBlank()) {
            LOGGER.warning("Password reset EMAIL_MISSING for resolved account.");
            return new PasswordResetRequestResult(PasswordResetStatus.EMAIL_MISSING, null);
        }
        if (!user.emailVerified()) {
            LOGGER.info("Password reset ACCOUNT_NOT_ELIGIBLE for resolved account.");
            return new PasswordResetRequestResult(PasswordResetStatus.ACCOUNT_NOT_ELIGIBLE, null);
        }
        String otp;
        try (Connection connection = connectionProvider.get()) {
            connection.setAutoCommit(false);
            try {
                otp = otpService.issue(connection, user.userId(), user.email(), OtpPurpose.PASSWORD_RESET);
                connection.commit();
                LOGGER.info("Password reset OTP_STORAGE_SUCCEEDED for resolved account.");
            } catch (IllegalStateException exception) {
                connection.rollback();
                LOGGER.info("Password reset THROTTLED by server-side OTP issuance policy.");
                return new PasswordResetRequestResult(PasswordResetStatus.THROTTLED, null);
            } catch (SQLException exception) {
                connection.rollback();
                logSqlFailure("OTP_STORAGE_FAILED", exception);
                return new PasswordResetRequestResult(PasswordResetStatus.OTP_STORAGE_FAILED, null);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            logSqlFailure("OTP_STORAGE_FAILED", exception);
            return new PasswordResetRequestResult(PasswordResetStatus.OTP_STORAGE_FAILED, null);
        }
        try {
            emailService.sendPasswordResetOtp(user.email(), user.fullName(), otp);
        } catch (EmailServiceException exception) {
            invalidateFailedDelivery(user.userId());
            PasswordResetStatus status = switch (exception.reason()) {
                case CONFIGURATION -> PasswordResetStatus.BREVO_CONFIGURATION_MISSING;
                case UNAUTHORIZED -> PasswordResetStatus.BREVO_UNAUTHORIZED;
                case NETWORK -> PasswordResetStatus.NETWORK_ERROR;
                case INTERRUPTED -> PasswordResetStatus.INTERRUPTED_REQUEST;
                default -> PasswordResetStatus.BREVO_PROVIDER_ERROR;
            };
            String causeType = exception.getCause() == null ? "none" : exception.getCause().getClass().getName();
            LOGGER.severe("Password reset " + status + ": " + exception.getMessage() + ", cause=" + causeType);
            return new PasswordResetRequestResult(status, null);
        }
        return new PasswordResetRequestResult(PasswordResetStatus.DELIVERY_ACCEPTED, user);
    }

    public PasswordResetRequestResult resendPasswordReset(long userId) {
        try {
            Optional<User> found = userDAO.findById(userId);
            if (found.isEmpty()) {
                LOGGER.info("Password reset resend ACCOUNT_NOT_FOUND for session-bound user.");
                return new PasswordResetRequestResult(PasswordResetStatus.ACCOUNT_NOT_FOUND, null);
            }
            LOGGER.info("Password reset resend resolved session-bound user.");
            return deliverPasswordReset(found.get());
        } catch (SQLException exception) {
            logSqlFailure("ACCOUNT_LOOKUP_FAILED", exception);
            return new PasswordResetRequestResult(PasswordResetStatus.OTP_STORAGE_FAILED, null);
        }
    }

    private void invalidateFailedDelivery(long userId) {
        try (Connection connection = connectionProvider.get()) {
            otpService.invalidate(connection, userId, OtpPurpose.PASSWORD_RESET);
        } catch (SQLException exception) { logSqlFailure("FAILED_DELIVERY_OTP_INVALIDATION_FAILED", exception); }
    }

    private void logSqlFailure(String category, SQLException exception) {
        LOGGER.severe("Password reset " + category + ": SQLState=" + exception.getSQLState()
                + ", vendorCode=" + exception.getErrorCode() + ", exception=" + exception.getClass().getName());
    }

    public Optional<Long> resolveUserId(String loginInput) throws SQLException {
        String login = loginInput != null && loginInput.trim().toUpperCase().startsWith("TNT-")
                ? AuthValidation.normalizeStudentId(loginInput) : AuthValidation.normalizeEmail(loginInput);
        return userDAO.findByLogin(login).map(User::userId);
    }

    public OtpService.VerificationResult verifyPasswordReset(long userId, String code) throws SQLException {
        try (Connection connection = connectionProvider.get()) {
            connection.setAutoCommit(false);
            try {
                OtpService.VerificationResult result = otpService.verify(
                        connection, userId, OtpPurpose.PASSWORD_RESET, code);
                connection.commit();
                return result;
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public String resetPassword(long userId, String password, String confirmation) throws SQLException {
        if (!AuthValidation.isValidPassword(password)) {
            return "Password must have at least 8 characters, uppercase, lowercase, and a number.";
        }
        if (!password.equals(confirmation)) {
            return "Password confirmation does not match.";
        }
        try (Connection connection = connectionProvider.get()) {
            userDAO.updatePassword(connection, userId, PasswordUtil.hash(password));
        }
        return null;
    }
}
