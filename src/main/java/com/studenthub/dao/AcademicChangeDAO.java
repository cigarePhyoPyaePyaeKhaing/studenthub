package com.studenthub.dao;

import com.studenthub.util.AcademicGroupPolicy;

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

        public long getRequestId() { return requestId; }
        public long getUserId() { return userId; }
        public String getStudentId() { return studentId; }
        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
        public Integer getOldSemester() { return oldSemester; }
        public String getOldSection() { return oldSection; }
        public int getRequestedSemester() { return requestedSemester; }
        public String getRequestedSection() { return requestedSection; }
        public String getReason() { return reason; }
        public String getStatus() { return status; }
        public String getAdminNote() { return adminNote; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getReviewedAt() { return reviewedAt; }

        public String getCreatedLabel() {
            return createdAt == null ? "" : createdAt.format(FORMATTER);
        }

        public String getReviewedLabel() {
            return reviewedAt == null ? "" : reviewedAt.format(FORMATTER);
        }
    }

    private final NotificationDAO notificationDAO;

    public AcademicChangeDAO() {
        this(new NotificationDAO());
    }

    public AcademicChangeDAO(NotificationDAO notificationDAO) {
        this.notificationDAO = notificationDAO != null ? notificationDAO : new NotificationDAO();
    }

    public boolean hasPending(long userId) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            return hasPending(c, userId);
        }
    }

    public boolean hasPending(Connection c, long userId) throws SQLException {
        String sql = "SELECT 1 FROM academic_change_requests WHERE user_id = ? AND status = 'PENDING'";
        try (PreparedStatement s = c.prepareStatement(sql)) {
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
        try (Connection c = DBConnection.getConnection()) {
            create(c, userId, semester, section, reason);
        }
    }

    public void create(Connection c, long userId, int semester, String section, String reason) throws SQLException {
        if (hasPending(c, userId)) {
            throw new IllegalStateException("You already have a pending academic change request.");
        }
        String sql = """
                INSERT INTO academic_change_requests
                    (user_id, old_semester, old_section, requested_semester, requested_section, reason)
                SELECT user_id, semester, section_name, ?, ?, ?
                FROM users WHERE user_id = ?
                """;
        try (PreparedStatement s = c.prepareStatement(sql)) {
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

        // Notify admins after request is safely created
        try {
            String userQuery = "SELECT full_name, student_id, semester, section_name FROM users WHERE user_id = ?";
            String fullName = null;
            String studentId = null;
            Integer oldSem = null;
            String oldSec = null;
            try (PreparedStatement userStmt = c.prepareStatement(userQuery)) {
                userStmt.setLong(1, userId);
                try (ResultSet userRs = userStmt.executeQuery()) {
                    if (userRs.next()) {
                        fullName = userRs.getString("full_name");
                        studentId = userRs.getString("student_id");
                        int os = userRs.getInt("semester");
                        oldSem = userRs.wasNull() ? null : os;
                        oldSec = userRs.getString("section_name");
                    }
                }
            }

            StringBuilder msg = new StringBuilder();
            if (fullName != null && !fullName.isBlank()) {
                msg.append(fullName.trim());
                if (studentId != null && !studentId.isBlank()) {
                    msg.append(" (").append(studentId.trim()).append(")");
                }
            } else if (studentId != null && !studentId.isBlank()) {
                msg.append(studentId.trim());
            } else {
                msg.append("A student");
            }

            if (oldSem != null && oldSec != null && !oldSec.isBlank()) {
                msg.append(" requested to change Semester ").append(oldSem).append(" / Section ").append(oldSec)
                        .append(" to Semester ").append(semester).append(" / Section ").append(section).append(".");
            } else {
                msg.append(" requested Semester ").append(semester).append(" / Section ").append(section).append(".");
            }

            notificationDAO.createForAdminRole(c, userId, "ACADEMIC_CHANGE_REQUEST", "Academic Change Request",
                    msg.toString(), "/admin/academic-changes?status=PENDING");
        } catch (Exception notifEx) {
            System.err.println("Admin notification creation skipped: " + notifEx.getMessage());
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
                String normalizedGroup = AcademicGroupPolicy.normalize(semester, section);
                if (normalizedGroup == null) {
                    throw new SQLException("Academic change request contains an invalid semester/group combination.", "22023");
                }
                section = normalizedGroup;
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

            // Create notification for requesting student
            String title = approve ? "Academic Change Request Approved" : "Academic Change Request Rejected";
            String type = approve ? "ACADEMIC_CHANGE_APPROVED" : "ACADEMIC_CHANGE_REJECTED";
            String message;
            if (approve) {
                message = "Your academic information change request was approved. Your semester and "
                        + AcademicGroupPolicy.groupLabel(semester).toLowerCase(java.util.Locale.ROOT) + " have been updated.";
            } else {
                if (note != null && !note.isBlank()) {
                    message = "Your academic information change request was rejected. Admin note: " + note.trim();
                } else {
                    message = "Your academic information change request was rejected.";
                }
            }
            try {
                notificationDAO.createDirect(c, userId, adminId, type, title, message, "/profile");
            } catch (Exception notifEx) {
                System.err.println("Student notification creation skipped: " + notifEx.getMessage());
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
