package com.studenthub.controller;

import com.studenthub.model.DiscussionScope;
import com.studenthub.service.DiscussionService;
import com.studenthub.util.CsrfToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "SendDiscussionMessageServlet", urlPatterns = "/discussions/messages")
@MultipartConfig(maxFileSize = 26214400L, maxRequestSize = 27262976L)
public class SendDiscussionMessageServlet extends HttpServlet {
    private final DiscussionService service = new DiscussionService();

    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        if (!CsrfToken.isValid(request)) { response.sendError(HttpServletResponse.SC_FORBIDDEN); return; }
        String scope = DiscussionScope.fromRequest(request.getParameter("scope")).name();
        com.studenthub.util.AttachmentStorage storage = new com.studenthub.util.AttachmentStorage();
        String stored = null;
        try {
            java.util.Optional<com.studenthub.util.AttachmentValidator.Upload> upload =
                    com.studenthub.util.AttachmentValidator.validate(request.getPart("attachment"), true);
            com.studenthub.model.Attachment attachment = null;
            if (upload.isPresent()) {
                stored = storage.save(upload.get());
                attachment = upload.get().metadata(stored);
            }
            DiscussionService.OperationResult result = service.send(
                    (Long) request.getSession().getAttribute("userId"), scope, request.getParameter("message"), attachment);
            if (!result.successful() && stored != null) storage.delete(stored);
            request.getSession().setAttribute(result.successful() ? "flash" : "flashError", result.message());
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        } catch (SQLException exception) {
            if (stored != null) storage.delete(stored);
            getServletContext().log("Discussion message send failed: " + exception.getClass().getName());
            request.getSession().setAttribute("flashError", "The message could not be sent right now.");
        } catch (ServletException | IllegalStateException exception) {
            if (stored != null) storage.delete(stored);
            request.getSession().setAttribute("flashError", "Choose a supported attachment within the size limit.");
        } catch (IOException exception) {
            if (stored != null) storage.delete(stored);
            request.getSession().setAttribute("flashError", exception.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/discussions?scope=" + scope);
    }
}
