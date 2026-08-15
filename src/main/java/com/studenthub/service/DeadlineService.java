package com.studenthub.service;

import com.studenthub.dao.DeadlineDAO;
import com.studenthub.dao.NotificationDAO;
import com.studenthub.model.Deadline;
import com.studenthub.model.Post;
import com.studenthub.util.Authorization;
import com.studenthub.util.DBConnection;
import com.studenthub.util.DeadlineValidation;
import com.studenthub.util.DeadlineScope;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

public class DeadlineService {
    public record OperationResult(boolean successful, String message) {
    }
    private final DeadlineDAO deadlineDAO = new DeadlineDAO();
    private final PostService postService = new PostService();
    private final NotificationDAO notificationDAO = new NotificationDAO();

    public OperationResult create(long userId,Object role,String titleInput,String subjectInput,String dueInput,
                                  String scopeInput,Long postId)throws SQLException{
        if(!Authorization.canManageDeadlines(role))return new OperationResult(false,"FORBIDDEN");
        String title=normalize(titleInput),subject=normalize(subjectInput),scope=normalize(scopeInput).toUpperCase(java.util.Locale.ROOT);
        String error=DeadlineValidation.validate(title,subject,dueInput,scope);if(error!=null)return new OperationResult(false,error);
        OperationResult postCheck=checkPostAuthorization(userId,role,postId);if(postCheck!=null)return postCheck;
        LocalDateTime due=DeadlineValidation.parseDueDate(dueInput);
        try(Connection connection=DBConnection.getConnection()){connection.setAutoCommit(false);try{
            DeadlineDAO.AcademicScope academic=deadlineDAO.findScope(connection,userId);
            if(academic.semester()==null){connection.rollback();return new OperationResult(false,"Your account needs a semester before creating deadlines.");}
            DeadlineScope.Resolved resolved=DeadlineScope.resolve(scope,academic.semester(),academic.sectionName());if("SECTION".equals(scope)&&resolved.sectionName()==null){connection.rollback();return new OperationResult(false,"Your account needs a section for section deadlines.");}
            long id=deadlineDAO.create(connection,postId,title,subject,due,resolved.semester(),resolved.sectionName(),userId);if(id<=0)throw new SQLException("Deadline was saved but its identifier was unavailable.");
            notificationDAO.createForDeadline(connection,id,userId,title);connection.commit();
        }catch(SQLException exception){connection.rollback();throw exception;}finally{connection.setAutoCommit(true);}}
        return new OperationResult(true,"Deadline created.");
    }

    public Optional<Deadline> findById(long id)throws SQLException{return deadlineDAO.findById(id);}

    public OperationResult update(long userId,Object role,long id,String titleInput,String subjectInput,
                                  String dueInput,String scopeInput,Long postId)throws SQLException{
        Optional<Deadline> found=deadlineDAO.findById(id);if(found.isEmpty())return new OperationResult(false,"NOT_FOUND");
        if(!Authorization.canManageDeadline(role,userId,found.get().createdBy()))return new OperationResult(false,"FORBIDDEN");
        String title=normalize(titleInput),subject=normalize(subjectInput),scope=normalize(scopeInput).toUpperCase(java.util.Locale.ROOT);
        String error=DeadlineValidation.validate(title,subject,dueInput,scope);if(error!=null)return new OperationResult(false,error);
        OperationResult postCheck=checkPostAuthorization(userId,role,postId);if(postCheck!=null)return postCheck;
        try(Connection connection=DBConnection.getConnection()){
            DeadlineDAO.AcademicScope academic=deadlineDAO.findScope(connection,found.get().createdBy());
            if(academic.semester()==null)return new OperationResult(false,"The deadline creator has no configured semester.");
            DeadlineScope.Resolved resolved=DeadlineScope.resolve(scope,academic.semester(),academic.sectionName());if("SECTION".equals(scope)&&resolved.sectionName()==null)return new OperationResult(false,"The deadline creator has no configured section.");
            if(deadlineDAO.update(connection,id,postId,title,subject,DeadlineValidation.parseDueDate(dueInput),resolved.semester(),resolved.sectionName())!=1)return new OperationResult(false,"NOT_FOUND");
        }
        return new OperationResult(true,"Deadline updated.");
    }

    public OperationResult delete(long userId,Object role,long id)throws SQLException{
        Optional<Deadline> found=deadlineDAO.findById(id);if(found.isEmpty())return new OperationResult(false,"NOT_FOUND");
        if(!Authorization.canManageDeadline(role,userId,found.get().createdBy()))return new OperationResult(false,"FORBIDDEN");
        try(Connection connection=DBConnection.getConnection()){if(deadlineDAO.delete(connection,id)!=1)return new OperationResult(false,"NOT_FOUND");}
        return new OperationResult(true,"Deadline deleted.");
    }

    private OperationResult checkPostAuthorization(long userId,Object role,Long postId)throws SQLException{
        if(postId==null)return null;Optional<Post> post=postService.findById(postId);if(post.isEmpty())return new OperationResult(false,"Related announcement does not exist.");
        if(!Authorization.canManagePost(role,userId,post.get().authorId()))return new OperationResult(false,"You cannot link that announcement.");return null;
    }
    private String normalize(String value){return value==null?"":value.trim();}
}
