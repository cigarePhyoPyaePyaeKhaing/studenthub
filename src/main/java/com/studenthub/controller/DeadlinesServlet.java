package com.studenthub.controller;

import com.studenthub.dao.PostDAO;
import com.studenthub.util.Authorization;
import com.studenthub.util.CsrfToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name="DeadlinesServlet",urlPatterns="/deadlines")
public class DeadlinesServlet extends HttpServlet{
    private final PostDAO postDAO = new PostDAO();

    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!Authorization.isAuthenticated(request.getSession(false))) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        long userId = (Long) request.getSession().getAttribute("userId");
        try {
            request.setAttribute("upcomingDeadlines", postDAO.findUpcomingDeadlinesForUser(userId, 100));
            request.setAttribute("pastDeadlines", postDAO.findPastDeadlinesForUser(userId, 50));
        } catch (SQLException exception) {
            getServletContext().log("Deadlines load failed: " + exception.getClass().getName());
            request.setAttribute("upcomingDeadlines", List.of());
            request.setAttribute("pastDeadlines", List.of());
            request.setAttribute("error", "Deadlines are temporarily unavailable.");
        }
        request.setAttribute("canCreateDeadline", Authorization.canManagePosts(request.getSession().getAttribute("role")));
        request.setAttribute("csrfToken", CsrfToken.getOrCreate(request.getSession()));
        Object flash = request.getSession().getAttribute("flash");
        if (flash != null) {
            request.setAttribute("message", flash);
            request.getSession().removeAttribute("flash");
        }
        request.getRequestDispatcher("/WEB-INF/views/deadlines/index.jsp").forward(request, response);
    }
}
