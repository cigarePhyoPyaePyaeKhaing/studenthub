package com.studenthub.controller;

import com.studenthub.service.AccountDeletionService;
import com.studenthub.util.CsrfToken;
import com.studenthub.util.ProfilePhotoStorage;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name="DeleteAccountServlet", urlPatterns="/profile/delete-account")
public class DeleteAccountServlet extends HttpServlet {
    private final AccountDeletionService service;
    private final ProfilePhotoStorage photoStorage;
    public DeleteAccountServlet() { this(new AccountDeletionService(), new ProfilePhotoStorage()); }
    DeleteAccountServlet(AccountDeletionService service, ProfilePhotoStorage photoStorage) { this.service=service; this.photoStorage=photoStorage; }

    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); response.setCharacterEncoding("UTF-8");
        HttpSession session=request.getSession(false);
        if(session==null || !(session.getAttribute("userId") instanceof Long userId)) { write(response,401,"ACCOUNT_DELETE_UNAUTHENTICATED","Sign in again to continue."); return; }
        if(!CsrfToken.isValid(request)) { logSafe("Account deletion rejected: userId="+userId+", stage=csrf_validation, code=ACCOUNT_DELETE_CSRF_INVALID"); write(response,403,"ACCOUNT_DELETE_CSRF_INVALID","Your security session expired. Refresh the page and try again."); return; }
        try {
            AccountDeletionService.Result result=service.deleteOwnAccount(userId,request.getParameter("currentPassword"));
            if(!result.success()) {
                if("ACCOUNT_DELETE_LAST_ADMIN".equals(result.code())) write(response,409,result.code(),"StudentHub must have at least one administrator before this account can be deleted.");
                else if("ACCOUNT_DELETE_PASSWORD_INVALID".equals(result.code())) write(response,400,result.code(),"Incorrect password.");
                else write(response,409,result.code(),"This account could not be deleted.");
                return;
            }
            photoStorage.delete(result.profileImage());
            session.invalidate();
            Cookie cookie=new Cookie("STUDENTHUB_SESSION",""); cookie.setHttpOnly(true); cookie.setPath(request.getContextPath().isEmpty()?"/":request.getContextPath()); cookie.setMaxAge(0); response.addCookie(cookie);
            response.setStatus(200);
            response.getWriter().write("{\"success\":true,\"code\":\"ACCOUNT_DELETE_OK\",\"redirectUrl\":\""+escape(request.getContextPath()+"/login?accountDeleted=1")+"\"}");
        } catch(SQLException exception) {
            logSafe("Account deletion failed: userId="+userId+", stage=transaction, exception="+exception.getClass().getName()+", SQLState="+exception.getSQLState()+", errorCode="+exception.getErrorCode());
            write(response,500,"ACCOUNT_DELETE_DB_ERROR","Your account could not be deleted right now.");
        } catch(RuntimeException exception) {
            logSafe("Account deletion failed: userId="+userId+", stage=transaction, exception="+exception.getClass().getName());
            write(response,500,"ACCOUNT_DELETE_SERVER_ERROR","Your account could not be deleted right now.");
        }
    }
    @Override protected void doGet(HttpServletRequest request,HttpServletResponse response)throws IOException { response.sendError(405); }
    private void write(HttpServletResponse response,int status,String code,String message)throws IOException { response.setStatus(status); response.getWriter().write("{\"success\":false,\"code\":\""+code+"\",\"message\":\""+escape(message)+"\"}"); }
    private String escape(String value){return value.replace("\\","\\\\").replace("\"","\\\"");}
    private void logSafe(String value){try{if(getServletContext()!=null)getServletContext().log(value);else System.err.println(value);}catch(Exception ignored){System.err.println(value);}}
}
