package com.studenthub.service;

import com.studenthub.dao.CategoryDAO;
import com.studenthub.dao.PostDAO;
import com.studenthub.dao.NotificationDAO;
import com.studenthub.util.DBConnection;
import com.studenthub.util.PostValidation;
import com.studenthub.util.Authorization;
import com.studenthub.model.Post;
import java.sql.Connection;
import java.sql.SQLException;

public class PostService {
    public record OperationResult(boolean successful, String message) {
    }

    private final PostDAO postDAO = new PostDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    public OperationResult create(long userId, Object role, String titleInput, String contentInput,
                                  Long categoryId, String visibilityInput)
            throws SQLException {
        if (!Authorization.canManagePosts(role)) return new OperationResult(false, "FORBIDDEN");
        String title = titleInput == null ? "" : titleInput.trim();
        String content = contentInput == null ? "" : contentInput.trim();
        String visibility = visibilityInput == null ? "" : visibilityInput.trim().toUpperCase(java.util.Locale.ROOT);
        String validationError = PostValidation.validate(title, content, categoryId, visibility);
        if (validationError != null) return new OperationResult(false, validationError);
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                if (!categoryDAO.exists(connection, categoryId)) {
                    connection.rollback(); return new OperationResult(false, "Select a valid category.");
                }
                if (!postDAO.authorHasScope(connection, userId, visibility)) {
                    connection.rollback(); return new OperationResult(false, "Your account does not have the semester/section details required for that visibility.");
                }
                long postId = postDAO.create(connection, userId, categoryId, title, content, visibility);
                if (postId <= 0) throw new SQLException("Post was saved but its identifier was unavailable.");
                notificationDAO.createForPost(connection, postId, userId, "ANNOUNCEMENT", title,
                        "A new announcement was published.", "/posts/comments?postId=" + postId, null);
                connection.commit();
            } catch (SQLException exception) {
                connection.rollback(); throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
        return new OperationResult(true, "Post published.");
    }

    public java.util.Optional<Post> findById(long postId) throws SQLException { return postDAO.findById(postId); }

    public OperationResult update(long userId, Object role, long postId, String titleInput,
                                  String contentInput, Long categoryId, String visibilityInput) throws SQLException {
        java.util.Optional<Post> found = postDAO.findById(postId);
        if (found.isEmpty()) return new OperationResult(false, "NOT_FOUND");
        if (!Authorization.canManagePost(role, userId, found.get().authorId())) return new OperationResult(false, "FORBIDDEN");
        String title=titleInput==null?"":titleInput.trim(); String content=contentInput==null?"":contentInput.trim();
        String visibility=visibilityInput==null?"":visibilityInput.trim().toUpperCase(java.util.Locale.ROOT);
        String error=PostValidation.validate(title,content,categoryId,visibility); if(error!=null)return new OperationResult(false,error);
        try(Connection connection=DBConnection.getConnection()){
            if(!categoryDAO.exists(connection,categoryId))return new OperationResult(false,"Select a valid category.");
            if(!postDAO.authorHasScope(connection,found.get().authorId(),visibility))return new OperationResult(false,"The post author does not have the academic details required for that visibility.");
            if(postDAO.update(connection,postId,categoryId,title,content,visibility)!=1)return new OperationResult(false,"NOT_FOUND");
        }
        return new OperationResult(true,"Post updated.");
    }

    public OperationResult delete(long userId,Object role,long postId)throws SQLException{
        java.util.Optional<Post> found=postDAO.findById(postId);if(found.isEmpty())return new OperationResult(false,"NOT_FOUND");
        if(!Authorization.canManagePost(role,userId,found.get().authorId()))return new OperationResult(false,"FORBIDDEN");
        try(Connection connection=DBConnection.getConnection()){connection.setAutoCommit(false);try{if(postDAO.deleteRelatedAndPost(connection,postId)!=1){connection.rollback();return new OperationResult(false,"NOT_FOUND");}connection.commit();}catch(SQLException exception){connection.rollback();throw exception;}finally{connection.setAutoCommit(true);}}
        return new OperationResult(true,"Post deleted.");
    }
}
