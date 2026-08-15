package com.studenthub.controller;

import com.studenthub.service.DeadlineService;
import com.studenthub.util.Authorization;
import com.studenthub.util.CsrfToken;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name="DeleteDeadlineServlet",urlPatterns="/deadlines/delete")
public class DeleteDeadlineServlet extends HttpServlet{
    private final DeadlineService service=new DeadlineService();
    @Override protected void doPost(HttpServletRequest request,HttpServletResponse response)throws IOException{if(!Authorization.isAuthenticated(request.getSession(false))){response.sendRedirect(request.getContextPath()+"/login");return;}if(!Authorization.canManageDeadlines(request.getSession().getAttribute("role"))||!CsrfToken.isValid(request)){response.sendError(403);return;}Long id=parseId(request.getParameter("id"));if(id==null){response.sendError(400);return;}try{DeadlineService.OperationResult result=service.delete((Long)request.getSession().getAttribute("userId"),request.getSession().getAttribute("role"),id);if("FORBIDDEN".equals(result.message())){response.sendError(403);return;}if("NOT_FOUND".equals(result.message())){response.sendError(404);return;}request.getSession().setAttribute("flash",result.message());response.sendRedirect(request.getContextPath()+"/deadlines");}catch(SQLException exception){getServletContext().log("Delete-deadline failure: "+exception.getClass().getName());response.sendError(503);}}
    private Long parseId(String value){try{long id=Long.parseLong(value);return id>0?id:null;}catch(Exception exception){return null;}}
}
