package com.studenthub.controller;

import com.studenthub.model.DiscussionScope;
import com.studenthub.service.DiscussionService;
import com.studenthub.util.CsrfToken;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "SendDiscussionMessageServlet", urlPatterns = "/discussions/messages")
@MultipartConfig(maxFileSize=52428800L,maxRequestSize=57671680L)
public class SendDiscussionMessageServlet extends HttpServlet {
    private final DiscussionService service = new DiscussionService();

    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, jakarta.servlet.ServletException {
        request.setCharacterEncoding("UTF-8");
        if (!CsrfToken.isValid(request)) { response.sendError(HttpServletResponse.SC_FORBIDDEN); return; }
        String scope = DiscussionScope.fromRequest(request.getParameter("scope")).name();
        com.studenthub.util.AttachmentRequest.Result attachment=com.studenthub.util.AttachmentRequest.read(request.getPart("attachment"));
        if(!attachment.valid()){request.getSession().setAttribute("flashError",attachment.error());response.sendRedirect(request.getContextPath()+"/discussions?scope="+scope);return;}
        try {
            DiscussionService.OperationResult result = service.send(
                    (Long) request.getSession().getAttribute("userId"), scope, request.getParameter("message"),attachment.upload());
            request.getSession().setAttribute(result.successful() ? "flash" : "flashError", result.message());
            if(!result.successful())com.studenthub.util.AttachmentRequest.discard(attachment.upload());
        } catch (SecurityException exception) {
            com.studenthub.util.AttachmentRequest.discard(attachment.upload());
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        } catch (SQLException exception) {
            com.studenthub.util.AttachmentRequest.discard(attachment.upload());
            getServletContext().log("Discussion message send failed: " + exception.getClass().getName());
            request.getSession().setAttribute("flashError", "The message could not be sent right now.");
        }
        response.sendRedirect(request.getContextPath() + "/discussions?scope=" + scope);
    }
}
