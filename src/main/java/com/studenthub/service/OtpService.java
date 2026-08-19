package com.studenthub.service;

import com.studenthub.dao.OtpDAO;
import com.studenthub.model.OtpPurpose;
import com.studenthub.util.PasswordUtil;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

public class OtpService {
    public enum VerificationResult { SUCCESS, INVALID, EXPIRED, TOO_MANY_ATTEMPTS }

    private static final Duration VALIDITY = Duration.ofMinutes(10);
    private static final int MAX_ATTEMPTS = 5;
    private static final SecureRandom RANDOM = new SecureRandom();
    private final OtpDAO otpDAO = new OtpDAO();

    public String issue(Connection connection, long userId, String email, OtpPurpose purpose)
            throws SQLException {
        if (otpDAO.sentWithin(connection, userId, purpose, 60)) {
            throw new IllegalStateException("Please wait 60 seconds before requesting another code.");
        }
        String code = "%06d".formatted(RANDOM.nextInt(1_000_000));
        otpDAO.invalidateActive(connection, userId, purpose);
        otpDAO.create(connection, userId, email, purpose, PasswordUtil.hash(code), Instant.now().plus(VALIDITY));
        return code;
    }

    public VerificationResult verify(Connection connection, long userId, OtpPurpose purpose, String code)
            throws SQLException {
        Optional<OtpDAO.ActiveCode> optional = otpDAO.lockActive(connection, userId, purpose);
        if (optional.isEmpty()) {
            return VerificationResult.INVALID;
        }
        OtpDAO.ActiveCode active = optional.get();
        if (active.attemptCount() >= MAX_ATTEMPTS) {
            return VerificationResult.TOO_MANY_ATTEMPTS;
        }
        if (Instant.now().isAfter(active.expiresAt())) {
            otpDAO.markUsed(connection, active.codeId());
            return VerificationResult.EXPIRED;
        }
        if (code == null || !code.matches("\\d{6}") || !PasswordUtil.matches(code, active.codeHash())) {
            otpDAO.incrementAttempts(connection, active.codeId());
            return VerificationResult.INVALID;
        }
        otpDAO.markUsed(connection, active.codeId());
        return VerificationResult.SUCCESS;
    }

    public void invalidate(Connection connection, long userId, OtpPurpose purpose) throws SQLException {
        otpDAO.invalidateActive(connection, userId, purpose);
    }
}
