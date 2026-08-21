package com.studenthub.dao;

import com.studenthub.model.PostComment;
import com.studenthub.model.Role;
import com.studenthub.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostEngagementDAO {
    public record CommentRecord(long commentId, long postId, long authorId) {}

    public boolean toggleLike(Connection connection, long postId, long authenticatedUserId) throws SQLException {
        String remove = "DELETE FROM reactions WHERE post_id=? AND user_id=? AND reaction_type='LIKE'";
        try (PreparedStatement statement = connection.prepareStatement(remove)) {
            statement.setLong(1, postId); statement.setLong(2, authenticatedUserId);
            if (statement.executeUpdate() == 1) return false;
        }
        String add = "INSERT INTO reactions(post_id,user_id,reaction_type) VALUES(?,?,'LIKE')";
        try (PreparedStatement statement = connection.prepareStatement(add)) {
            statement.setLong(1, postId); statement.setLong(2, authenticatedUserId);
            statement.executeUpdate();
            return true;
        }
    }

    public List<PostComment> findComments(long postId, int limit) throws SQLException {
        String sql = """
                SELECT cm.comment_id,cm.post_id,cm.user_id,cm.content,cm.created_at,
                       author.name AS full_name,author.role
                FROM comments cm JOIN users author ON author.id=cm.user_id
                WHERE cm.post_id=? ORDER BY cm.created_at ASC,cm.comment_id ASC LIMIT ?
                """;
        List<PostComment> comments = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, postId); statement.setInt(2, Math.max(1, Math.min(limit, 200)));
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) comments.add(new PostComment(result.getLong("comment_id"),
                        result.getLong("post_id"), result.getLong("user_id"),
                        result.getString("full_name"), Role.valueOf(result.getString("role")),
                        result.getString("content"), result.getTimestamp("created_at").toLocalDateTime()));
            }
        }
        return comments;
    }

    public long addComment(long postId, long authenticatedUserId, String content) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) { return addComment(connection,postId,authenticatedUserId,content); }
    }
    public long addComment(Connection connection,long postId,long authenticatedUserId,String content)throws SQLException{
        String sql = "INSERT INTO comments(post_id,user_id,content) VALUES(?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, postId); statement.setLong(2, authenticatedUserId); statement.setString(3, content);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) { return keys.next() ? keys.getLong(1) : 0; }
        }
    }

    public CommentRecord findComment(long commentId) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT comment_id,post_id,user_id FROM comments WHERE comment_id=?")) {
            statement.setLong(1, commentId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? new CommentRecord(commentId, result.getLong("post_id"),
                        result.getLong("user_id")) : null;
            }
        }
    }

    public int deleteComment(long commentId) throws SQLException {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM comments WHERE comment_id=?")) {
            statement.setLong(1, commentId); return statement.executeUpdate();
        }
    }
}
