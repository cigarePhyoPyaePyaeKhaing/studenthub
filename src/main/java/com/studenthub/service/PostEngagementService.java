package com.studenthub.service;

import com.studenthub.dao.PostDAO;
import com.studenthub.dao.PostEngagementDAO;
import com.studenthub.dao.NotificationDAO;
import com.studenthub.model.Post;
import com.studenthub.model.PostComment;
import com.studenthub.dao.AttachmentDAO;
import com.studenthub.util.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class PostEngagementService {
    public record OperationResult(boolean successful, String message, long postId) {}
    public record CommentsView(Post post, List<PostComment> comments) {}
    private final PostDAO postDAO = new PostDAO();
    private final PostEngagementDAO engagementDAO = new PostEngagementDAO();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    public OperationResult toggleLike(long authenticatedUserId, long postId) throws SQLException {
        if (postDAO.findVisibleById(postId, authenticatedUserId).isEmpty()) {
            return new OperationResult(false, "NOT_FOUND", postId);
        }
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                boolean active = engagementDAO.toggleLike(connection, postId, authenticatedUserId);
                Post post = postDAO.findVisibleById(postId, authenticatedUserId).orElseThrow();
                if (active && post.authorId() != authenticatedUserId) {
                    notificationDAO.createForPost(connection,postId,authenticatedUserId,"REACTION",
                            post.title(),"Someone liked your announcement.","/posts/comments?postId="+postId,post.authorId());
                }
                connection.commit();
                return new OperationResult(true, active ? "Announcement liked." : "Reaction removed.", postId);
            } catch (SQLException exception) {
                connection.rollback(); throw exception;
            } finally { connection.setAutoCommit(true); }
        }
    }

    public Optional<CommentsView> loadComments(long authenticatedUserId, long postId) throws SQLException {
        Optional<Post> post = postDAO.findVisibleById(postId, authenticatedUserId);
        if (post.isEmpty()) return Optional.empty();
        return Optional.of(new CommentsView(post.get(), engagementDAO.findComments(postId, 200)));
    }

    public OperationResult addComment(long authenticatedUserId, long postId, String rawContent) throws SQLException {
        return addComment(authenticatedUserId,postId,rawContent,null);
    }
    public OperationResult addComment(long authenticatedUserId,long postId,String rawContent,AttachmentUpload attachment)throws SQLException{
        String error = CommentValidation.validate(rawContent,attachment!=null);
        if (error != null) return new OperationResult(false, error, postId);
        if (postDAO.findVisibleById(postId, authenticatedUserId).isEmpty()) {
            return new OperationResult(false, "NOT_FOUND", postId);
        }
        Post post=postDAO.findVisibleById(postId,authenticatedUserId).orElseThrow();
        long id;
        try(Connection connection=DBConnection.getConnection()){connection.setAutoCommit(false);try{
            id=engagementDAO.addComment(connection,postId,authenticatedUserId,CommentValidation.normalize(rawContent));
            if(id<=0)throw new SQLException("Comment identifier was unavailable after insertion.");
            if(attachment!=null)new AttachmentDAO().insert(connection,"COMMENT",id,attachment);
            if(post.authorId()!=authenticatedUserId)notificationDAO.createForPost(connection,postId,authenticatedUserId,"COMMENT",
                    post.title(),"A new comment was added to your announcement.","/posts/comments?postId="+postId,post.authorId());
            connection.commit();}catch(SQLException e){connection.rollback();throw e;}finally{connection.setAutoCommit(true);}}
        return new OperationResult(true, "Comment added.", postId);
    }

    public OperationResult deleteComment(long authenticatedUserId, Object role, long commentId) throws SQLException {
        PostEngagementDAO.CommentRecord comment = engagementDAO.findComment(commentId);
        EngagementAuthorization.DeleteDecision decision = EngagementAuthorization.commentDeleteDecision(
                comment != null, role, authenticatedUserId, comment == null ? 0 : comment.authorId());
        if (decision == EngagementAuthorization.DeleteDecision.NOT_FOUND)
            return new OperationResult(false, "NOT_FOUND", 0);
        if (decision == EngagementAuthorization.DeleteDecision.FORBIDDEN) {
            return new OperationResult(false, "FORBIDDEN", comment.postId());
        }
        if (!"ADMIN".equals(String.valueOf(role))
                && postDAO.findVisibleById(comment.postId(), authenticatedUserId).isEmpty()) {
            return new OperationResult(false, "FORBIDDEN", comment.postId());
        }
        String attachmentKey=new AttachmentDAO().findStorageKey("COMMENT",commentId);
        if(engagementDAO.deleteComment(commentId)==1){if(attachmentKey!=null)new AttachmentStorage().delete(attachmentKey);return new OperationResult(true,"Comment deleted.",comment.postId());}
        return new OperationResult(false,"NOT_FOUND",comment.postId());
    }
}
