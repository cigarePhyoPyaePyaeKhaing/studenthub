package com.studenthub.dao;

import com.studenthub.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AcademicChangeDAO {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public record Item(
            long requestId,
            long userId,
            String studentId,
            String fullName,
            String email,
            Integer oldSemester,
            String oldSection,
            int requestedSemester,
            String requestedSection,
            String reason,
            String status,
            String adminNote,
            LocalDateTime createdAt,
            LocalDateTime reviewedAt) {

        public String getCreatedLabel() {
            return createdAt == null ? "" : createdAt.format(FORMATTER);
        }

        public String getReviewedLabel() {
            return reviewedAt == null ? "" : reviewedAt.format(FORMATTER);
        }
    }

    public boolean hasPending(long userId) throws SQLException {
        String sql = "SELECT 1 FROM academic_change_requests WHERE user_id = ? AND status = 'PENDING'";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setLong(1, userId);
            try (ResultSet r = s.executeQuery()) {
                return r.next();
            }
        }
    }

    public Optional<Item> findPendingForUser(long userId) throws SQLException {
        String sql = """
                SELECT a.request_id, a.user_id, u.student_id, u.full_name, u.email,
                       a.old_semester, a.old_section, a.requested_semester, a.requested_section,
                       a.reason, a.status, a.admin_note, a.created_at, a.reviewed_at
                FROM academic_change_requests a
                JOIN users u ON u.user_id = a.user_id
                WHERE a.user_id = ? AND a.status = 'PENDING'
                ORDER BY a.created_at DESC LIMIT 1
                """;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setLong(1, userId);
            try (ResultSet r = s.executeQuery()) {
                return r.next() ? Optional.of(map(r)) : Optional.empty();
            }
        }
    }

    public void create(long userId, int semester, String section, String reason) throws SQLException {
        if (hasPending(userId)) {
            throw new IllegalStateException("You already have a pending academic change request.");
        }
        String sql = """
                INSERT INTO academic_change_requests
                    (user_id, old_semester, old_section, requested_semester, requested_section, reason)
                SELECT user_id, semester, section_name, ?, ?, ?
                FROM users WHERE user_id = ?
                """;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, semester);
            s.setString(2, section);
            s.setString(3, reason);
            s.setLong(4, userId);
            try {
                if (s.executeUpdate() != 1) {
                    throw new IllegalStateException("Account not found.");
                }
            } catch (SQLException e) {
                if (e.getErrorCode() == 1062) {
                    throw new IllegalStateException("You already have a pending academic change request.");
                }
                throw e;
            }
        }
    }

    public List<Item> pending() throws SQLException {
        return list("WHERE a.status = 'PENDING' ORDER BY a.created_at DESC", 50);
    }

    public List<Item> listByStatus(String status) throws SQLException {
        if (status == null || status.isBlank() || "PENDING".equalsIgnoreCase(status)) {
            return pending();
        }
        if ("APPROVED".equalsIgnoreCase(status) || "REJECTED".equalsIgnoreCase(status)) {
            return list("WHERE a.status = '" + status.toUpperCase() + "' ORDER BY a.created_at DESC", 50);
        }
        return list("ORDER BY a.created_at DESC", 100);
    }

    private List<Item> list(String where, int limit) throws SQLException {
        String sql = "SELECT a.request_id, a.user_id, u.student_id, u.full_name, u.email, "
                + "a.old_semester, a.old_section, a.requested_semester, a.requested_section, "
                + "a.reason, a.status, a.admin_note, a.created_at, a.reviewed_at "
                + "FROM academic_change_requests a JOIN users u ON u.user_id = a.user_id "
                + where + " LIMIT ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, limit);
            try (ResultSet r = s.executeQuery()) {
                List<Item> items = new ArrayList<>();
                while (r.next()) {
                    items.add(map(r));
                }
                return items;
            }
        }
    }

    public boolean review(long requestId, long adminId, boolean approve, String note) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            return review(c, requestId, adminId, approve, note);
        }
    }

    public boolean review(Connection c, long requestId, long adminId, boolean approve, String note) throws SQLException {
        c.setAutoCommit(false);
        try {
            long userId;
            int semester;
            String section;
            try (PreparedStatement s = c.prepareStatement(
                    "SELECT user_id, requested_semester, requested_section FROM academic_change_requests WHERE request_id = ? AND status = 'PENDING' FOR UPDATE")) {
                s.setLong(1, requestId);
                try (ResultSet r = s.executeQuery()) {
                    if (!r.next()) {
                        c.rollback();
                        return false;
                    }
                    userId = r.getLong(1);
                    semester = r.getInt(2);
                    section = r.getString(3);
                }
            }
            if (approve) {
                try (PreparedStatement s = c.prepareStatement(
                        "UPDATE users SET semester = ?, section_name = ? WHERE user_id = ?")) {
                    s.setInt(1, semester);
                    s.setString(2, section);
                    s.setLong(3, userId);
                    s.executeUpdate();
                }
            }
            try (PreparedStatement s = c.prepareStatement(
                    "UPDATE academic_change_requests SET status = ?, reviewed_by = ?, reviewed_at = UTC_TIMESTAMP(), admin_note = ? WHERE request_id = ?")) {
                s.setString(1, approve ? "APPROVED" : "REJECTED");
                s.setLong(2, adminId);
                s.setString(3, note != null && !note.isBlank() ? note.trim() : null);
                s.setLong(4, requestId);
                s.executeUpdate();
            }
            c.commit();
            return true;
        } catch (SQLException e) {
            c.rollback();
            throw e;
        } finally {
            c.setAutoCommit(true);
        }
    }

    private Item map(ResultSet r) throws SQLException {
        int oldSem = r.getInt("old_semester");
        Integer oldSemester = r.wasNull() ? null : oldSem;
        Timestamp createdAtTs = r.getTimestamp("created_at");
        Timestamp reviewedAtTs = r.getTimestamp("reviewed_at");
        return new Item(
                r.getLong("request_id"),
                r.getLong("user_id"),
                r.getString("student_id"),
                r.getString("full_name"),
                r.getString("email"),
                oldSemester,
                r.getString("old_section"),
                r.getInt("requested_semester"),
                r.getString("requested_section"),
                r.getString("reason"),
                r.getString("status"),
                r.getString("admin_note"),
                createdAtTs == null ? null : createdAtTs.toLocalDateTime(),
                reviewedAtTs == null ? null : reviewedAtTs.toLocalDateTime());
    }
}