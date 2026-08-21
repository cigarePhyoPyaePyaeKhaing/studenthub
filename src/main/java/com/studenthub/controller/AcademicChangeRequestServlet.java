package com.studenthub.controller;

import com.studenthub.dao.AcademicChangeDAO;
import com.studenthub.util.Authorization;
import com.studenthub.util.CsrfToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;

@WebServlet(name = "AcademicChangeRequestServlet", urlPatterns = "/profile/academic-change")
public class AcademicChangeRequestServlet extends HttpServlet {
    private final AcademicChangeDAO dao;

    public AcademicChangeRequestServlet() {
        this(new AcademicChangeDAO());
    }

    public AcademicChangeRequestServlet(AcademicChangeDAO dao) {
        this.dao = dao;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (!Authorization.isAuthenticated(session)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if (!CsrfToken.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        long userId = (Long) session.getAttribute("userId");
        try {
            String semesterParam = request.getParameter("semester");
            if (semesterParam == null || semesterParam.isBlank()) {
                throw new IllegalArgumentException("Semester is required.");
            }
            int semester = Integer.parseInt(semesterParam.trim());
            String section = request.getParameter("sectionName") == null ? "" : request.getParameter("sectionName").trim().toUpperCase(Locale.ROOT);
            String reason = request.getParameter("reason") == null ? "" : request.getParameter("reason").trim();

            if (semester < 1 || semester > 10 || !section.matches("[A-Z0-9][A-Z0-9 -]{0,19}") || reason.length() < 10 || reason.length() > 1000) {
                throw new IllegalArgumentException("Enter a valid semester (1-10), section, and reason between 10 and 1000 characters.");
            }

            dao.create(userId, semester, section, reason);
            session.setAttribute("flash", "Academic change request submitted for administrator review.");
        } catch (IllegalStateException e) {
            session.setAttribute("flashError", e.getMessage());
        } catch (IllegalArgumentException e) {
            session.setAttribute("flashError", e.getMessage());
        } catch (SQLException e) {
            getServletContext().log("Academic change request failed: " + e.getClass().getName());
            session.setAttribute("flashError", "The request service is temporarily unavailable.");
        }
        response.sendRedirect(request.getContextPath() + "/profile");
    }
}