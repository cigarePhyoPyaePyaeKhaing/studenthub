package com.studenthub.dao;

import com.studenthub.model.OtpPurpose;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

public class OtpDAO {
    public record ActiveCode(long codeId, String codeHash, int attemptCount, Instant expiresAt) {
    }

    public void invalidateActive(Connection connection, long userId, OtpPurpose purpose) throws SQLException {
        String sql = "UPDATE verification_codes SET used_at = UTC_TIMESTAMP() "
                + "WHERE user_id = ? AND purpose = ? AND used_at IS NULL";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setString(2, purpose.name());
            statement.executeUpdate();
        }
    }

    public void create(Connection connection, long userId, String email, OtpPurpose purpose,
                       String codeHash, Instant expiresAt) throws SQLException {
        String sql = "INSERT INTO verification_codes "
                + "(user_id, email, purpose, code_hash, expires_at) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setString(2, email);
            statement.setString(3, purpose.name());
            statement.setString(4, codeHash);
            statement.setTimestamp(5, Timestamp.from(expiresAt));
            statement.executeUpdate();
        }
    }

    public Optional<ActiveCode> lockActive(Connection connection, long userId, OtpPurpose purpose)
            throws SQLException {
        String sql = """
                SELECT code_id, code_hash, attempt_count, expires_at
                FROM verification_codes
                WHERE user_id = ? AND purpose = ? AND used_at IS NULL
                ORDER BY created_at DESC LIMIT 1 FOR UPDATE
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setString(2, purpose.name());
            try (ResultSet results = statement.executeQuery()) {
                if (!results.next()) {
                    return Optional.empty();
                }
                return Optional.of(new ActiveCode(results.getLong("code_id"), results.getString("code_hash"),
                        results.getInt("attempt_count"), results.getTimestamp("expires_at").toInstant()));
            }
        }
    }

    public void incrementAttempts(Connection connection, long codeId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE verification_codes SET attempt_count = attempt_count + 1 WHERE code_id = ?")) {
            statement.setLong(1, codeId);
            statement.executeUpdate();
        }
    }

    public void markUsed(Connection connection, long codeId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE verification_codes SET used_at = UTC_TIMESTAMP() WHERE code_id = ? AND used_at IS NULL")) {
            statement.setLong(1, codeId);
            statement.executeUpdate();
        }
    }

    public boolean activeSentWithin(Connection connection, long userId, OtpPurpose purpose, int seconds)
            throws SQLException {
        String sql = "SELECT EXISTS(SELECT 1 FROM verification_codes WHERE user_id = ? AND purpose = ? "
                + "AND used_at IS NULL AND created_at > ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.setString(2, purpose.name());
            statement.setTimestamp(3, Timestamp.from(Instant.now().minusSeconds(seconds)));
            try (ResultSet results = statement.executeQuery()) {
                return results.next() && results.getBoolean(1);
            }
        }
    }
}
