package com.studenthub.dao;

import com.studenthub.model.Deadline;
import com.studenthub.util.DBConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeadlineDAO {
    public record AcademicScope(Integer semester, String sectionName) {
    }

    public List<Deadline> findUpcomingForUser(long viewerId, int limit) throws SQLException {
        return findForUser(viewerId, false, limit);
    }

    public List<Deadline> findPastForUser(long viewerId, int limit) throws SQLException {
        return findForUser(viewerId, true, limit);
    }

    private List<Deadline> findForUser(long viewerId, boolean past, int limit) throws SQLException {
        String comparison = past ? "<" : ">=";
        String order = past ? "DESC" : "ASC";
        String sql = "SELECT d.deadline_id,d.post_id,d.title,d.subject_name,d.due_date,d.semester,"
                + "d.section_name,d.created_by,d.created_at,creator.full_name creator_name,p.title related_post_title "
                + "FROM deadlines d JOIN users viewer ON viewer.user_id=? "
                + "JOIN users creator ON creator.user_id=d.created_by LEFT JOIN posts p ON p.post_id=d.post_id "
                + "WHERE viewer.semester IS NOT NULL AND d.semester=viewer.semester "
                + "AND (d.section_name IS NULL OR d.section_name=viewer.section_name) "
                + "AND d.due_date " + comparison + " CURRENT_TIMESTAMP "
                + "ORDER BY d.due_date " + order + ",d.deadline_id " + order + " LIMIT ?";
        List<Deadline> deadlines = new ArrayList<>();
        try (Connection connection=DBConnection.getConnection();PreparedStatement statement=connection.prepareStatement(sql)) {
            statement.setLong(1,viewerId);statement.setInt(2,limit);
            try(ResultSet result=statement.executeQuery()){while(result.next())deadlines.add(map(result));}
        }
        if (!past) deadlines.sort(com.studenthub.util.DeadlineOrdering.upcoming());
        return deadlines;
    }

    public Optional<Deadline> findById(long id) throws SQLException {
        String sql="SELECT d.deadline_id,d.post_id,d.title,d.subject_name,d.due_date,d.semester,"
                +"d.section_name,d.created_by,d.created_at,creator.full_name creator_name,p.title related_post_title "
                +"FROM deadlines d JOIN users creator ON creator.user_id=d.created_by "
                +"LEFT JOIN posts p ON p.post_id=d.post_id WHERE d.deadline_id=?";
        try(Connection connection=DBConnection.getConnection();PreparedStatement statement=connection.prepareStatement(sql)){statement.setLong(1,id);try(ResultSet result=statement.executeQuery()){return result.next()?Optional.of(map(result)):Optional.empty();}}
    }

    public Optional<Deadline> findByPostId(long postId) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            return findByPostId(connection, postId);
        }
    }

    public Optional<Deadline> findByPostId(Connection connection, long postId) throws SQLException {
        String sql = "SELECT d.deadline_id,d.post_id,d.title,d.subject_name,d.due_date,d.semester,"
                + "d.section_name,d.created_by,d.created_at,creator.full_name creator_name,p.title related_post_title "
                + "FROM deadlines d JOIN users creator ON creator.user_id=d.created_by "
                + "LEFT JOIN posts p ON p.post_id=d.post_id WHERE d.post_id=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, postId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(map(result)) : Optional.empty();
            }
        }
    }

    public List<Deadline> findAllForUser(long viewerId) throws SQLException {
        String sql = "SELECT d.deadline_id,d.post_id,d.title,d.subject_name,d.due_date,d.semester,"
                + "d.section_name,d.created_by,d.created_at,creator.full_name creator_name,p.title related_post_title "
                + "FROM deadlines d JOIN users viewer ON viewer.user_id=? "
                + "JOIN users creator ON creator.user_id=d.created_by LEFT JOIN posts p ON p.post_id=d.post_id "
                + "WHERE viewer.semester IS NOT NULL AND d.semester=viewer.semester "
                + "AND (d.section_name IS NULL OR d.section_name=viewer.section_name) "
                + "ORDER BY d.due_date ASC, d.deadline_id ASC";
        List<Deadline> deadlines = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, viewerId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) deadlines.add(map(result));
            }
        }
        return deadlines;
    }

    public AcademicScope findScope(Connection connection,long userId)throws SQLException{
        try(PreparedStatement statement=connection.prepareStatement("SELECT semester,section_name FROM users WHERE user_id=?")){statement.setLong(1,userId);try(ResultSet result=statement.executeQuery()){if(!result.next())return new AcademicScope(null,null);int semester=result.getInt("semester");return new AcademicScope(result.wasNull()?null:semester,result.getString("section_name"));}}
    }

    public long create(Connection connection,Long postId,String title,String subject,LocalDateTime due,
                       int semester,String section,long creator)throws SQLException{
        String sql="INSERT INTO deadlines(post_id,title,subject_name,due_date,semester,section_name,created_by) VALUES(?,?,?,?,?,?,?)";
        try(PreparedStatement statement=connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)){
            setValues(statement,postId,title,subject,due,semester,section);statement.setLong(7,creator);statement.executeUpdate();try(ResultSet keys=statement.getGeneratedKeys()){return keys.next()?keys.getLong(1):0;}}
    }

    public int update(Connection connection,long id,Long postId,String title,String subject,LocalDateTime due,
                      int semester,String section)throws SQLException{
        String sql="UPDATE deadlines SET post_id=?,title=?,subject_name=?,due_date=?,semester=?,section_name=? WHERE deadline_id=?";
        try(PreparedStatement statement=connection.prepareStatement(sql)){setValues(statement,postId,title,subject,due,semester,section);statement.setLong(7,id);return statement.executeUpdate();}
    }

    public int delete(Connection connection,long id)throws SQLException{try(PreparedStatement statement=connection.prepareStatement("DELETE FROM deadlines WHERE deadline_id=?")){statement.setLong(1,id);return statement.executeUpdate();}}

    private void setValues(PreparedStatement statement,Long postId,String title,String subject,LocalDateTime due,int semester,String section)throws SQLException{if(postId==null)statement.setNull(1,Types.BIGINT);else statement.setLong(1,postId);statement.setString(2,title);statement.setString(3,subject);statement.setTimestamp(4,Timestamp.valueOf(due));statement.setInt(5,semester);if(section==null)statement.setNull(6,Types.VARCHAR);else statement.setString(6,section);}
    private Deadline map(ResultSet result)throws SQLException{long postValue=result.getLong("post_id");Long postId=result.wasNull()?null:postValue;Timestamp due=result.getTimestamp("due_date");Timestamp created=result.getTimestamp("created_at");return new Deadline(result.getLong("deadline_id"),postId,result.getString("related_post_title"),result.getString("title"),result.getString("subject_name"),due.toLocalDateTime(),result.getInt("semester"),result.getString("section_name"),result.getLong("created_by"),result.getString("creator_name"),created.toLocalDateTime());}
}
