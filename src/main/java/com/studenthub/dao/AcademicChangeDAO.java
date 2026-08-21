package com.studenthub.dao;
import com.studenthub.util.DBConnection;
import java.sql.*;import java.util.*;
public class AcademicChangeDAO{
 public record Item(long requestId,long userId,String studentId,String fullName,Integer oldSemester,String oldSection,int requestedSemester,String requestedSection,String reason,String status,String adminNote){}
 public boolean hasPending(long userId)throws SQLException{try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement("SELECT 1 FROM academic_change_requests WHERE user_id=? AND status='PENDING'")){s.setLong(1,userId);try(ResultSet r=s.executeQuery()){return r.next();}}}
 public void create(long userId,int semester,String section,String reason)throws SQLException{String sql="INSERT INTO academic_change_requests(user_id,old_semester,old_section,requested_semester,requested_section,reason) SELECT user_id,semester,section_name,?,?,? FROM users WHERE user_id=?";try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement(sql)){s.setInt(1,semester);s.setString(2,section);s.setString(3,reason);s.setLong(4,userId);try{if(s.executeUpdate()!=1)throw new IllegalStateException("Account not found.");}catch(SQLException e){if(e.getErrorCode()==1062)throw new IllegalStateException("You already have a pending academic change request.");throw e;}}}
 public List<Item> pending()throws SQLException{return list("WHERE a.status='PENDING' ORDER BY a.created_at",50);}
 private List<Item> list(String where,int limit)throws SQLException{String sql="SELECT a.request_id,a.user_id,u.student_id,u.full_name,a.old_semester,a.old_section,a.requested_semester,a.requested_section,a.reason,a.status,a.admin_note FROM academic_change_requests a JOIN users u ON u.user_id=a.user_id "+where+" LIMIT ?";try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement(sql)){s.setInt(1,limit);try(ResultSet r=s.executeQuery()){List<Item>x=new ArrayList<>();while(r.next()){int old=r.getInt(5);Integer oldSemester=r.wasNull()?null:old;x.add(new Item(r.getLong(1),r.getLong(2),r.getString(3),r.getString(4),oldSemester,r.getString(6),r.getInt(7),r.getString(8),r.getString(9),r.getString(10),r.getString(11)));}return x;}}}
    public boolean review(long requestId, long adminId, boolean approve, String note) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                long userId;
                int semester;
                String section;
                try (PreparedStatement s = c.prepareStatement(
                        "SELECT user_id, requested_semester, requested_section FROM academic_change_requests WHERE request_id=? AND status='PENDING' FOR UPDATE")) {
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
                            "UPDATE users SET semester=?, section_name=?, academic_info_locked=TRUE WHERE user_id=?")) {
                        s.setInt(1, semester);
                        s.setString(2, section);
                        s.setLong(3, userId);
                        s.executeUpdate();
                    }
                }

                try (PreparedStatement s = c.prepareStatement(
                        "UPDATE academic_change_requests SET status=?, reviewed_by=?, reviewed_at=UTC_TIMESTAMP(), admin_note=? WHERE request_id=?")) {
                    s.setString(1, approve ? "APPROVED" : "REJECTED");
                    s.setLong(2, adminId);
                    s.setString(3, note);
                    s.setLong(4, requestId);
                    s.executeUpdate();
                }

                try (PreparedStatement s = c.prepareStatement(
                        "INSERT INTO notifications (notification_type, title, message, link_url, actor_id, target_user_id, visibility) VALUES ('ACADEMIC_CHANGE', ?, ?, '/profile', ?, ?, 'ALL')")) {
                    s.setString(1, approve ? "Academic Change Approved" : "Academic Change Rejected");
                    String msg = approve
                            ? "Your request to change to Semester " + semester + " Section " + section + " was approved."
                            : "Your academic change request was rejected." + (note != null && !note.isBlank() ? " Note: " + note : "");
                    s.setString(2, msg);
                    s.setLong(3, adminId);
                    s.setLong(4, userId);
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
    }
}
