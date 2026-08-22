package com.studenthub.service;

import com.studenthub.dao.CategoryDAO;
import com.studenthub.dao.PostDAO;
import com.studenthub.dao.NotificationDAO;
import com.studenthub.util.DBConnection;
import com.studenthub.util.PostValidation;
import com.studenthub.util.Authorization;
import com.studenthub.model.Post;
import com.studenthub.dao.AttachmentDAO;
import com.studenthub.util.AttachmentUpload;
import java.sql.Connection;
import java.sql.SQLException;

public class PostService {
    public record OperationResult(boolean successful, String message, long postId) {
        public OperationResult(boolean successful,String message){this(successful,message,0);}
    }

    private final PostDAO postDAO = new PostDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    public OperationResult create(long userId, Object role, String titleInput, String contentInput,
                                  Long categoryId, String visibilityInput)
            throws SQLException {
        return create(userId, role, titleInput, contentInput, categoryId, visibilityInput, null);
    }

    public OperationResult create(long userId, Object role, String titleInput, String contentInput,
                                  Long categoryId, String visibilityInput, String deadlineDateInput)
            throws SQLException {
        return create(userId,role,titleInput,contentInput,categoryId,visibilityInput,deadlineDateInput,null);
    }
    public OperationResult create(long userId,Object role,String titleInput,String contentInput,Long categoryId,String visibilityInput,String deadlineDateInput,AttachmentUpload attachment)throws SQLException {
        if (!Authorization.canManagePosts(role)) return new OperationResult(false, "FORBIDDEN");
        String title = titleInput == null ? "" : titleInput.trim();
        String content = contentInput == null ? "" : contentInput.trim();
        String visibility = visibilityInput == null ? "" : visibilityInput.trim().toUpperCase(java.util.Locale.ROOT);
        String categoryName = categoryId != null ? categoryDAO.findNameById(categoryId) : null;
        String validationError = PostValidation.validate(title, content, categoryId, categoryName, visibility, deadlineDateInput, attachment!=null);
        if (validationError != null) return new OperationResult(false, validationError);
        java.time.LocalDateTime deadlineDate = PostValidation.parseDeadline(deadlineDateInput);
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (!categoryDAO.exists(connection, categoryId)) {
                    connection.rollback(); return new OperationResult(false, "Select a valid category.");
                }
                if (!postDAO.authorHasScope(connection, userId, visibility)) {
                    connection.rollback(); return new OperationResult(false, "Your account does not have the semester/section details required for that visibility.");
                }
                long postId = postDAO.create(connection, userId, categoryId, title, content, visibility, deadlineDate);
                if (postId <= 0) throw new SQLException("Post was saved but its identifier was unavailable.");
                if(attachment!=null)new AttachmentDAO().insert(connection,"POST",postId,attachment);
                String notifType = deadlineDate != null ? "DEADLINE" : "ANNOUNCEMENT";
                String notifMessage = deadlineDate != null ? "A new deadline announcement was published." : "A new announcement was published.";
                notificationDAO.createForPost(connection, postId, userId, notifType, title,
                        notifMessage, "/posts/comments?postId=" + postId, null);
                connection.commit();
                return new OperationResult(true,"Post published.",postId);
            } catch (SQLException exception) {
                connection.rollback(); throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public java.util.Optional<Post> findById(long postId) throws SQLException { return postDAO.findById(postId); }

    public OperationResult update(long userId, Object role, long postId, String titleInput,
                                  String contentInput, Long categoryId, String visibilityInput) throws SQLException {
        return update(userId, role, postId, titleInput, contentInput, categoryId, visibilityInput, null);
    }

    public OperationResult update(long userId, Object role, long postId, String titleInput,
                                  String contentInput, Long categoryId, String visibilityInput,
                                  String deadlineDateInput) throws SQLException {
        java.util.Optional<Post> found = postDAO.findById(postId);
        if (found.isEmpty()) return new OperationResult(false, "NOT_FOUND");
        if (!Authorization.canManagePost(role, userId, found.get().authorId())) return new OperationResult(false, "FORBIDDEN");
        String title = titleInput == null ? "" : titleInput.trim();
        String content = contentInput == null ? "" : contentInput.trim();
        String visibility = visibilityInput == null ? "" : visibilityInput.trim().toUpperCase(java.util.Locale.ROOT);
        String categoryName = categoryId != null ? categoryDAO.findNameById(categoryId) : null;
        String error = PostValidation.validate(title, content, categoryId, categoryName, visibility, deadlineDateInput);
        if (error != null) return new OperationResult(false, error);
        java.time.LocalDateTime deadlineDate = PostValidation.parseDeadline(deadlineDateInput);
        try (Connection connection = DBConnection.getConnection()) {
            if (!categoryDAO.exists(connection, categoryId)) return new OperationResult(false, "Select a valid category.");
            if (!postDAO.authorHasScope(connection, found.get().authorId(), visibility)) {
                return new OperationResult(false, "The post author does not have the academic details required for that visibility.");
            }
            if (postDAO.update(connection, postId, categoryId, title, content, visibility, deadlineDate) != 1) {
                return new OperationResult(false, "NOT_FOUND");
            }
        }
        return new OperationResult(true, "Post updated.");
    }

    public OperationResult delete(long userId, Object role, long postId) throws SQLException {
        java.util.Optional<Post> found = postDAO.findById(postId);
        if (found.isEmpty()) return new OperationResult(false, "NOT_FOUND");
        if (!Authorization.canManagePost(role, userId, found.get().authorId())) return new OperationResult(false, "FORBIDDEN");
        String attachmentKey=new AttachmentDAO().findStorageKey("POST",postId);
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (postDAO.deleteRelatedAndPost(connection, postId) != 1) {
                    connection.rollback(); return new OperationResult(false, "NOT_FOUND");
                }
                connection.commit();
                if(attachmentKey!=null)new com.studenthub.util.AttachmentStorage().delete(attachmentKey);
            } catch (SQLException exception) {
                connection.rollback(); throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
        return new OperationResult(true, "Post deleted.");
    }
}
