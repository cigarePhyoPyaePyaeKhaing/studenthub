package com.studenthub.dao;

import com.studenthub.model.Attachment;
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

public class AttachmentDAO {

    public long create(Connection connection, String entityType, long entityId,
                       String originalFilename, String storedFilename, String fileType,
                       String mimeType, long fileSize, long uploaderId) throws SQLException {
        String sql = """
                INSERT INTO attachments
                    (entity_type, entity_id, original_filename, stored_filename, file_type, mime_type, file_size, uploader_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, entityType);
            statement.setLong(2, entityId);
            statement.setString(3, originalFilename);
            statement.setString(4, storedFilename);
            statement.setString(5, fileType);
            statement.setString(6, mimeType);
            statement.setLong(7, fileSize);
            statement.setLong(8, uploaderId);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        throw new SQLException("Failed to retrieve generated attachment ID.");
    }

    public List<Attachment> findByEntity(String entityType, long entityId) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            return findByEntity(connection, entityType, entityId);
        }
    }

    public List<Attachment> findByEntity(Connection connection, String entityType, long entityId) throws SQLException {
        String sql = """
                SELECT attachment_id, entity_type, entity_id, original_filename, stored_filename,
                       file_type, mime_type, file_size, uploader_id, created_at
                FROM attachments
                WHERE entity_type = ? AND entity_id = ?
                ORDER BY attachment_id ASC
                """;
        List<Attachment> list = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, entityType);
            statement.setLong(2, entityId);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    list.add(map(results));
                }
            }
        }
        return list;
    }

    public Optional<Attachment> findById(long attachmentId) throws SQLException {
        String sql = """
                SELECT attachment_id, entity_type, entity_id, original_filename, stored_filename,
                       file_type, mime_type, file_size, uploader_id, created_at
                FROM attachments
                WHERE attachment_id = ?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, attachmentId);
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? Optional.of(map(results)) : Optional.empty();
            }
        }
    }

    public int deleteByEntity(Connection connection, String entityType, long entityId) throws SQLException {
        String sql = "DELETE FROM attachments WHERE entity_type = ? AND entity_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, entityType);
            statement.setLong(2, entityId);
            return statement.executeUpdate();
        }
    }

    private Attachment map(ResultSet rs) throws SQLException {
        Timestamp created = rs.getTimestamp("created_at");
        LocalDateTime createdAt = created == null ? null : created.toLocalDateTime();
        return new Attachment(
                rs.getLong("attachment_id"),
                rs.getString("entity_type"),
                rs.getLong("entity_id"),
                rs.getString("original_filename"),
                rs.getString("stored_filename"),
                rs.getString("file_type"),
                rs.getString("mime_type"),
                rs.getLong("file_size"),
                rs.getLong("uploader_id"),
                createdAt);
    }
}
