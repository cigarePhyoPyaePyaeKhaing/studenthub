package com.studenthub.controller;

import com.studenthub.dao.NotificationDAO;
import com.studenthub.util.CsrfToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name="NotificationsServlet",urlPatterns="/notifications")
public class NotificationsServlet extends HttpServlet {
    private final NotificationDAO dao=new NotificationDAO();
    @Override protected void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
        long userId=(Long)request.getSession().getAttribute("userId");
        try{request.setAttribute("notifications",dao.findVisible(userId,100));}
        catch(SQLException e){getServletContext().log("Notification load failed: "+e.getClass().getName());request.setAttribute("notifications",List.of());request.setAttribute("error","Notifications are temporarily unavailable.");}
        request.setAttribute("csrfToken",CsrfToken.getOrCreate(request.getSession()));
        request.getRequestDispatcher("/WEB-INF/views/notifications.jsp").forward(request,response);
    }
    @Override protected void doPost(HttpServletRequest request,HttpServletResponse response)throws IOException{
        if(!CsrfToken.isValid(request)){response.sendError(403);return;}
        Long id=positiveId(request.getParameter("id"));if(id==null){response.sendError(400);return;}
        try{if(!dao.markRead(id,(Long)request.getSession().getAttribute("userId"))){response.sendError(404);return;}}
        catch(SQLException e){getServletContext().log("Mark notification read failed: "+e.getClass().getName());response.sendError(500);return;}
        response.sendRedirect(request.getContextPath()+"/notifications");
    }
    private Long positiveId(String value){try{long id=Long.parseLong(value);return id>0?id:null;}catch(Exception e){return null;}}
}
