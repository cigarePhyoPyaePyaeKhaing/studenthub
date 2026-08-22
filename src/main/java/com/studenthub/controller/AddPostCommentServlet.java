package com.studenthub.controller;

import com.studenthub.service.PostEngagementService;
import com.studenthub.util.CsrfToken;
import com.studenthub.util.FeedRedirect;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "AddPostCommentServlet", urlPatterns = "/posts/comments/add")
@MultipartConfig(maxFileSize=20971520L,maxRequestSize=22000000L)
public class AddPostCommentServlet extends HttpServlet {
    private final PostEngagementService service = new PostEngagementService();
    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, jakarta.servlet.ServletException {
        request.setCharacterEncoding("UTF-8");
        if (!CsrfToken.isValid(request)) { response.sendError(403); return; }
        Long postId = FeedRedirect.positiveId(request.getParameter("postId"));
        if (postId == null) { response.sendError(400); return; }
        com.studenthub.util.AttachmentRequest.Result attachment=com.studenthub.util.AttachmentRequest.read(request.getPart("attachment"));
        if(!attachment.valid()){request.getSession().setAttribute("flashError",attachment.error());response.sendRedirect(request.getContextPath()+"/posts/comments?postId="+postId);return;}
        try {
            PostEngagementService.OperationResult result = service.addComment(
                    (Long) request.getSession().getAttribute("userId"), postId,
                    request.getParameter("content"),attachment.upload());
            if ("NOT_FOUND".equals(result.message())) { com.studenthub.util.AttachmentRequest.discard(attachment.upload()); response.sendError(404); return; }
            if(!result.successful())com.studenthub.util.AttachmentRequest.discard(attachment.upload());
            request.getSession().setAttribute(result.successful() ? "flash" : "flashError", result.message());
        } catch (SQLException exception) {
            com.studenthub.util.AttachmentRequest.discard(attachment.upload());
            getServletContext().log("Add comment failed: " + exception.getClass().getName());
            request.getSession().setAttribute("flashError", "Your comment could not be added right now.");
        }
        response.sendRedirect(request.getContextPath() + "/posts/comments?postId=" + postId);
    }
}
