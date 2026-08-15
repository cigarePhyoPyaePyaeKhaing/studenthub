package com.studenthub.controller;

import com.studenthub.model.UserProfile;
import com.studenthub.service.ProfileService;
import com.studenthub.util.CsrfToken;
import com.studenthub.util.ProfileSession;
import com.studenthub.util.ProfileAuthorization;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet(name = "ProfileServlet", urlPatterns = "/profile")
public class ProfileServlet extends HttpServlet {
    private final ProfileService profileService = new ProfileService();

    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long userId = (Long) request.getSession().getAttribute("userId");
        try {
            Optional<UserProfile> found = profileService.findOwnProfile(userId);
            if (found.isEmpty()) {
                request.getSession().invalidate();
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
            request.setAttribute("profile", found.get());
            request.setAttribute("editing", "true".equalsIgnoreCase(request.getParameter("edit")));
        } catch (SQLException exception) {
            getServletContext().log("Profile load failed: " + exception.getClass().getName());
            request.setAttribute("error", "Your profile is temporarily unavailable.");
        }
        request.setAttribute("csrfToken", CsrfToken.getOrCreate(request.getSession()));
        moveFlash(request, "flash", "message");
        moveFlash(request, "flashError", "error");
        request.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(request, response);
    }

    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        if (!ProfileAuthorization.canSubmitUpdate(CsrfToken.isValid(request))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        try {
            long authenticatedUserId = (Long) request.getSession().getAttribute("userId");
            ProfileService.UpdateResult result = profileService.updateOwnProfile(
                    ProfileAuthorization.updateTarget(authenticatedUserId),
                    request.getParameter("fullName"), request.getParameter("semester"),
                    request.getParameter("sectionName"));
            if ("NOT_FOUND".equals(result.message())) {
                request.getSession().invalidate();
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
            getServletContext().log("Profile update failed: " + exception.getClass().getName());
            request.getSession().setAttribute("flashError", "Your profile could not be updated right now.");
            response.sendRedirect(request.getContextPath() + "/profile?edit=true");
        }
    }

    private void moveFlash(HttpServletRequest request, String sessionName, String requestName) {
        Object value = request.getSession().getAttribute(sessionName);
        if (value != null) {
            request.setAttribute(requestName, value);
            request.getSession().removeAttribute(sessionName);
        }
    }
}
