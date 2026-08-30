package com.studenthub.controller;

import com.studenthub.service.AdminService;
import com.studenthub.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name="AdminUsersServlet",urlPatterns="/admin/users")
public class AdminUsersServlet extends HttpServlet {
    private final AdminService service=new AdminService();
    @Override protected void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
        if(!AdminRequest.requireAdmin(request,response))return;
        try {var directory=service.users(request.getParameter("q"));request.setAttribute("users",directory.users());request.setAttribute("query",directory.search());request.setAttribute("totalUsers",directory.totalUsers());}
        catch(IllegalArgumentException exception){request.setAttribute("users",List.of());request.setAttribute("totalUsers",0L);request.setAttribute("error",exception.getMessage());}
        catch(SQLException exception){getServletContext().log("Admin users load failed: "+exception.getClass().getName());request.setAttribute("users",List.of());request.setAttribute("totalUsers",0L);request.setAttribute("error","Users are temporarily unavailable.");}
        request.setAttribute("csrfToken",CsrfToken.getOrCreate(request.getSession()));moveFlash(request);
        request.getRequestDispatcher("/WEB-INF/views/admin/users.jsp").forward(request,response);
    }
    private void moveFlash(HttpServletRequest request){Object value=request.getSession().getAttribute("flash");if(value!=null){request.setAttribute("message",value);request.getSession().removeAttribute("flash");}Object error=request.getSession().getAttribute("flashError");if(error!=null){request.setAttribute("error",error);request.getSession().removeAttribute("flashError");}}
}
