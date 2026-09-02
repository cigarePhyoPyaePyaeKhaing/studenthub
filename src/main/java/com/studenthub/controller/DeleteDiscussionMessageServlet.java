package com.studenthub.controller;

import com.studenthub.model.DiscussionScope;
import com.studenthub.service.DiscussionService;
import com.studenthub.util.CsrfToken;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "DeleteDiscussionMessageServlet", urlPatterns = "/discussions/messages/delete")
public class DeleteDiscussionMessageServlet extends HttpServlet {
    private final DiscussionService service = new DiscussionService();

    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!CsrfToken.isValid(request)) { response.sendError(HttpServletResponse.SC_FORBIDDEN); return; }
        String scope = DiscussionScope.fromRequest(request.getParameter("scope")).name();
        long messageId;
        try { messageId = Long.parseLong(request.getParameter("id")); }
        catch (RuntimeException exception) { response.sendError(HttpServletResponse.SC_BAD_REQUEST); return; }
        try {
            DiscussionService.OperationResult result = service.delete(
                    (Long) request.getSession().getAttribute("userId"),
                    request.getSession().getAttribute("role"), messageId);
            if ("FORBIDDEN".equals(result.message())) { response.sendError(HttpServletResponse.SC_FORBIDDEN); return; }
            if ("NOT_FOUND".equals(result.message())) { response.sendError(HttpServletResponse.SC_NOT_FOUND); return; }
            request.getSession().setAttribute("flash", result.message());
        } catch (SQLException exception) {
            getServletContext().log("Discussion message delete failed: " + exception.getClass().getName());
            request.getSession().setAttribute("flashError", "The message could not be deleted right now.");
        }
        Object selectedScope = request.getSession().getAttribute("selectedDiscussionScope");
        String scopeQuery = "ADMIN".equals(String.valueOf(request.getSession().getAttribute("role")))
                && selectedScope instanceof String key && !key.isBlank()
                ? "?moderationScope=" + java.net.URLEncoder.encode(key, java.nio.charset.StandardCharsets.UTF_8)
                : "?scope=" + scope;
        response.sendRedirect(request.getContextPath() + "/discussions" + scopeQuery);
    }
}
