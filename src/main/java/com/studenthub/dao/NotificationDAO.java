package com.studenthub.dao;

import com.studenthub.model.Notification;
import com.studenthub.util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {
    private static final String VISIBLE = """
        (n.target_user_id IS NULL OR n.target_user_id = viewer.user_id)
        AND (n.visibility='ALL'
          OR (n.visibility='SEMESTER' AND n.semester=viewer.semester)
          OR (n.visibility='SECTION' AND n.semester=viewer.semester
              AND n.section_name=viewer.section_name))
        """;

    private record PostSummary(String title, String content) {}

    public static Long extractPostId(String linkUrl) {
        if (linkUrl == null || linkUrl.isBlank()) return null;
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?:postId|post_id|id)=(\\d+)").matcher(linkUrl);
        if (matcher.find()) {
            try {
                return Long.parseLong(matcher.group(1));
            } catch (NumberFormatException ignored) {}
        }
        matcher = java.util.regex.Pattern.compile("/posts/(\\d+)").matcher(linkUrl);
        if (matcher.find()) {
            try {
                return Long.parseLong(matcher.group(1));
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private void enrichAnnouncementContents(Connection connection, List<Notification> items) throws SQLException {
        if (items == null || items.isEmpty()) return;
        java.util.Set<Long> postIds = new java.util.LinkedHashSet<>();
        for (Notification item : items) {
            if ("ANNOUNCEMENT".equalsIgnoreCase(item.type())) {
                Long postId = extractPostId(item.linkUrl());
                if (postId != null) {
                    postIds.add(postId);
                }
            }
        }
        if (postIds.isEmpty()) return;

        StringBuilder sql = new StringBuilder("SELECT post_id, title, content FROM posts WHERE post_id IN (");
        int count = 0;
        for (int i = 0; i < postIds.size(); i++) {
            if (i > 0) sql.append(",");
            sql.append("?");
        }
        sql.append(")");

        java.util.Map<Long, PostSummary> postMap = new java.util.HashMap<>();
        try (PreparedStatement s = connection.prepareStatement(sql.toString())) {
            int idx = 1;
            for (Long id : postIds) {
                s.setLong(idx++, id);
            }
            try (ResultSet r = s.executeQuery()) {
                while (r.next()) {
                    long postId = r.getLong("post_id");
                    String title = r.getString("title");
                    String content = r.getString("content");
                    postMap.put(postId, new PostSummary(title, content));
                }
            }
        }

        for (int i = 0; i < items.size(); i++) {
            Notification item = items.get(i);
            if ("ANNOUNCEMENT".equalsIgnoreCase(item.type())) {
                Long postId = extractPostId(item.linkUrl());
                if (postId != null && postMap.containsKey(postId)) {
                    PostSummary post = postMap.get(postId);
                    String resolvedTitle = (post.title() != null && !post.title().isBlank()) ? post.title() : item.title();
                    String resolvedContent = (post.content() != null && !post.content().isBlank()) ? post.content() : item.message();
                    items.set(i, new Notification(
                        item.notificationId(),
                        item.type(),
                        resolvedTitle,
                        resolvedContent,
                        item.linkUrl(),
                        item.read(),
                        item.createdAt(),
                        item.actorUserId(),
                        item.actorName(),
                        item.actorAvatarUrl()
                    ));
                }
            }
        }
    }

    public List<Notification> findVisible(long userId, int limit) throws SQLException {
        String sql = "SELECT n.notification_id,n.notification_type,n.title,n.message,n.link_url,n.created_at,"+
                "actor.user_id actor_user_id,actor.full_name actor_name,actor.profile_image actor_avatar_url,"+
                "(nr.notification_id IS NOT NULL) is_read FROM notifications n JOIN users viewer ON viewer.user_id=? "+
                "LEFT JOIN users actor ON actor.user_id=n.actor_id "+
                "LEFT JOIN notification_reads nr ON nr.notification_id=n.notification_id AND nr.user_id=viewer.user_id "+
                "WHERE "+VISIBLE+" ORDER BY n.created_at DESC,n.notification_id DESC LIMIT ?";
        List<Notification> items = new ArrayList<>();
        try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement(sql)){
            s.setLong(1,userId);s.setInt(2,Math.max(1,Math.min(limit,100)));
            try(ResultSet r=s.executeQuery()){while(r.next())items.add(map(r));}
            enrichAnnouncementContents(c, items);
        }
        return items;
    }

    public long countUnread(long userId) throws SQLException {
        String sql="SELECT COUNT(*) FROM notifications n JOIN users viewer ON viewer.user_id=? "+
                "LEFT JOIN notification_reads nr ON nr.notification_id=n.notification_id AND nr.user_id=viewer.user_id "+
                "WHERE "+VISIBLE+" AND nr.notification_id IS NULL";
        try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement(sql)){
            s.setLong(1,userId);try(ResultSet r=s.executeQuery()){return r.next()?r.getLong(1):0;}
        }
    }

    public boolean markRead(long notificationId,long userId) throws SQLException {
        try(Connection c=DBConnection.getConnection()){
            if(!canView(c,notificationId,userId))return false;
            try(PreparedStatement s=c.prepareStatement("INSERT IGNORE INTO notification_reads(notification_id,user_id) VALUES(?,?)")){
                s.setLong(1,notificationId);s.setLong(2,userId);s.executeUpdate();return true;
            }
        }
    }

    private boolean canView(Connection c,long notificationId,long userId)throws SQLException{
        String sql="SELECT 1 FROM notifications n JOIN users viewer ON viewer.user_id=? WHERE n.notification_id=? AND "+VISIBLE;
        try(PreparedStatement s=c.prepareStatement(sql)){
            s.setLong(1,userId);s.setLong(2,notificationId);try(ResultSet r=s.executeQuery()){return r.next();}
        }
    }

    public void createForPost(Connection c,long postId,long actor,String type,String title,String message,String link,Long target)throws SQLException{
        String sql="INSERT INTO notifications(notification_type,title,message,link_url,actor_id,target_user_id,visibility,semester,section_name) "+
                "SELECT ?,?,?,?,?,?,p.visibility,u.semester,CASE WHEN p.visibility='SECTION' THEN u.section_name ELSE NULL END "+
                "FROM posts p JOIN users u ON u.user_id=p.user_id WHERE p.post_id=?";
        try(PreparedStatement s=c.prepareStatement(sql)){s.setString(1,type);s.setString(2,title);s.setString(3,message);s.setString(4,link);
            s.setLong(5,actor);if(target==null)s.setNull(6,Types.BIGINT);else s.setLong(6,target);s.setLong(7,postId);s.executeUpdate();}
    }

    public void createForDeadline(Connection c,long deadlineId,long actor,String title)throws SQLException{
        String sql="INSERT INTO notifications(notification_type,title,message,link_url,actor_id,visibility,semester,section_name) "+
                "SELECT 'DEADLINE',?,'A new deadline was added.',CONCAT('/deadlines'),?,"+
                "CASE WHEN section_name IS NULL THEN 'SEMESTER' ELSE 'SECTION' END,semester,section_name FROM deadlines WHERE deadline_id=?";
        try(PreparedStatement s=c.prepareStatement(sql)){s.setString(1,title);s.setLong(2,actor);s.setLong(3,deadlineId);s.executeUpdate();}
    }

    public void createForAdminRole(Connection connection, long actorId, String type, String title, String message, String linkUrl) throws SQLException {
        String sql = """
                INSERT INTO notifications (notification_type, title, message, link_url, actor_id, target_user_id, visibility)
                SELECT ?, ?, ?, ?, ?, user_id, 'ALL'
                FROM users
                WHERE role = 'ADMIN'
                """;
        try (PreparedStatement s = connection.prepareStatement(sql)) {
            s.setString(1, type);
            s.setString(2, title);
            s.setString(3, message);
            s.setString(4, linkUrl);
            s.setLong(5, actorId);
            s.executeUpdate();
        }
    }

    public void createForAdminRole(long actorId, String type, String title, String message, String linkUrl) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            createForAdminRole(connection, actorId, type, title, message, linkUrl);
        }
    }

    public void createDirect(Connection connection, long targetUserId, long actorId, String type, String title, String message, String linkUrl) throws SQLException {
        String sql = """
                INSERT INTO notifications (notification_type, title, message, link_url, actor_id, target_user_id, visibility)
                VALUES (?, ?, ?, ?, ?, ?, 'ALL')
                """;
        try (PreparedStatement s = connection.prepareStatement(sql)) {
            s.setString(1, type);
            s.setString(2, title);
            s.setString(3, message);
            s.setString(4, linkUrl);
            s.setLong(5, actorId);
            s.setLong(6, targetUserId);
            s.executeUpdate();
        }
    }

    public void createDirect(long targetUserId, long actorId, String type, String title, String message, String linkUrl) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            createDirect(connection, targetUserId, actorId, type, title, message, linkUrl);
        }
    }

    private Notification map(ResultSet r)throws SQLException{
        long actorId=r.getLong("actor_user_id"); Long nullableActor=r.wasNull()?null:actorId;
        return new Notification(r.getLong("notification_id"),r.getString("notification_type"),
            r.getString("title"),r.getString("message"),r.getString("link_url"),r.getBoolean("is_read"),
            r.getTimestamp("created_at").toLocalDateTime(),nullableActor,r.getString("actor_name"),r.getString("actor_avatar_url"));}
}
