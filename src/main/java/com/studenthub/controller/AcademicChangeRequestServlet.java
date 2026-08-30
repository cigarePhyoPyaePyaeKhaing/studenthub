package com.studenthub.controller;

import com.studenthub.dao.AcademicChangeDAO;
import com.studenthub.model.UserProfile;
import com.studenthub.model.Role;
import com.studenthub.service.ProfileService;
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
import java.util.Optional;

@WebServlet(name = "AcademicChangeRequestServlet", urlPatterns = "/profile/academic-change")
public class AcademicChangeRequestServlet extends HttpServlet {
    private final AcademicChangeDAO dao;
    private final ProfileService profileService;

    public AcademicChangeRequestServlet() {
        this(new AcademicChangeDAO(), new ProfileService());
    }

    public AcademicChangeRequestServlet(AcademicChangeDAO dao) {
        this(dao, new ProfileService());
    }

    public AcademicChangeRequestServlet(AcademicChangeDAO dao, ProfileService profileService) {
        this.dao = dao;
        this.profileService = profileService;
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
            Optional<UserProfile> currentProfile = profileService.findOwnProfile(userId);
            if (currentProfile.isPresent() && currentProfile.get().getRole() == Role.ADMIN) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
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

            if (currentProfile.isPresent()) {
                UserProfile profile = currentProfile.get();
                if (profile.getSemester() != null && profile.getSemester().equals(semester)
                        && profile.getSectionName() != null && profile.getSectionName().trim().equalsIgnoreCase(section)) {
                    throw new IllegalArgumentException("Requested semester and section must be different from your current semester and section.");
                }
            }

            dao.create(userId, semester, section, reason);
            session.setAttribute("flash", "Academic change request submitted for administrator review.");
        } catch (IllegalStateException e) {
            session.setAttribute("flashError", e.getMessage());
        } catch (IllegalArgumentException e) {
            session.setAttribute("flashError", e.getMessage());
        } catch (SQLException e) {
            logSafe("Academic change request failed: " + e.getClass().getName()
                    + ", SQLState=" + e.getSQLState()
                    + ", errorCode=" + e.getErrorCode()
                    + ", message=" + e.getMessage(), e);
            session.setAttribute("flashError", "The request service is temporarily unavailable.");
        }
        response.sendRedirect(request.getContextPath() + "/profile");
    }

    private void logSafe(String message, Throwable throwable) {
        try {
            if (getServletConfig() != null && getServletContext() != null) {
                if (throwable != null) {
                    getServletContext().log(message, throwable);
                } else {
                    getServletContext().log(message);
                }
                return;
            }
        } catch (Exception ignored) {
        }
        System.err.println(message);
        if (throwable != null) {
            throwable.printStackTrace(System.err);
        }
    }
}
