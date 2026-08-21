package com.studenthub.controller;

import com.studenthub.dao.CategoryDAO;
import com.studenthub.service.AttachmentStorageService;
import com.studenthub.service.PostService;
import com.studenthub.util.Authorization;
import com.studenthub.util.CsrfToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "CreatePostServlet", urlPatterns = "/posts/create")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 50 * 1024 * 1024, maxRequestSize = 60 * 1024 * 1024)
public class CreatePostServlet extends HttpServlet {
    private final PostService postService = new PostService();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final AttachmentStorageService storageService = new AttachmentStorageService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!requireManager(request, response)) return;
        if (!CsrfToken.isValid(request)) {
            response.sendError(403);
            return;
        }

        List<AttachmentStorageService.StoredFileInfo> storedFiles = new ArrayList<>();
        try {
            for (Part part : request.getParts()) {
                if ("attachment".equalsIgnoreCase(part.getName()) && part.getSize() > 0) {
                    String submittedName = part.getSubmittedFileName();
                    if (submittedName != null && !submittedName.isBlank()) {
                        storedFiles.add(storageService.store(part.getInputStream(), submittedName, part.getContentType(), part.getSize()));
                    }
                }
            }
        } catch (IllegalArgumentException | SecurityException ex) {
            request.setAttribute("error", ex.getMessage());
            doGet(request, response);
            return;
        }

        try {
            LocalDateTime dueDate = parseDateTime(request.getParameter("dueDate"));
            PostService.OperationResult result = postService.create(
                    (Long) request.getSession().getAttribute("userId"),
                    request.getSession().getAttribute("role"),
                    request.getParameter("title"),
                    request.getParameter("content"),
                    parseCategory(request.getParameter("categoryId")),
                    request.getParameter("visibility"),
                    dueDate,
                    request.getParameter("subjectName"),
                    storedFiles);

            if ("FORBIDDEN".equals(result.message())) {
                response.sendError(403);
                return;
            }
            if (result.successful()) {
                request.getSession().setAttribute("flash", result.message());
                response.sendRedirect(request.getContextPath() + "/home");
                return;
            }
            request.setAttribute("error", result.message());
        } catch (SQLException exception) {
            getServletContext().log("Create-post database failure: " + exception.getClass().getName()
                    + ", SQLState=" + exception.getSQLState() + ", code=" + exception.getErrorCode());
            request.setAttribute("error", "The post could not be published right now.");
        }
        doGet(request, response);
    }

    private boolean requireManager(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!Authorization.isAuthenticated(request.getSession(false))) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        if (!Authorization.canManagePosts(request.getSession().getAttribute("role"))) {
            response.sendError(403);
            return false;
        }
        return true;
    }

    private Long parseCategory(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            long id = Long.parseLong(value);
            return id > 0 ? id : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDateTime.parse(value.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
