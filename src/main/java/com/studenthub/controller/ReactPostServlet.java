package com.studenthub.controller;

import com.studenthub.service.PostEngagementService;
import com.studenthub.util.CsrfToken;
import com.studenthub.util.FeedRedirect;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "ReactPostServlet", urlPatterns = "/posts/react")
public class ReactPostServlet extends HttpServlet {
    private final PostEngagementService service = new PostEngagementService();
    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!CsrfToken.isValid(request)) { response.sendError(403); return; }
        Long postId = FeedRedirect.positiveId(request.getParameter("postId"));
        if (postId == null) { response.sendError(400); return; }
        try {
            PostEngagementService.OperationResult result = service.toggleLike(
                    (Long) request.getSession().getAttribute("userId"), postId);
            if ("NOT_FOUND".equals(result.message())) { response.sendError(404); return; }
        } catch (SQLException exception) {
            getServletContext().log("Post reaction failed: " + exception.getClass().getName());
            request.getSession().setAttribute("flashError", "Your reaction could not be saved right now.");
        }
        response.sendRedirect(request.getContextPath() + FeedRedirect.path(request.getParameter("source"),
                FeedRedirect.positiveId(request.getParameter("category"))));
    }
}
