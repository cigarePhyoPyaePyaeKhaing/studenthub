package com.studenthub.controller;

import com.studenthub.dao.AcademicChangeDAO;
import com.studenthub.dao.UserDAO;
import com.studenthub.util.AdminRequest;
import com.studenthub.util.CsrfToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "AdminAcademicChangesServlet", urlPatterns = "/admin/academic-changes")
public class AdminAcademicChangesServlet extends HttpServlet {
    private final AcademicChangeDAO dao;
    private final UserDAO userDAO;

    public AdminAcademicChangesServlet() {
        this(new AcademicChangeDAO(), new UserDAO());
    }

    public AdminAcademicChangesServlet(AcademicChangeDAO dao) {
        this(dao, new UserDAO());
    }

    public AdminAcademicChangesServlet(AcademicChangeDAO dao, UserDAO userDAO) {
        this.dao = dao;
        this.userDAO = userDAO;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!AdminRequest.requireAdmin(request, response, userDAO)) {
            return;
        }
        String status = request.getParameter("status");
        try {
            request.setAttribute("requests", dao.listByStatus(status));
            request.setAttribute("currentStatus", status != null && !status.isBlank() ? status.toUpperCase() : "PENDING");
        } catch (SQLException e) {
            getServletContext().log("Admin academic changes load failed: " + e.getClass().getName());
            request.setAttribute("error", "Requests are temporarily unavailable.");
        }
        request.setAttribute("csrfToken", CsrfToken.getOrCreate(request.getSession()));
        request.getRequestDispatcher("/WEB-INF/views/admin/academic-changes.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!AdminRequest.requireAdmin(request, response, userDAO)) {
            return;
        }
        if (!CsrfToken.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String decision = request.getParameter("decision");
        if (!"approve".equalsIgnoreCase(decision) && !"reject".equalsIgnoreCase(decision)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            long id = Long.parseLong(request.getParameter("id"));
            long adminId = (Long) request.getSession().getAttribute("userId");
            boolean approved = "approve".equalsIgnoreCase(decision);
            String note = request.getParameter("adminNote");

            boolean success = dao.review(id, adminId, approved, note);
            if (!success) {
                request.getSession().setAttribute("flashError", "Request was already reviewed or not found.");
            } else {
                request.getSession().setAttribute("flash", "Academic change request " + (approved ? "approved." : "rejected."));
            }
        } catch (SQLException | NumberFormatException e) {
            getServletContext().log("Admin academic change review failed: " + e.getClass().getName());
            request.getSession().setAttribute("flashError", "Unable to complete review action.");
        }
        response.sendRedirect(request.getContextPath() + "/admin/academic-changes");
    }
}