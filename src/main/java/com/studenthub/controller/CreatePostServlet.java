package com.studenthub.controller;

import com.studenthub.dao.CategoryDAO;
import com.studenthub.service.PostService;
import com.studenthub.util.Authorization;
import com.studenthub.util.CsrfToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "CreatePostServlet", urlPatterns = "/posts/create")
@MultipartConfig(maxFileSize = 26214400L, maxRequestSize = 27262976L)
public class CreatePostServlet extends HttpServlet {
    private final PostService postService = new PostService();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!requireManager(request, response)) return;
        try {
            request.setAttribute("categories", categoryDAO.findAll());
        } catch (SQLException exception) {
            getServletContext().log("Create-post categories failed: " + exception.getClass().getName());
            request.setAttribute("error", "Categories are temporarily unavailable.");
        }
        request.setAttribute("csrfToken", CsrfToken.getOrCreate(request.getSession()));
        request.getRequestDispatcher("/WEB-INF/views/posts/create.jsp").forward(request, response);
    }

    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!requireManager(request, response)) return;
        if (!CsrfToken.isValid(request)) { response.sendError(403); return; }
        com.studenthub.util.AttachmentStorage storage = new com.studenthub.util.AttachmentStorage();
        String stored = null;
        try {
            java.util.Optional<com.studenthub.util.AttachmentValidator.Upload> upload =
                    com.studenthub.util.AttachmentValidator.validate(request.getPart("attachment"), false);
            com.studenthub.model.Attachment attachment = null;
            if (upload.isPresent()) { stored = storage.save(upload.get()); attachment = upload.get().metadata(stored); }
            PostService.OperationResult result = postService.create((Long) request.getSession().getAttribute("userId"),
                    request.getSession().getAttribute("role"), request.getParameter("title"),
                    request.getParameter("content"), parseCategory(request.getParameter("categoryId")),
                    request.getParameter("visibility"), request.getParameter("deadlineDate"), attachment);
            if ("FORBIDDEN".equals(result.message())) { response.sendError(403); return; }
            if (result.successful()) {
                request.getSession().setAttribute("flash", result.message());
                response.sendRedirect(request.getContextPath() + "/home");
                return;
            }
            if (stored != null) storage.delete(stored);
            request.setAttribute("error", result.message());
        } catch (SQLException exception) {
            if (stored != null) storage.delete(stored);
            getServletContext().log("Create-post database failure: " + exception.getClass().getName()
                    + ", SQLState=" + exception.getSQLState() + ", code=" + exception.getErrorCode());
            request.setAttribute("error", "The post could not be published right now.");
        } catch (IOException | ServletException | IllegalStateException exception) {
            if (stored != null) storage.delete(stored);
            request.setAttribute("error", exception.getMessage());
        }
        doGet(request, response);
    }

    private boolean requireManager(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!Authorization.isAuthenticated(request.getSession(false))) {
            response.sendRedirect(request.getContextPath() + "/login"); return false;
        }
        if (!Authorization.canManagePosts(request.getSession().getAttribute("role"))) {
            response.sendError(403); return false;
        }
        return true;
    }

    private Long parseCategory(String value) {
        if (value == null || value.isBlank()) return null;
        try { long id = Long.parseLong(value); return id > 0 ? id : null; }
        catch (NumberFormatException exception) { return null; }
    }
}
