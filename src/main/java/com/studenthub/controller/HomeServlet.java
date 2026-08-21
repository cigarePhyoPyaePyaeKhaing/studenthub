package com.studenthub.controller;

import com.studenthub.service.DashboardService;
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

@WebServlet(name = "HomeServlet", urlPatterns = "/home")
public class HomeServlet extends HttpServlet {
    private final DashboardService dashboardService = new DashboardService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!Authorization.isAuthenticated(request.getSession(false))) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        long startTime = System.currentTimeMillis();
        long userId = (Long) request.getSession().getAttribute("userId");
        Long categoryId = parseCategory(request.getParameter("category"));
        try {
            DashboardService.DashboardData data = dashboardService.load(userId, categoryId);
            request.setAttribute("posts", data.posts());
            request.setAttribute("categories", data.categories());
            request.setAttribute("deadlines", data.deadlines());
        } catch (SQLException exception) {
            logSafe("Dashboard load failed: " + exception.getClass().getName()
                    + ", SQLState=" + exception.getSQLState() + ", code=" + exception.getErrorCode());
            request.setAttribute("posts", List.of());
            request.setAttribute("categories", List.of());
            request.setAttribute("deadlines", List.of());
            request.setAttribute("dashboardError", "Dashboard information is temporarily unavailable.");
        }
        request.setAttribute("selectedCategory", categoryId);
        request.setAttribute("canCreatePost", Authorization.canManagePosts(request.getSession().getAttribute("role")));
        request.setAttribute("csrfToken", CsrfToken.getOrCreate(request.getSession()));
        Object flash = request.getSession().getAttribute("flash");
        if (flash != null) {
            request.setAttribute("message", flash);
            request.getSession().removeAttribute("flash");
        }
        Object flashError = request.getSession().getAttribute("flashError");
        if (flashError != null) {
            request.setAttribute("dashboardError", flashError);
            request.getSession().removeAttribute("flashError");
        }
        logSafe("Home dashboard load completed in " + (System.currentTimeMillis() - startTime) + " ms");
        request.getRequestDispatcher("/WEB-INF/views/home.jsp").forward(request, response);
    }

    private void logSafe(String message) {
        try {
            if (getServletConfig() != null && getServletContext() != null) {
                getServletContext().log(message);
            }
        } catch (Exception ignored) {
        }
    }

    private Long parseCategory(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            long parsed = Long.parseLong(value);
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
