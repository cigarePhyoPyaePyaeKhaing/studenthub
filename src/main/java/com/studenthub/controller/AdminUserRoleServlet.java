package com.studenthub.controller;

import com.studenthub.service.AdminService;
import com.studenthub.util.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name="AdminUserRoleServlet",urlPatterns="/admin/users/role")
public class AdminUserRoleServlet extends HttpServlet {
    private final AdminService service=new AdminService();
    @Override protected void doPost(HttpServletRequest request,HttpServletResponse response)throws IOException{
        if(!AdminRequest.requireAdmin(request,response))return;if(!AdminMutationPolicy.canProceed(request.getSession().getAttribute("role"),CsrfToken.isValid(request))){response.sendError(403);return;}Long target=AdminRequest.positiveId(request.getParameter("userId"));if(target==null){response.sendError(400);return;}
        try {var result=service.changeRole((Long)request.getSession().getAttribute("userId"),request.getSession().getAttribute("role"),target,request.getParameter("role"));if("FORBIDDEN".equals(result.message())){response.sendError(403);return;}if("NOT_FOUND".equals(result.message())){response.sendError(404);return;}request.getSession().setAttribute(result.successful()?"flash":"flashError",result.message());response.sendRedirect(request.getContextPath()+"/admin/users/view?id="+target);}
        catch(SQLException exception){getServletContext().log("Admin role change failed: "+exception.getClass().getName());request.getSession().setAttribute("flashError","The role could not be changed right now.");response.sendRedirect(request.getContextPath()+"/admin/users/view?id="+target);}
    }
}
