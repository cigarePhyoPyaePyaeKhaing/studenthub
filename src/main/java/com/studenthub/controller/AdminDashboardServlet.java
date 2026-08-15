package com.studenthub.controller;

import com.studenthub.service.AdminService;
import com.studenthub.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name="AdminDashboardServlet",urlPatterns="/admin")
public class AdminDashboardServlet extends HttpServlet {
    private final AdminService service = new AdminService();
    @Override protected void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
        if(!AdminRequest.requireAdmin(request,response))return;
        try { var data=service.dashboard();request.setAttribute("stats",data.stats());request.setAttribute("recentUsers",data.recentUsers()); }
        catch(SQLException exception){getServletContext().log("Admin dashboard load failed: "+exception.getClass().getName());request.setAttribute("recentUsers",List.of());request.setAttribute("error","Admin dashboard data is temporarily unavailable.");}
        request.setAttribute("csrfToken",CsrfToken.getOrCreate(request.getSession()));
        request.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(request,response);
    }
}
