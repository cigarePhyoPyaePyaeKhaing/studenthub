package com.studenthub.controller;

import com.studenthub.model.UserProfile;
import com.studenthub.service.ProfileService;
import com.studenthub.util.Authorization;
import com.studenthub.util.CsrfToken;
import com.studenthub.util.ProfileAuthorization;
import com.studenthub.util.ProfileSession;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet(name = "ProfileServlet", urlPatterns = "/profile")
public class ProfileServlet extends HttpServlet {
    private final ProfileService profileService;

    public ProfileServlet() {
        this(new ProfileService());
    }

    public ProfileServlet(ProfileService profileService) {
        this.profileService = profileService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (!Authorization.isAuthenticated(session)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        long userId = (Long) session.getAttribute("userId");
        try {
            Optional<UserProfile> found = profileService.findOwnProfile(userId);
            if (found.isEmpty()) {
                session.invalidate();
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
            request.setAttribute("profile", found.get());
            request.setAttribute("editing", "true".equalsIgnoreCase(request.getParameter("edit")));
        } catch (SQLException exception) {
            getServletContext().log("Profile load failed: " + exception.getClass().getName()
                    + ", SQLState=" + exception.getSQLState()
                    + ", errorCode=" + exception.getErrorCode()
                    + ", message=" + exception.getMessage(), exception);
            request.setAttribute("error", "Your profile is temporarily unavailable.");
        }
        request.setAttribute("csrfToken", CsrfToken.getOrCreate(request.getSession()));
        moveFlash(request, "flash", "message");
        moveFlash(request, "flashError", "error");
        request.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        if (!Authorization.isAuthenticated(session)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if (!ProfileAuthorization.canSubmitUpdate(CsrfToken.isValid(request))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        try {
            long authenticatedUserId = (Long) session.getAttribute("userId");
            ProfileService.UpdateResult result = profileService.updateOwnProfile(
                    ProfileAuthorization.updateTarget(authenticatedUserId),
                    request.getParameter("fullName"), request.getParameter("semester"),
                    request.getParameter("sectionName"));
            if ("NOT_FOUND".equals(result.message())) {
                session.invalidate();
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
            if (result.successful()) {
                ProfileSession.refresh(request.getSession(), result.profile());
                request.getSession().setAttribute("flash", result.message());
                response.sendRedirect(request.getContextPath() + "/profile");
            } else {
                request.getSession().setAttribute("flashError", result.message());
                response.sendRedirect(request.getContextPath() + "/profile?edit=true");
            }
        } catch (SQLException exception) {
            getServletContext().log("Profile update failed: " + exception.getClass().getName()
                    + ", SQLState=" + exception.getSQLState()
                    + ", errorCode=" + exception.getErrorCode()
                    + ", message=" + exception.getMessage(), exception);
            request.getSession().setAttribute("flashError", "Your profile could not be updated right now.");
            response.sendRedirect(request.getContextPath() + "/profile?edit=true");
        }
    }

    private void moveFlash(HttpServletRequest request, String sessionName, String requestName) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object value = session.getAttribute(sessionName);
            if (value != null) {
                request.setAttribute(requestName, value);
                session.removeAttribute(sessionName);
            }
        }
    }
}
