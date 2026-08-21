package com.studenthub.controller;

import com.studenthub.dao.CategoryDAO;
import com.studenthub.model.Post;
import com.studenthub.service.PostService;
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

@WebServlet(name = "EditPostServlet", urlPatterns = "/posts/edit")
public class EditPostServlet extends HttpServlet {
    private final PostService postService = new PostService();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!requireManager(request, response)) return;
        Long id = parseId(request.getParameter("id")); if (id == null) { response.sendError(400); return; }
        try {
            Optional<Post> found = postService.findById(id);
            if (found.isEmpty()) { response.sendError(404); return; }
            if (!Authorization.canManagePost(request.getSession().getAttribute("role"),
                    (Long) request.getSession().getAttribute("userId"), found.get().authorId())) {
                response.sendError(403); return;
            }
            request.setAttribute("post", found.get()); request.setAttribute("categories", categoryDAO.findAll());
            request.setAttribute("csrfToken", CsrfToken.getOrCreate(request.getSession()));
            request.getRequestDispatcher("/WEB-INF/views/posts/edit.jsp").forward(request, response);
        } catch (SQLException exception) {
            getServletContext().log("Edit-post load failed: " + exception.getClass().getName()); response.sendError(503);
        }
    }

    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!requireManager(request, response)) return;
        if (!CsrfToken.isValid(request)) { response.sendError(403); return; }
        Long id = parseId(request.getParameter("id")); if (id == null) { response.sendError(400); return; }
        try {
            PostService.OperationResult result = postService.update((Long) request.getSession().getAttribute("userId"),
                    request.getSession().getAttribute("role"), id, request.getParameter("title"),
                    request.getParameter("content"), parseId(request.getParameter("categoryId")),
                    request.getParameter("visibility"), request.getParameter("deadlineDate"));
            if ("FORBIDDEN".equals(result.message())) { response.sendError(403); return; }
            if ("NOT_FOUND".equals(result.message())) { response.sendError(404); return; }
            if (result.successful()) {
                request.getSession().setAttribute("flash", result.message());
                response.sendRedirect(request.getContextPath() + "/announcements"); return;
            }
            request.setAttribute("error", result.message()); doGet(request, response);
        } catch (SQLException exception) {
            getServletContext().log("Edit-post failure: " + exception.getClass().getName()); response.sendError(503);
        }
    }

    private boolean requireManager(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!Authorization.isAuthenticated(request.getSession(false))) { response.sendRedirect(request.getContextPath()+"/login"); return false; }
        if (!Authorization.canManagePosts(request.getSession().getAttribute("role"))) { response.sendError(403); return false; }
        return true;
    }
    private Long parseId(String value) { try { long id=Long.parseLong(value); return id>0?id:null; } catch(Exception exception){return null;} }
}
