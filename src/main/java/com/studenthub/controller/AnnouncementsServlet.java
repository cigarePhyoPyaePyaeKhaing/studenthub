package com.studenthub.controller;

import com.studenthub.dao.CategoryDAO;
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

@WebServlet(name = "AnnouncementsServlet", urlPatterns = "/announcements")
public class AnnouncementsServlet extends HttpServlet {
    private final PostDAO postDAO = new PostDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!Authorization.isAuthenticated(request.getSession(false))) {
            response.sendRedirect(request.getContextPath() + "/login"); return;
        }
        Long categoryId = parseId(request.getParameter("category"));
        try {
            request.setAttribute("posts", postDAO.findVisibleForUser((Long) request.getSession().getAttribute("userId"), categoryId));
            request.setAttribute("categories", categoryDAO.findAll());
        } catch (SQLException exception) {
            getServletContext().log("Announcements load failed: " + exception.getClass().getName());
            request.setAttribute("posts", List.of()); request.setAttribute("categories", List.of());
            request.setAttribute("error", "Announcements are temporarily unavailable.");
        }
        request.setAttribute("selectedCategory", categoryId);
        request.setAttribute("canCreatePost", Authorization.canManagePosts(request.getSession().getAttribute("role")));
        request.setAttribute("csrfToken", CsrfToken.getOrCreate(request.getSession()));
        Object flash = request.getSession().getAttribute("flash");
        if (flash != null) { request.setAttribute("message", flash); request.getSession().removeAttribute("flash"); }
        Object flashError = request.getSession().getAttribute("flashError");
        if (flashError != null) { request.setAttribute("error", flashError); request.getSession().removeAttribute("flashError"); }
        request.getRequestDispatcher("/WEB-INF/views/announcements.jsp").forward(request, response);
    }

    private Long parseId(String value) {
        try { long id = Long.parseLong(value); return id > 0 ? id : null; }
        catch (Exception exception) { return null; }
    }
}
