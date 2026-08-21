package com.studenthub.dao;

import com.studenthub.model.University;
import com.studenthub.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UniversityDAO {

    public List<University> findAll() throws SQLException {
        String sql = """
                SELECT university_id, name, short_name, status, requested_by, approved_by, created_at, approved_at
                FROM universities
                ORDER BY name ASC
                """;
        List<University> list = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                list.add(map(results));
            }
        }
        return list;
    }

    public List<University> findActive() throws SQLException {
        String sql = """
                SELECT university_id, name, short_name, status, requested_by, approved_by, created_at, approved_at
                FROM universities
                WHERE status = 'APPROVED'
                ORDER BY name ASC
                """;
        List<University> list = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                list.add(map(results));
            }
        }
        return list;
    }

    public Optional<University> findById(long universityId) throws SQLException {
        String sql = """
                SELECT university_id, name, short_name, status, requested_by, approved_by, created_at, approved_at
                FROM universities
                WHERE university_id = ?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, universityId);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? Optional.of(map(results)) : Optional.empty();
            }
        }
    }

    public Optional<University> findByNameOrShortName(String name, String shortName) throws SQLException {
        String sql = """
                SELECT university_id, name, short_name, status, requested_by, approved_by, created_at, approved_at
                FROM universities
                WHERE LOWER(TRIM(name)) = LOWER(TRIM(?))
                   OR (short_name IS NOT NULL AND TRIM(short_name) <> '' AND LOWER(TRIM(short_name)) = LOWER(TRIM(?)))
                LIMIT 1
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, name);
            statement.setString(2, shortName == null ? "" : shortName);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? Optional.of(map(results)) : Optional.empty();
            }
        }
    }

    public long create(String name, String shortName, String status, Long adminId) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            return create(connection, name, shortName, status, adminId);
        }
    }

    public long create(Connection connection, String name, String shortName, String status, Long adminId)
            throws SQLException {
        String normalizedName = name == null ? "" : name.trim();
        String normalizedShort = (shortName == null || shortName.isBlank()) ? null : shortName.trim().toUpperCase();
        String normalizedStatus = (status == null || status.isBlank()) ? "APPROVED" : status.trim().toUpperCase();

        String sql = """
                INSERT INTO universities (name, short_name, status, requested_by, approved_by, approved_at)
                VALUES (?, ?, ?, ?, ?, CASE WHEN ? = 'APPROVED' THEN UTC_TIMESTAMP() ELSE NULL END)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, normalizedName);
            if (normalizedShort == null) {
                statement.setNull(2, java.sql.Types.VARCHAR);
            } else {
                statement.setString(2, normalizedShort);
            }
            statement.setString(3, normalizedStatus);
            if (adminId == null) {
                statement.setNull(4, java.sql.Types.BIGINT);
                statement.setNull(5, java.sql.Types.BIGINT);
            } else {
                statement.setLong(4, adminId);
                statement.setLong(5, adminId);
            }
            statement.setString(6, normalizedStatus);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        throw new SQLException("Failed to retrieve generated university identifier.");
    }

    public int updateStatus(long universityId, String newStatus, Long adminId) throws SQLException {
        String sql = """
                UPDATE universities
                SET status = ?, approved_by = ?, approved_at = CASE WHEN ? = 'APPROVED' THEN UTC_TIMESTAMP() ELSE approved_at END
                WHERE university_id = ?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, newStatus);
            if (adminId == null) {
                statement.setNull(2, java.sql.Types.BIGINT);
            } else {
                statement.setLong(2, adminId);
            }
            statement.setString(3, newStatus);
            statement.setLong(4, universityId);
            return statement.executeUpdate();
        }
    }

    public int updateMetadata(long universityId, String name, String shortName) throws SQLException {
        String normalizedName = name == null ? "" : name.trim();
        String normalizedShort = (shortName == null || shortName.isBlank()) ? null : shortName.trim().toUpperCase();
        String sql = "UPDATE universities SET name = ?, short_name = ? WHERE university_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, normalizedName);
            if (normalizedShort == null) {
                statement.setNull(2, java.sql.Types.VARCHAR);
            } else {
                statement.setString(2, normalizedShort);
            }
            statement.setLong(3, universityId);
            return statement.executeUpdate();
        }
    }

    public boolean isReferenced(long universityId) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE university_id = ? LIMIT 1";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, universityId);
            try (ResultSet results = statement.executeQuery()) {
                return results.next();
            }
        }
    }

    public boolean deleteIfNotReferenced(long universityId) throws SQLException {
        if (isReferenced(universityId)) {
            return false;
        }
        String sql = "DELETE FROM universities WHERE university_id = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, universityId);
            return statement.executeUpdate() > 0;
        }
    }

    private University map(ResultSet rs) throws SQLException {
        long id = rs.getLong("university_id");
        String name = rs.getString("name");
        String shortName = rs.getString("short_name");
        String status = rs.getString("status");
        long reqVal = rs.getLong("requested_by");
        Long requestedBy = rs.wasNull() ? null : reqVal;
        long appVal = rs.getLong("approved_by");
        Long approvedBy = rs.wasNull() ? null : appVal;
        Timestamp created = rs.getTimestamp("created_at");
        LocalDateTime createdAt = created == null ? null : created.toLocalDateTime();
        Timestamp approved = rs.getTimestamp("approved_at");
        LocalDateTime approvedAt = approved == null ? null : approved.toLocalDateTime();
        return new University(id, name, shortName, status, requestedBy, approvedBy, createdAt, approvedAt);
    }
}
