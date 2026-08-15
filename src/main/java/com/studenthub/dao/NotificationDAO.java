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

    public List<Notification> findVisible(long userId, int limit) throws SQLException {
        String sql = "SELECT n.notification_id,n.notification_type,n.title,n.message,n.link_url,n.created_at,"+
                "(nr.notification_id IS NOT NULL) is_read FROM notifications n JOIN users viewer ON viewer.user_id=? "+
                "LEFT JOIN notification_reads nr ON nr.notification_id=n.notification_id AND nr.user_id=viewer.user_id "+
                "WHERE "+VISIBLE+" ORDER BY n.created_at DESC,n.notification_id DESC LIMIT ?";
        List<Notification> items = new ArrayList<>();
        try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement(sql)){
            s.setLong(1,userId);s.setInt(2,Math.max(1,Math.min(limit,100)));
            try(ResultSet r=s.executeQuery()){while(r.next())items.add(map(r));}
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

    private Notification map(ResultSet r)throws SQLException{return new Notification(r.getLong("notification_id"),r.getString("notification_type"),
            r.getString("title"),r.getString("message"),r.getString("link_url"),r.getBoolean("is_read"),r.getTimestamp("created_at").toLocalDateTime());}
}
