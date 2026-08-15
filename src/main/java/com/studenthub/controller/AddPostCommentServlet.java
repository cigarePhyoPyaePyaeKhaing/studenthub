package com.studenthub.controller;

import com.studenthub.service.PostEngagementService;
import com.studenthub.util.CsrfToken;
import com.studenthub.util.FeedRedirect;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "AddPostCommentServlet", urlPatterns = "/posts/comments/add")
public class AddPostCommentServlet extends HttpServlet {
    private final PostEngagementService service = new PostEngagementService();
    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        if (!CsrfToken.isValid(request)) { response.sendError(403); return; }
        Long postId = FeedRedirect.positiveId(request.getParameter("postId"));
        if (postId == null) { response.sendError(400); return; }
        try {
            PostEngagementService.OperationResult result = service.addComment(
                    (Long) request.getSession().getAttribute("userId"), postId,
                    request.getParameter("content"));
            if ("NOT_FOUND".equals(result.message())) { response.sendError(404); return; }
            request.getSession().setAttribute(result.successful() ? "flash" : "flashError", result.message());
        } catch (SQLException exception) {
            getServletContext().log("Add comment failed: " + exception.getClass().getName());
            request.getSession().setAttribute("flashError", "Your comment could not be added right now.");
        }
        response.sendRedirect(request.getContextPath() + "/posts/comments?postId=" + postId);
    }
}
