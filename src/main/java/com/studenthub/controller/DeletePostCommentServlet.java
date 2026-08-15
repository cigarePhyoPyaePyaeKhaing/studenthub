package com.studenthub.controller;

import com.studenthub.service.PostEngagementService;
import com.studenthub.util.CsrfToken;
import com.studenthub.util.FeedRedirect;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "DeletePostCommentServlet", urlPatterns = "/posts/comments/delete")
public class DeletePostCommentServlet extends HttpServlet {
    private final PostEngagementService service = new PostEngagementService();
    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!CsrfToken.isValid(request)) { response.sendError(403); return; }
        Long commentId = FeedRedirect.positiveId(request.getParameter("commentId"));
        if (commentId == null) { response.sendError(400); return; }
        try {
            var result = service.deleteComment((Long) request.getSession().getAttribute("userId"),
                    request.getSession().getAttribute("role"), commentId);
            if ("FORBIDDEN".equals(result.message())) { response.sendError(403); return; }
            if ("NOT_FOUND".equals(result.message())) { response.sendError(404); return; }
            request.getSession().setAttribute("flash", result.message());
            response.sendRedirect(request.getContextPath() + "/posts/comments?postId=" + result.postId());
        } catch (SQLException exception) {
            getServletContext().log("Delete comment failed: " + exception.getClass().getName());
            response.sendError(503);
        }
    }
}
