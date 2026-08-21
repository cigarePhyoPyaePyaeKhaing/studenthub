package com.studenthub.controller;

import com.studenthub.service.DiscussionService;
import com.studenthub.util.CsrfToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "DiscussionsServlet", urlPatterns = "/discussions")
public class DiscussionsServlet extends HttpServlet {
    private final DiscussionService service = new DiscussionService();

    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long userId = (Long) request.getSession().getAttribute("userId");
        try {
            request.setAttribute("room", service.load(userId, request.getParameter("scope")));
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        } catch (SQLException exception) {
            Throwable rootCause = exception.getCause();
            String rootCauseInfo = rootCause != null ? (", rootCause=" + rootCause.getClass().getName() + ": " + rootCause.getMessage()) : "";
            getServletContext().log("Discussion load failed: " + exception.getClass().getName()
                    + ", SQLState=" + exception.getSQLState() + ", code=" + exception.getErrorCode()
                    + ", message=" + exception.getMessage() + rootCauseInfo, exception);
            request.setAttribute("error", "Discussions are temporarily unavailable.");
        }
        request.setAttribute("csrfToken", CsrfToken.getOrCreate(request.getSession()));
        Object flash = request.getSession().getAttribute("flash");
        if (flash != null) {
            request.setAttribute("message", flash);
            request.getSession().removeAttribute("flash");
        }
        Object flashError = request.getSession().getAttribute("flashError");
        if (flashError != null) {
            request.setAttribute("error", flashError);
            request.getSession().removeAttribute("flashError");
        }
        request.getRequestDispatcher("/WEB-INF/views/discussions/index.jsp").forward(request, response);
    }
}
