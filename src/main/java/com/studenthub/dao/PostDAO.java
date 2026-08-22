package com.studenthub.dao;

import com.studenthub.model.Post;
import com.studenthub.model.Role;
import com.studenthub.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import com.studenthub.util.PostDeletionPlan;

public class PostDAO {
    public List<Post> findVisibleForUser(long viewerId, Long categoryId) throws SQLException {
        return findVisibleForUser(viewerId, categoryId, 30);
    }

    public List<Post> findVisibleForUser(long viewerId, Long categoryId, int limit) throws SQLException {
        String sql = """
                SELECT p.post_id, p.user_id, p.category_id, p.title, p.content, p.image_url, p.visibility, p.created_at,
                       p.deadline_date,
                       author.full_name AS author_name, author.role AS author_role,
                       c.category_name,
                       (SELECT COUNT(*) FROM reactions r WHERE r.post_id = p.post_id) AS reaction_count,
                       (SELECT COUNT(*) FROM comments cm WHERE cm.post_id = p.post_id) AS comment_count,
                       EXISTS(SELECT 1 FROM reactions mine WHERE mine.post_id = p.post_id
                              AND mine.user_id = ? AND mine.reaction_type = 'LIKE') AS current_user_reacted
                FROM posts p
                JOIN users author ON author.user_id = p.user_id
                JOIN users viewer ON viewer.user_id = ?
                LEFT JOIN categories c ON c.category_id = p.category_id
                WHERE (p.visibility = 'ALL'
                       OR (p.visibility = 'SEMESTER' AND author.semester = viewer.semester)
                       OR (p.visibility = 'SECTION' AND author.semester = viewer.semester
                           AND author.section_name = viewer.section_name))
                  AND (? IS NULL OR p.category_id = ?)
                ORDER BY p.created_at DESC, p.post_id DESC
                LIMIT ?
                """;
        List<Post> posts = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, viewerId);
            statement.setLong(2, viewerId);
            if (categoryId == null) {
                statement.setNull(3, java.sql.Types.BIGINT);
                statement.setNull(4, java.sql.Types.BIGINT);
            } else {
                statement.setLong(3, categoryId);
                statement.setLong(4, categoryId);
            }
            statement.setInt(5, Math.max(1, limit));
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    posts.add(map(result));
                }
            }
        }
        return posts;
    }

    public List<Post> findUpcomingDeadlinesForUser(long viewerId, int limit) throws SQLException {
        String sql = """
                SELECT p.post_id, p.user_id, p.category_id, p.title, p.content, p.image_url, p.visibility, p.created_at,
                       p.deadline_date,
                       author.full_name AS author_name, author.role AS author_role,
                       c.category_name,
                       (SELECT COUNT(*) FROM reactions r WHERE r.post_id = p.post_id) AS reaction_count,
                       (SELECT COUNT(*) FROM comments cm WHERE cm.post_id = p.post_id) AS comment_count,
                       EXISTS(SELECT 1 FROM reactions mine WHERE mine.post_id = p.post_id
                              AND mine.user_id = ? AND mine.reaction_type = 'LIKE') AS current_user_reacted
                FROM posts p
                JOIN users author ON author.user_id = p.user_id
                JOIN users viewer ON viewer.user_id = ?
                LEFT JOIN categories c ON c.category_id = p.category_id
                WHERE p.deadline_date IS NOT NULL
                  AND p.deadline_date >= CURRENT_TIMESTAMP
                  AND (p.visibility = 'ALL'
                       OR (p.visibility = 'SEMESTER' AND author.semester = viewer.semester)
                       OR (p.visibility = 'SECTION' AND author.semester = viewer.semester
                           AND author.section_name = viewer.section_name))
                ORDER BY p.deadline_date ASC, p.post_id ASC
                LIMIT ?
                """;
        List<Post> posts = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, viewerId);
            statement.setLong(2, viewerId);
            statement.setInt(3, Math.max(1, limit));
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    posts.add(map(result));
                }
            }
        }
        return posts;
    }

    public List<Post> findPastDeadlinesForUser(long viewerId, int limit) throws SQLException {
        String sql = """
                SELECT p.post_id, p.user_id, p.category_id, p.title, p.content, p.image_url, p.visibility, p.created_at,
                       p.deadline_date,
                       author.full_name AS author_name, author.role AS author_role,
                       c.category_name,
                       (SELECT COUNT(*) FROM reactions r WHERE r.post_id = p.post_id) AS reaction_count,
                       (SELECT COUNT(*) FROM comments cm WHERE cm.post_id = p.post_id) AS comment_count,
                       EXISTS(SELECT 1 FROM reactions mine WHERE mine.post_id = p.post_id
                              AND mine.user_id = ? AND mine.reaction_type = 'LIKE') AS current_user_reacted
                FROM posts p
                JOIN users author ON author.user_id = p.user_id
                JOIN users viewer ON viewer.user_id = ?
                LEFT JOIN categories c ON c.category_id = p.category_id
                WHERE p.deadline_date IS NOT NULL
                  AND p.deadline_date < CURRENT_TIMESTAMP
                  AND (p.visibility = 'ALL'
                       OR (p.visibility = 'SEMESTER' AND author.semester = viewer.semester)
                       OR (p.visibility = 'SECTION' AND author.semester = viewer.semester
                           AND author.section_name = viewer.section_name))
                ORDER BY p.deadline_date DESC, p.post_id DESC
                LIMIT ?
                """;
        List<Post> posts = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, viewerId);
            statement.setLong(2, viewerId);
            statement.setInt(3, Math.max(1, limit));
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    posts.add(map(result));
                }
            }
        }
        return posts;
    }

    public long create(Connection connection, long userId, long categoryId, String title, String content,
                       String visibility)
            throws SQLException {
        return create(connection, userId, categoryId, title, content, visibility, null);
    }

    public long create(Connection connection, long userId, long categoryId, String title, String content,
                       String visibility, java.time.LocalDateTime deadlineDate)
            throws SQLException {
        String sql = "INSERT INTO posts (user_id, category_id, title, content, visibility, deadline_date) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql,
                java.sql.Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, userId);
            statement.setLong(2, categoryId);
            statement.setString(3, title);
            statement.setString(4, content);
            statement.setString(5, visibility);
            if (deadlineDate == null) {
                statement.setNull(6, java.sql.Types.TIMESTAMP);
            } else {
                statement.setTimestamp(6, Timestamp.valueOf(deadlineDate));
            }
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : 0;
            }
        }
    }

    public java.util.Optional<Post> findById(long postId) throws SQLException {
        String sql = """
                SELECT p.post_id, p.user_id, p.category_id, p.title, p.content, p.image_url,
                       p.visibility, p.created_at, p.deadline_date,
                       author.full_name AS author_name, author.role AS author_role, c.category_name,
                       (SELECT COUNT(*) FROM reactions r WHERE r.post_id=p.post_id) reaction_count,
                       (SELECT COUNT(*) FROM comments cm WHERE cm.post_id=p.post_id) comment_count,
                       FALSE AS current_user_reacted
                FROM posts p JOIN users author ON author.user_id=p.user_id
                LEFT JOIN categories c ON c.category_id=p.category_id WHERE p.post_id=?
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, postId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? java.util.Optional.of(map(result)) : java.util.Optional.empty();
            }
        }
    }

    public java.util.Optional<Post> findVisibleById(long postId, long viewerId) throws SQLException {
        String sql = """
                SELECT p.post_id, p.user_id, p.category_id, p.title, p.content, p.image_url,
                       p.visibility, p.created_at, p.deadline_date,
                       author.full_name AS author_name, author.role AS author_role, c.category_name,
                       (SELECT COUNT(*) FROM reactions r WHERE r.post_id=p.post_id) reaction_count,
                       (SELECT COUNT(*) FROM comments cm WHERE cm.post_id=p.post_id) comment_count,
                       EXISTS(SELECT 1 FROM reactions mine WHERE mine.post_id=p.post_id
                              AND mine.user_id=? AND mine.reaction_type='LIKE') current_user_reacted
                FROM posts p
                JOIN users author ON author.user_id=p.user_id
                JOIN users viewer ON viewer.user_id=?
                LEFT JOIN categories c ON c.category_id=p.category_id
                WHERE p.post_id=? AND (p.visibility='ALL'
                    OR (p.visibility='SEMESTER' AND author.semester=viewer.semester)
                    OR (p.visibility='SECTION' AND author.semester=viewer.semester
                        AND author.section_name=viewer.section_name))
                """;
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, viewerId); statement.setLong(2, viewerId); statement.setLong(3, postId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? java.util.Optional.of(map(result)) : java.util.Optional.empty();
            }
        }
    }

    public int update(Connection connection, long postId, long categoryId, String title,
                      String content, String visibility) throws SQLException {
        return update(connection, postId, categoryId, title, content, visibility, null);
    }

    public int update(Connection connection, long postId, long categoryId, String title,
                      String content, String visibility, java.time.LocalDateTime deadlineDate) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE posts SET category_id=?, title=?, content=?, visibility=?, deadline_date=? WHERE post_id=?")) {
            statement.setLong(1, categoryId); statement.setString(2, title);
            statement.setString(3, content); statement.setString(4, visibility);
            if (deadlineDate == null) {
                statement.setNull(5, java.sql.Types.TIMESTAMP);
            } else {
                statement.setTimestamp(5, Timestamp.valueOf(deadlineDate));
            }
            statement.setLong(6, postId);
            return statement.executeUpdate();
        }
    }

    public int deleteRelatedAndPost(Connection connection, long postId) throws SQLException {
        for (String statement : PostDeletionPlan.relatedStatements()) execute(connection, statement, postId);
        return execute(connection, "DELETE FROM posts WHERE post_id=?", postId);
    }

    public boolean authorHasScope(Connection connection, long userId, String visibility) throws SQLException {
        if ("ALL".equals(visibility)) return true;
        String sql = "SECTION".equals(visibility)
                ? "SELECT 1 FROM users WHERE user_id=? AND semester IS NOT NULL AND section_name IS NOT NULL"
                : "SELECT 1 FROM users WHERE user_id=? AND semester IS NOT NULL";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            try (ResultSet result = statement.executeQuery()) { return result.next(); }
        }
    }

    private int execute(Connection connection, String sql, long postId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, postId); return statement.executeUpdate();
        }
    }

    private Post map(ResultSet result) throws SQLException {
        Timestamp created = result.getTimestamp("created_at");
        Timestamp deadlineTs = null;
        try {
            deadlineTs = result.getTimestamp("deadline_date");
        } catch (SQLException ignored) {
        }
        long categoryValue = result.getLong("category_id");
        Long categoryId = result.wasNull() ? null : categoryValue;
        return new Post(result.getLong("post_id"), result.getLong("user_id"), categoryId,
                result.getString("author_name"), Role.valueOf(result.getString("author_role")),
                result.getString("category_name"), result.getString("title"), result.getString("content"),
                result.getString("image_url"), result.getString("visibility"),
                created == null ? null : created.toLocalDateTime(), result.getLong("reaction_count"),
                result.getLong("comment_count"), result.getBoolean("current_user_reacted"),
                deadlineTs == null ? null : deadlineTs.toLocalDateTime());
    }
}
