package com.studenthub.service;

import com.studenthub.dao.AttachmentDAO;
import com.studenthub.dao.CategoryDAO;
import com.studenthub.dao.DeadlineDAO;
import com.studenthub.dao.NotificationDAO;
import com.studenthub.dao.PostDAO;
import com.studenthub.model.Post;
import com.studenthub.util.Authorization;
import com.studenthub.util.DBConnection;
import com.studenthub.util.PostValidation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class PostService {
    public record OperationResult(boolean successful, String message) {
    }

    private final PostDAO postDAO = new PostDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();
    private final DeadlineDAO deadlineDAO = new DeadlineDAO();
    private final AttachmentDAO attachmentDAO = new AttachmentDAO();

    public OperationResult create(long userId, Object role, String titleInput, String contentInput,
                                  Long categoryId, String visibilityInput) throws SQLException {
        return create(userId, role, titleInput, contentInput, categoryId, visibilityInput, null, null, List.of());
    }

    public OperationResult create(long userId, Object role, String titleInput, String contentInput,
                                  Long categoryId, String visibilityInput, LocalDateTime dueDateTime,
                                  String subjectName, List<AttachmentStorageService.StoredFileInfo> files)
            throws SQLException {
        if (!Authorization.canManagePosts(role)) return new OperationResult(false, "FORBIDDEN");
        String title = titleInput == null ? "" : titleInput.trim();
        String content = contentInput == null ? "" : contentInput.trim();
        String visibility = visibilityInput == null ? "" : visibilityInput.trim().toUpperCase(Locale.ROOT);

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Optional<String> categoryNameOpt = categoryDAO.findNameById(connection, categoryId != null ? categoryId : -1L);
                if (categoryNameOpt.isEmpty()) {
                    connection.rollback();
                    return new OperationResult(false, "Select a valid category.");
                }
                String categoryName = categoryNameOpt.get();
                String validationError = PostValidation.validate(title, content, categoryId, categoryName, visibility, dueDateTime);
                if (validationError != null) {
                    connection.rollback();
                    return new OperationResult(false, validationError);
                }
                if (!postDAO.authorHasScope(connection, userId, visibility)) {
                    connection.rollback();
                    return new OperationResult(false, "Your account does not have the semester/section details required for that visibility.");
                }
                long postId = postDAO.create(connection, userId, categoryId, title, content, visibility);
                if (postId <= 0) throw new SQLException("Post was saved but its identifier was unavailable.");

                if (files != null) {
                    for (AttachmentStorageService.StoredFileInfo file : files) {
                        attachmentDAO.create(connection, "POST", postId, file.originalFilename(),
                                file.storedFilename(), file.fileType(), file.mimeType(), file.fileSize(), userId);
                    }
                }

                if (dueDateTime != null) {
                    DeadlineDAO.AcademicScope scope = deadlineDAO.findScope(connection, userId);
                    int sem = scope.semester() != null ? scope.semester() : 1;
                    String sec = "SECTION".equalsIgnoreCase(visibility) ? scope.sectionName() : null;
                    String subj = (subjectName != null && !subjectName.isBlank()) ? subjectName.trim() : categoryName;
                    long deadlineId = deadlineDAO.create(connection, postId, title, subj, dueDateTime, sem, sec, userId);
                    if (deadlineId > 0) {
                        notificationDAO.createForDeadline(connection, deadlineId, userId, title);
                    }
                }

                notificationDAO.createForPost(connection, postId, userId, "ANNOUNCEMENT", title,
                        "A new announcement was published.", "/posts/comments?postId=" + postId, null);
                connection.commit();
                return new OperationResult(true, "Post published.");
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public Optional<Post> findById(long postId) throws SQLException {
        return postDAO.findById(postId);
    }

    public OperationResult update(long userId, Object role, long postId, String titleInput,
                                  String contentInput, Long categoryId, String visibilityInput) throws SQLException {
        return update(userId, role, postId, titleInput, contentInput, categoryId, visibilityInput, null, null, List.of());
    }

    public OperationResult update(long userId, Object role, long postId, String titleInput,
                                  String contentInput, Long categoryId, String visibilityInput,
                                  LocalDateTime dueDateTime, String subjectName,
                                  List<AttachmentStorageService.StoredFileInfo> files) throws SQLException {
        Optional<Post> found = postDAO.findById(postId);
        if (found.isEmpty()) return new OperationResult(false, "NOT_FOUND");
        if (!Authorization.canManagePost(role, userId, found.get().authorId())) return new OperationResult(false, "FORBIDDEN");

        String title = titleInput == null ? "" : titleInput.trim();
        String content = contentInput == null ? "" : contentInput.trim();
        String visibility = visibilityInput == null ? "" : visibilityInput.trim().toUpperCase(Locale.ROOT);

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Optional<String> categoryNameOpt = categoryDAO.findNameById(connection, categoryId != null ? categoryId : -1L);
                if (categoryNameOpt.isEmpty()) {
                    connection.rollback();
                    return new OperationResult(false, "Select a valid category.");
                }
                String categoryName = categoryNameOpt.get();
                String error = PostValidation.validate(title, content, categoryId, categoryName, visibility, dueDateTime);
                if (error != null) {
                    connection.rollback();
                    return new OperationResult(false, error);
                }
                if (!postDAO.authorHasScope(connection, found.get().authorId(), visibility)) {
                    connection.rollback();
                    return new OperationResult(false, "The post author does not have the academic details required for that visibility.");
                }
                if (postDAO.update(connection, postId, categoryId, title, content, visibility) != 1) {
                    connection.rollback();
                    return new OperationResult(false, "NOT_FOUND");
                }

                if (files != null) {
                    for (AttachmentStorageService.StoredFileInfo file : files) {
                        attachmentDAO.create(connection, "POST", postId, file.originalFilename(),
                                file.storedFilename(), file.fileType(), file.mimeType(), file.fileSize(), userId);
                    }
                }

                if (dueDateTime != null) {
                    Long existingDeadlineId = null;
                    try (PreparedStatement s = connection.prepareStatement("SELECT deadline_id FROM deadlines WHERE post_id=? LIMIT 1")) {
                        s.setLong(1, postId);
                        try (ResultSet r = s.executeQuery()) {
                            if (r.next()) existingDeadlineId = r.getLong(1);
                        }
                    }
                    DeadlineDAO.AcademicScope scope = deadlineDAO.findScope(connection, found.get().authorId());
                    int sem = scope.semester() != null ? scope.semester() : 1;
                    String sec = "SECTION".equalsIgnoreCase(visibility) ? scope.sectionName() : null;
                    String subj = (subjectName != null && !subjectName.isBlank()) ? subjectName.trim() : categoryName;

                    if (existingDeadlineId != null) {
                        deadlineDAO.update(connection, existingDeadlineId, postId, title, subj, dueDateTime, sem, sec);
                    } else {
                        deadlineDAO.create(connection, postId, title, subj, dueDateTime, sem, sec, found.get().authorId());
                    }
                }

                connection.commit();
                return new OperationResult(true, "Post updated.");
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public OperationResult delete(long userId, Object role, long postId) throws SQLException {
        Optional<Post> found = postDAO.findById(postId);
        if (found.isEmpty()) return new OperationResult(false, "NOT_FOUND");
        if (!Authorization.canManagePost(role, userId, found.get().authorId())) return new OperationResult(false, "FORBIDDEN");
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (postDAO.deleteRelatedAndPost(connection, postId) != 1) {
                    connection.rollback();
                    return new OperationResult(false, "NOT_FOUND");
                }
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
        return new OperationResult(true, "Post deleted.");
    }
}
