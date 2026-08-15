package com.studenthub.controller;

import com.studenthub.service.AdminService;
import com.studenthub.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name="AdminUserDetailServlet",urlPatterns="/admin/users/view")
public class AdminUserDetailServlet extends HttpServlet {
    private final AdminService service=new AdminService();
    @Override protected void doGet(HttpServletRequest request,HttpServletResponse response)throws ServletException,IOException{
        if(!AdminRequest.requireAdmin(request,response))return;Long id=AdminRequest.positiveId(request.getParameter("id"));if(id==null){response.sendError(400);return;}
        try {var user=service.user(id);if(user.isEmpty()){response.sendError(404);return;}request.setAttribute("managedUser",user.get());}
        catch(SQLException exception){getServletContext().log("Admin user detail load failed: "+exception.getClass().getName());request.setAttribute("error","User information is temporarily unavailable.");}
        request.setAttribute("csrfToken",CsrfToken.getOrCreate(request.getSession()));Object flash=request.getSession().getAttribute("flash");if(flash!=null){request.setAttribute("message",flash);request.getSession().removeAttribute("flash");}Object flashError=request.getSession().getAttribute("flashError");if(flashError!=null){request.setAttribute("error",flashError);request.getSession().removeAttribute("flashError");}
        request.getRequestDispatcher("/WEB-INF/views/admin/user-detail.jsp").forward(request,response);
    }
}
