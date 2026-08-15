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

public class AuthService {
    public record RegistrationResult(boolean successful, long userId, String message) {
    }

    public enum LoginStatus { SUCCESS, INVALID_CREDENTIALS, EMAIL_NOT_VERIFIED }

    public record LoginResult(LoginStatus status, User user) {
    }

    private final UserDAO userDAO = new UserDAO();
    private final OtpService otpService = new OtpService();
    private final EmailService emailService = new BrevoEmailService();

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
        try (Connection connection = DBConnection.getConnection()) {
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
        try (Connection connection = DBConnection.getConnection()) {
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
        try (Connection connection = DBConnection.getConnection()) {
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

    public void requestPasswordReset(String loginInput) throws SQLException, EmailServiceException {
        String login = loginInput != null && loginInput.trim().toUpperCase().startsWith("TNT-")
                ? AuthValidation.normalizeStudentId(loginInput) : AuthValidation.normalizeEmail(loginInput);
        Optional<User> found = userDAO.findByLogin(login);
        if (found.isEmpty() || !found.get().emailVerified()) {
            return;
        }
        User user = found.get();
        String otp;
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                otp = otpService.issue(connection, user.userId(), user.email(), OtpPurpose.PASSWORD_RESET);
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
        emailService.sendPasswordResetOtp(user.email(), user.fullName(), otp);
    }

    public Optional<Long> resolveUserId(String loginInput) throws SQLException {
        String login = loginInput != null && loginInput.trim().toUpperCase().startsWith("TNT-")
                ? AuthValidation.normalizeStudentId(loginInput) : AuthValidation.normalizeEmail(loginInput);
        return userDAO.findByLogin(login).map(User::userId);
    }

    public OtpService.VerificationResult verifyPasswordReset(long userId, String code) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
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
        try (Connection connection = DBConnection.getConnection()) {
            userDAO.updatePassword(connection, userId, PasswordUtil.hash(password));
        }
        return null;
    }
}
