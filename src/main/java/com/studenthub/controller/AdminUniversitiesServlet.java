package com.studenthub.controller;

import com.studenthub.dao.UniversityDAO;
import com.studenthub.model.University;
import com.studenthub.util.AdminRequest;
import com.studenthub.util.CsrfToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "AdminUniversitiesServlet", urlPatterns = "/admin/universities")
public class AdminUniversitiesServlet extends HttpServlet {
    private final UniversityDAO universityDAO = new UniversityDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!AdminRequest.requireAdmin(request, response)) {
            return;
        }
        try {
            List<University> list = universityDAO.findAll();
            request.setAttribute("universities", list);
        } catch (SQLException exception) {
            getServletContext().log("Admin universities load failed: " + exception.getClass().getName());
            request.setAttribute("universities", List.of());
            request.setAttribute("error", "Universities are temporarily unavailable.");
        }
        request.setAttribute("csrfToken", CsrfToken.getOrCreate(request.getSession()));
        moveFlash(request);
        request.getRequestDispatcher("/WEB-INF/views/admin/universities.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        if (!AdminRequest.requireAdmin(request, response)) {
            return;
        }
        if (!CsrfToken.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = request.getParameter("action");
        long adminId = (Long) request.getSession().getAttribute("userId");

        try {
            if ("create".equalsIgnoreCase(action)) {
                handleCreate(request, adminId);
            } else if ("activate".equalsIgnoreCase(action)) {
                handleStatusChange(request, adminId, "APPROVED", "University activated.");
            } else if ("deactivate".equalsIgnoreCase(action)) {
                handleStatusChange(request, adminId, "INACTIVE", "University deactivated.");
            } else if ("edit".equalsIgnoreCase(action)) {
                handleEdit(request);
            } else {
                request.getSession().setAttribute("flashError", "Invalid action requested.");
            }
        } catch (SQLException exception) {
            getServletContext().log("University operation failed: " + exception.getClass().getName(), exception);
            request.getSession().setAttribute("flashError", "The university operation could not be completed.");
        }
        response.sendRedirect(request.getContextPath() + "/admin/universities");
    }

    private void handleCreate(HttpServletRequest request, long adminId) throws SQLException {
        String name = request.getParameter("name");
        String shortName = request.getParameter("shortName");
        if (name == null || name.trim().length() < 2 || name.trim().length() > 180) {
            request.getSession().setAttribute("flashError", "University name must be between 2 and 180 characters.");
            return;
        }
        if (shortName != null && !shortName.isBlank() && shortName.trim().length() > 30) {
            request.getSession().setAttribute("flashError", "Short name must be 30 characters or fewer.");
            return;
        }
        Optional<University> existing = universityDAO.findByNameOrShortName(name, shortName);
        if (existing.isPresent()) {
            request.getSession().setAttribute("flashError", "A university with that name or short name already exists.");
            return;
        }
        universityDAO.create(name, shortName, "APPROVED", adminId);
        request.getSession().setAttribute("flash", "University added and activated successfully.");
    }

    private void handleStatusChange(HttpServletRequest request, long adminId, String status, String successMessage)
            throws SQLException {
        Long id = AdminRequest.positiveId(request.getParameter("id"));
        if (id == null) {
            request.getSession().setAttribute("flashError", "Invalid university selected.");
            return;
        }
        Optional<University> found = universityDAO.findById(id);
        if (found.isEmpty()) {
            request.getSession().setAttribute("flashError", "University not found.");
            return;
        }
        universityDAO.updateStatus(id, status, adminId);
        request.getSession().setAttribute("flash", successMessage);
    }

    private void handleEdit(HttpServletRequest request) throws SQLException {
        Long id = AdminRequest.positiveId(request.getParameter("id"));
        String name = request.getParameter("name");
        String shortName = request.getParameter("shortName");
        if (id == null || name == null || name.trim().length() < 2 || name.trim().length() > 180) {
            request.getSession().setAttribute("flashError", "Enter a valid university name (2-180 characters).");
            return;
        }
        Optional<University> duplicate = universityDAO.findByNameOrShortName(name, shortName);
        if (duplicate.isPresent() && duplicate.get().universityId() != id) {
            request.getSession().setAttribute("flashError", "Another university already uses that name or abbreviation.");
            return;
        }
        universityDAO.updateMetadata(id, name, shortName);
        request.getSession().setAttribute("flash", "University details updated.");
    }

    private void moveFlash(HttpServletRequest request) {
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
    }
}
