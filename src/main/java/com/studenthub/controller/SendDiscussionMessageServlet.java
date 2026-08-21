package com.studenthub.controller;

import com.studenthub.model.DiscussionScope;
import com.studenthub.service.AttachmentStorageService;
import com.studenthub.service.DiscussionService;
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
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "SendDiscussionMessageServlet", urlPatterns = "/discussions/messages")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 50 * 1024 * 1024, maxRequestSize = 60 * 1024 * 1024)
public class SendDiscussionMessageServlet extends HttpServlet {
    private final DiscussionService service = new DiscussionService();
    private final AttachmentStorageService storageService = new AttachmentStorageService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!CsrfToken.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String scope = DiscussionScope.fromRequest(request.getParameter("scope")).name();

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
            request.getSession().setAttribute("flashError", ex.getMessage());
            response.sendRedirect(request.getContextPath() + "/discussions?scope=" + scope);
            return;
        }

        try {
            DiscussionService.OperationResult result = service.send(
                    (Long) request.getSession().getAttribute("userId"),
                    scope,
                    request.getParameter("message"),
                    storedFiles);
            request.getSession().setAttribute(result.successful() ? "flash" : "flashError", result.message());
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        } catch (SQLException exception) {
            getServletContext().log("Discussion message send failed: " + exception.getClass().getName());
            request.getSession().setAttribute("flashError", "The message could not be sent right now.");
        }
        response.sendRedirect(request.getContextPath() + "/discussions?scope=" + scope);
    }
}
