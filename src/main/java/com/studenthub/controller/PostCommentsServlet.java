package com.studenthub.controller;

import com.studenthub.service.PostEngagementService;
import com.studenthub.util.CsrfToken;
import com.studenthub.util.FeedRedirect;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "PostCommentsServlet", urlPatterns = "/posts/comments")
public class PostCommentsServlet extends HttpServlet {
    private final PostEngagementService service = new PostEngagementService();
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Long postId = FeedRedirect.positiveId(request.getParameter("postId"));
        if (postId == null) { response.sendError(400); return; }
        try {
            var view = service.loadComments((Long) request.getSession().getAttribute("userId"), postId);
            if (view.isEmpty()) { response.sendError(404); return; }
            request.setAttribute("post", view.get().post());
            request.setAttribute("comments", view.get().comments());
        } catch (SQLException exception) {
            getServletContext().log("Post comments load failed: " + exception.getClass().getName());
            request.setAttribute("error", "Comments are temporarily unavailable.");
        }
        request.setAttribute("csrfToken", CsrfToken.getOrCreate(request.getSession()));
        moveFlash(request, "flash", "message"); moveFlash(request, "flashError", "error");
        request.getRequestDispatcher("/WEB-INF/views/posts/comments.jsp").forward(request, response);
    }
    private void moveFlash(HttpServletRequest request, String sessionName, String requestName) {
        Object value = request.getSession().getAttribute(sessionName);
        if (value != null) { request.setAttribute(requestName, value); request.getSession().removeAttribute(sessionName); }
    }
}
