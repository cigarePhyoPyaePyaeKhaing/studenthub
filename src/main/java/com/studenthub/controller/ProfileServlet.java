package com.studenthub.controller;

import com.studenthub.dao.AcademicChangeDAO;
import com.studenthub.model.UserProfile;
import com.studenthub.service.ProfileService;
import com.studenthub.util.Authorization;
import com.studenthub.util.CsrfToken;
import com.studenthub.util.ProfileAuthorization;
import com.studenthub.util.ProfileSession;
import com.studenthub.util.ProfilePhotoStorage;
import com.studenthub.util.ProfilePhotoValidator;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet(name = "ProfileServlet", urlPatterns = "/profile")
@MultipartConfig(maxFileSize = ProfilePhotoValidator.MAX_BYTES, maxRequestSize = 2300000L)
public class ProfileServlet extends HttpServlet {
    private final ProfileService profileService;
    private final AcademicChangeDAO academicChangeDAO;
    private final ProfilePhotoStorage photoStorage = new ProfilePhotoStorage();

    public ProfileServlet() {
        this(new ProfileService(), new AcademicChangeDAO());
    }

    public ProfileServlet(ProfileService profileService) {
        this(profileService, new AcademicChangeDAO());
    }

    public ProfileServlet(ProfileService profileService, AcademicChangeDAO academicChangeDAO) {
        this.profileService = profileService;
        this.academicChangeDAO = academicChangeDAO;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (!Authorization.isAuthenticated(session)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        long startTime = System.currentTimeMillis();
        long userId = (Long) session.getAttribute("userId");
        try {
            Optional<UserProfile> found = profileService.findOwnProfile(userId);
            if (found.isEmpty()) {
                session.invalidate();
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
            UserProfile profile = found.get();
            boolean editing = "true".equalsIgnoreCase(request.getParameter("edit"));
            request.setAttribute("profile", profile);
            request.setAttribute("editing", editing);

            if (editing && !profile.isUniversityLocked()) {
                request.setAttribute("availableUniversities", profileService.listAvailableUniversities());
            } else {
                request.setAttribute("availableUniversities", java.util.Collections.emptyList());
            }

            if (academicChangeDAO != null) {
                try {
                    request.setAttribute("pendingAcademicRequest", academicChangeDAO.findPendingForUser(userId).orElse(null));
                } catch (Exception academicException) {
                    logSafe("Pending academic request lookup degraded gracefully: "
                            + academicException.getClass().getName() + " - " + academicException.getMessage());
                    request.setAttribute("pendingAcademicRequest", null);
                    request.setAttribute("academicRequestUnavailable", true);
                }
            }
        } catch (SQLException exception) {
            logSafe("Profile load failed: " + exception.getClass().getName()
                    + ", SQLState=" + exception.getSQLState()
                    + ", errorCode=" + exception.getErrorCode()
                    + ", message=" + exception.getMessage(), exception);
            request.setAttribute("error", "Your profile is temporarily unavailable.");
        } catch (Exception exception) {
            logSafe("Unexpected profile error: " + exception.getClass().getName()
                    + " - " + exception.getMessage(), exception);
            request.setAttribute("error", "Your profile is temporarily unavailable.");
        }
        request.setAttribute("csrfToken", CsrfToken.getOrCreate(request.getSession()));
        moveFlash(request, "flash", "message");
        moveFlash(request, "flashError", "error");
        if (request.getAttribute("error") == null) {
            logSafe("Profile load completed in " + (System.currentTimeMillis() - startTime) + " ms");
        }
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

        try {
            if (!ProfileAuthorization.canSubmitUpdate(CsrfToken.isValid(request))) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
        } catch (IllegalStateException exception) {
            request.getSession().setAttribute("flashError", "Choose a JPG, PNG, or WEBP image no larger than 2 MB.");
            response.sendRedirect(request.getContextPath() + "/profile?edit=true");
            return;
        }
        try {
            long authenticatedUserId = (Long) session.getAttribute("userId");
            PhotoChange photoChange = readPhotoChange(request);
            if (!photoChange.valid()) {
                request.getSession().setAttribute("flashError", photoChange.error());
                response.sendRedirect(request.getContextPath() + "/profile?edit=true");
                return;
            }
            ProfileService.UpdateResult result = profileService.updateOwnProfile(
                    ProfileAuthorization.updateTarget(authenticatedUserId),
                    request.getParameter("fullName"),
                    request.getParameter("semester"),
                    request.getParameter("sectionName"),
                    request.getParameter("universityId"));
            if ("NOT_FOUND".equals(result.message())) {
                session.invalidate();
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
            if (result.successful()) {
                UserProfile updatedProfile = applyPhotoChange(authenticatedUserId, result.profile(), photoChange);
                if (updatedProfile == null) {
                    request.getSession().setAttribute("flashError", "Your profile photo could not be updated right now.");
                    response.sendRedirect(request.getContextPath() + "/profile?edit=true");
                    return;
                }
                ProfileSession.refresh(request.getSession(), updatedProfile);
                request.getSession().setAttribute("flash", result.message());
                response.sendRedirect(request.getContextPath() + "/profile");
            } else {
                request.getSession().setAttribute("flashError", result.message());
                response.sendRedirect(request.getContextPath() + "/profile?edit=true");
            }
        } catch (SQLException exception) {
            logSafe("Profile update failed: " + exception.getClass().getName()
                    + ", SQLState=" + exception.getSQLState()
                    + ", errorCode=" + exception.getErrorCode()
                    + ", message=" + exception.getMessage(), exception);
            request.getSession().setAttribute("flashError", "Your profile could not be updated right now.");
            response.sendRedirect(request.getContextPath() + "/profile?edit=true");
        } catch (IllegalStateException | ServletException exception) {
            request.getSession().setAttribute("flashError", "Choose a JPG, PNG, or WEBP image no larger than 2 MB.");
            response.sendRedirect(request.getContextPath() + "/profile?edit=true");
        }
    }

    private PhotoChange readPhotoChange(HttpServletRequest request) throws IOException, ServletException {
        boolean remove = "true".equals(request.getParameter("removePhoto"));
        Part part = request.getPart("profilePhoto");
        if (part == null || part.getSize() == 0) return new PhotoChange(true, remove, null, null, null);
        if (part.getSize() > ProfilePhotoValidator.MAX_BYTES) {
            return new PhotoChange(false, false, null, null, "Profile photo must be 2 MB or smaller.");
        }
        byte[] content = part.getInputStream().readNBytes((int) ProfilePhotoValidator.MAX_BYTES + 1);
        Optional<String> extension = ProfilePhotoValidator.validatedExtension(part.getContentType(), content);
        return extension.map(value -> new PhotoChange(true, false, content, value, null))
                .orElseGet(() -> new PhotoChange(false, false, null, null,
                        "Choose a valid JPG, PNG, or WEBP image."));
    }

    private UserProfile applyPhotoChange(long userId, UserProfile profile, PhotoChange change) throws SQLException {
        if (change.content() == null && !change.remove()) return profile;
        String previous = profile.avatarUrl();
        if (change.remove()) {
            UserProfile updated = profileService.updateProfileImage(userId, null);
            if (updated != null) photoStorage.delete(previous);
            return updated;
        }
        String filename = null;
        try {
            filename = photoStorage.save(change.content(), change.extension());
            UserProfile updated = profileService.updateProfileImage(userId, filename);
            if (updated == null) {
                photoStorage.delete(filename);
                return null;
            }
            photoStorage.delete(previous);
            return updated;
        } catch (IOException exception) {
            if (filename != null) photoStorage.delete(filename);
            return null;
        } catch (SQLException exception) {
            if (filename != null) photoStorage.delete(filename);
            throw exception;
        }
    }

    private record PhotoChange(boolean valid, boolean remove, byte[] content, String extension, String error) {}

    private void logSafe(String message) {
        logSafe(message, null);
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
