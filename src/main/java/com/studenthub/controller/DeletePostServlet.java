package com.studenthub.controller;

import com.studenthub.service.PostService;
import com.studenthub.util.Authorization;
import com.studenthub.util.CsrfToken;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "DeletePostServlet", urlPatterns = "/posts/delete")
public class DeletePostServlet extends HttpServlet {
    private final PostService postService = new PostService();
    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!Authorization.isAuthenticated(request.getSession(false))) { response.sendRedirect(request.getContextPath()+"/login"); return; }
        if (!Authorization.canManagePosts(request.getSession().getAttribute("role")) || !CsrfToken.isValid(request)) { response.sendError(403); return; }
        Long id=parseId(request.getParameter("id")); if(id==null){response.sendError(400);return;}
        try {
            PostService.OperationResult result=postService.delete((Long)request.getSession().getAttribute("userId"),request.getSession().getAttribute("role"),id);
            if("FORBIDDEN".equals(result.message())){response.sendError(403);return;}
            if("NOT_FOUND".equals(result.message())){response.sendError(404);return;}
            request.getSession().setAttribute("flash",result.message()); response.sendRedirect(request.getContextPath()+"/announcements");
        } catch(SQLException exception){getServletContext().log("Delete-post failure: "+exception.getClass().getName());response.sendError(503);}
    }
    private Long parseId(String value){try{long id=Long.parseLong(value);return id>0?id:null;}catch(Exception exception){return null;}}
}
