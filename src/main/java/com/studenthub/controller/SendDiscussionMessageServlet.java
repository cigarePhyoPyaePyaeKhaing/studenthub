package com.studenthub.controller;

import com.studenthub.model.DiscussionScope;
import com.studenthub.service.DiscussionService;
import com.studenthub.util.CsrfToken;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "SendDiscussionMessageServlet", urlPatterns = "/discussions/messages")
public class SendDiscussionMessageServlet extends HttpServlet {
    private final DiscussionService service = new DiscussionService();

    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        if (!CsrfToken.isValid(request)) { response.sendError(HttpServletResponse.SC_FORBIDDEN); return; }
        String scope = DiscussionScope.fromRequest(request.getParameter("scope")).name();
        try {
            DiscussionService.OperationResult result = service.send(
                    (Long) request.getSession().getAttribute("userId"), scope, request.getParameter("message"));
            request.getSession().setAttribute(result.successful() ? "flash" : "flashError", result.message());
        } catch (SQLException exception) {
            getServletContext().log("Discussion message send failed: " + exception.getClass().getName());
            request.getSession().setAttribute("flashError", "The message could not be sent right now.");
        }
        response.sendRedirect(request.getContextPath() + "/discussions?scope=" + scope);
    }
}
