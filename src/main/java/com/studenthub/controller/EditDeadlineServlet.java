package com.studenthub.controller;

import com.studenthub.model.Deadline;
import com.studenthub.service.DeadlineService;
import com.studenthub.util.Authorization;
import com.studenthub.util.CsrfToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet(name="EditDeadlineServlet",urlPatterns="/deadlines/edit")
public class EditDeadlineServlet extends HttpServlet{
    private final DeadlineService service=new DeadlineService();
    @Override protected void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{if(!requireManager(request,response))return;Long id=parseId(request.getParameter("id"));if(id==null){response.sendError(400);return;}try{Optional<Deadline> found=service.findById(id);if(found.isEmpty()){response.sendError(404);return;}if(!Authorization.canManageDeadline(request.getSession().getAttribute("role"),(Long)request.getSession().getAttribute("userId"),found.get().createdBy())){response.sendError(403);return;}request.setAttribute("deadline",found.get());request.setAttribute("csrfToken",CsrfToken.getOrCreate(request.getSession()));request.getRequestDispatcher("/WEB-INF/views/deadlines/edit.jsp").forward(request,response);}catch(SQLException exception){getServletContext().log("Edit-deadline load failed: "+exception.getClass().getName());response.sendError(503);}}
    @Override protected void doPost(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{request.setCharacterEncoding("UTF-8");if(!requireManager(request,response))return;if(!CsrfToken.isValid(request)){response.sendError(403);return;}Long id=parseId(request.getParameter("id"));if(id==null){response.sendError(400);return;}try{DeadlineService.OperationResult result=service.update((Long)request.getSession().getAttribute("userId"),request.getSession().getAttribute("role"),id,request.getParameter("title"),request.getParameter("subjectName"),request.getParameter("dueDate"),request.getParameter("scope"),parseId(request.getParameter("postId")));if("FORBIDDEN".equals(result.message())){response.sendError(403);return;}if("NOT_FOUND".equals(result.message())){response.sendError(404);return;}if(result.successful()){request.getSession().setAttribute("flash",result.message());response.sendRedirect(request.getContextPath()+"/deadlines");return;}request.setAttribute("error",result.message());doGet(request,response);}catch(SQLException exception){getServletContext().log("Edit-deadline failure: "+exception.getClass().getName());response.sendError(503);}}
    private boolean requireManager(HttpServletRequest request,HttpServletResponse response)throws IOException{if(!Authorization.isAuthenticated(request.getSession(false))){response.sendRedirect(request.getContextPath()+"/login");return false;}if(!Authorization.canManageDeadlines(request.getSession().getAttribute("role"))){response.sendError(403);return false;}return true;}
    private Long parseId(String value){if(value==null||value.isBlank())return null;try{long id=Long.parseLong(value);return id>0?id:null;}catch(Exception exception){return null;}}
}
