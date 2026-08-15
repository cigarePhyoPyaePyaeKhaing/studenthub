package com.studenthub.controller;

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

@WebServlet(name="CreateDeadlineServlet",urlPatterns="/deadlines/create")
public class CreateDeadlineServlet extends HttpServlet{
    private final DeadlineService service=new DeadlineService();
    @Override protected void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{if(!requireManager(request,response))return;request.setAttribute("csrfToken",CsrfToken.getOrCreate(request.getSession()));request.getRequestDispatcher("/WEB-INF/views/deadlines/create.jsp").forward(request,response);}
    @Override protected void doPost(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{request.setCharacterEncoding("UTF-8");if(!requireManager(request,response))return;if(!CsrfToken.isValid(request)){response.sendError(403);return;}try{DeadlineService.OperationResult result=service.create((Long)request.getSession().getAttribute("userId"),request.getSession().getAttribute("role"),request.getParameter("title"),request.getParameter("subjectName"),request.getParameter("dueDate"),request.getParameter("scope"),parseId(request.getParameter("postId")));if("FORBIDDEN".equals(result.message())){response.sendError(403);return;}if(result.successful()){request.getSession().setAttribute("flash",result.message());response.sendRedirect(request.getContextPath()+"/deadlines");return;}request.setAttribute("error",result.message());doGet(request,response);}catch(SQLException exception){getServletContext().log("Create-deadline failure: "+exception.getClass().getName());request.setAttribute("error","The deadline could not be created right now.");doGet(request,response);}}
    private boolean requireManager(HttpServletRequest request,HttpServletResponse response)throws IOException{if(!Authorization.isAuthenticated(request.getSession(false))){response.sendRedirect(request.getContextPath()+"/login");return false;}if(!Authorization.canManageDeadlines(request.getSession().getAttribute("role"))){response.sendError(403);return false;}return true;}
    private Long parseId(String value){if(value==null||value.isBlank())return null;try{long id=Long.parseLong(value);return id>0?id:null;}catch(Exception exception){return null;}}
}
