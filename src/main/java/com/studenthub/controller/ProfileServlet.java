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
import java.time.Duration;
import java.time.LocalDateTime;

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
        long authenticatedUserId = (Long) session.getAttribute("userId");
        Long requestedUserId = parseUserId(request.getParameter("userId"));
        if (request.getParameter("userId") != null && requestedUserId == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        long userId = requestedUserId == null ? authenticatedUserId : requestedUserId;
        boolean publicProfile = userId != authenticatedUserId;
        try {
            Optional<UserProfile> found = publicProfile
                    ? profileService.findPublicProfile(userId) : profileService.findOwnProfile(userId);
            if (found.isEmpty()) {
                if (publicProfile) response.sendError(HttpServletResponse.SC_NOT_FOUND);
                else {
                    session.invalidate();
                    response.sendRedirect(request.getContextPath() + "/login");
                }
                return;
            }
            UserProfile profile = found.get();
            boolean editing = !publicProfile && "true".equalsIgnoreCase(request.getParameter("edit"));
            request.setAttribute("profile", profile);
            request.setAttribute("editing", editing);
            request.setAttribute("publicProfile", publicProfile);
            try {
                setPresence(request, profileService.findLastActive(userId).orElse(null));
            } catch (Exception presenceException) {
                setPresence(request, null);
                logSafe("Presence lookup degraded gracefully: " + presenceException.getClass().getName());
            }

            if (editing && !profile.isUniversityLocked()) {
                request.setAttribute("availableUniversities", profileService.listAvailableUniversities());
            } else {
                request.setAttribute("availableUniversities", java.util.Collections.emptyList());
            }

            if (!publicProfile && academicChangeDAO != null) {
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

    private Long parseUserId(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            long parsed = Long.parseLong(value);
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private void setPresence(HttpServletRequest request, LocalDateTime lastActive) {
        if (lastActive == null) {
            request.setAttribute("presenceLabel", "Last seen unavailable");
            return;
        }
        long minutes = Math.max(0, Duration.between(lastActive, LocalDateTime.now()).toMinutes());
        boolean active = minutes < 3;
        request.setAttribute("activeNow", active);
        if (active) request.setAttribute("presenceLabel", "Active now");
        else if (minutes < 60) request.setAttribute("presenceLabel", "Last seen " + minutes + " minutes ago");
        else if (minutes < 1440) request.setAttribute("presenceLabel", "Last seen " + (minutes / 60) + " hours ago");
        else request.setAttribute("presenceLabel", "Last seen " + (minutes / 1440) + " days ago");
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
                PhotoUpdateResult photoResult = applyPhotoChange(authenticatedUserId, result.profile(), photoChange);
                UserProfile updatedProfile = photoResult.profile();
                if (updatedProfile == null) {
                    request.getSession().setAttribute("flashError", photoResult.error());
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

    private PhotoUpdateResult applyPhotoChange(long userId, UserProfile profile, PhotoChange change) throws SQLException {
        if (change.content() == null && !change.remove()) return new PhotoUpdateResult(profile, null);
        String previous = profile.avatarUrl();
        if (change.remove()) {
            UserProfile updated = profileService.updateProfileImage(userId, null);
            if (updated != null) photoStorage.delete(previous);
            return new PhotoUpdateResult(updated, updated == null ? "Your profile photo reference could not be removed." : null);
        }
        if (!photoStorage.isConfigured()) {
            logSafe("Profile photo upload rejected: storage_not_configured");
            return new PhotoUpdateResult(null, "Profile photo storage is not configured right now. Please contact support.");
        }
        if (!photoStorage.ensureWritable()) {
            logSafe("Profile photo upload rejected: storage_not_writable");
            return new PhotoUpdateResult(null, "Profile photo storage is temporarily unavailable. Please try again later.");
        }
        String filename = null;
        try {
            filename = photoStorage.save(change.content(), change.extension());
            UserProfile updated = profileService.updateProfileImage(userId, filename);
            if (updated == null) {
                photoStorage.delete(filename);
                logSafe("Profile photo upload failed: database_update_returned_no_profile");
                return new PhotoUpdateResult(null, "Your profile photo was saved but could not be linked to your account. Please try again.");
            }
            photoStorage.delete(previous);
            return new PhotoUpdateResult(updated, null);
        } catch (IOException exception) {
            if (filename != null) photoStorage.delete(filename);
            logSafe("Profile photo storage failed: " + exception.getClass().getName()
                    + ", storageConfigured=" + photoStorage.isConfigured());
            return new PhotoUpdateResult(null, "Your profile photo could not be saved. Please try again.");
        } catch (SQLException exception) {
            if (filename != null) photoStorage.delete(filename);
            logSafe("Profile photo upload failed: database_update_failed, SQLState=" + exception.getSQLState()
                    + ", errorCode=" + exception.getErrorCode());
            return new PhotoUpdateResult(null, "Your profile photo could not be linked to your account. Please try again.");
        }
    }

    private record PhotoChange(boolean valid, boolean remove, byte[] content, String extension, String error) {}
    private record PhotoUpdateResult(UserProfile profile, String error) {}

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
