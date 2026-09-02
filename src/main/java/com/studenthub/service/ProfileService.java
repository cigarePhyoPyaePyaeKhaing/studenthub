package com.studenthub.service;

import com.studenthub.dao.UniversityDAO;
import com.studenthub.dao.UserDAO;
import com.studenthub.model.University;
import com.studenthub.model.UserProfile;
import com.studenthub.model.Role;
import com.studenthub.util.ProfileValidation;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public class ProfileService {
    public record UpdateResult(boolean successful, String message, UserProfile profile) {}
    private final UserDAO userDAO;
    private final UniversityDAO universityDAO;

    public ProfileService() {
        this(new UserDAO(), new UniversityDAO());
    }

    public ProfileService(UserDAO userDAO) {
        this(userDAO, new UniversityDAO());
    }

    public ProfileService(UserDAO userDAO, UniversityDAO universityDAO) {
        this.userDAO = userDAO;
        this.universityDAO = universityDAO;
    }

    public Optional<UserProfile> findOwnProfile(long authenticatedUserId) throws SQLException {
        return userDAO.findProfileById(authenticatedUserId);
    }

    public Optional<UserProfile> findPublicProfile(long userId) throws SQLException {
        return userId > 0 ? userDAO.findProfileById(userId) : Optional.empty();
    }

    public Optional<LocalDateTime> findLastActive(long userId) throws SQLException {
        return userDAO.findLastActive(userId);
    }

    public int findAdminDisplayNumber(long userId) throws SQLException {
        return userDAO.findAdminDisplayNumber(userId);
    }

    public List<University> listAvailableUniversities() {
        if (universityDAO == null) {
            return Collections.emptyList();
        }
        try {
            return universityDAO.listApprovedUniversities();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public UpdateResult updateOwnProfile(long authenticatedUserId, String fullName,
                                         String semester, String section, String universityIdStr) throws SQLException {
        Optional<UserProfile> current = userDAO.findProfileById(authenticatedUserId);
        if (current.isEmpty()) return new UpdateResult(false, "NOT_FOUND", null);

        UserProfile currentProfile = current.get();

        // 1. Handle one-time University selection if provided
        if (universityIdStr != null && !universityIdStr.isBlank()) {
            if (!currentProfile.isUniversityLocked() && currentProfile.getUniversityId() == null) {
                try {
                    long requestedUnivId = Long.parseLong(universityIdStr.trim());
                    if (requestedUnivId <= 0) {
                        return new UpdateResult(false, "Please select a valid approved university.", null);
                    }
                    if (universityDAO != null) {
                        Optional<University> univ = universityDAO.findApprovedById(requestedUnivId);
                        if (univ.isEmpty()) {
                            return new UpdateResult(false, "Selected university is not valid or not approved.", null);
                        }
                    }
                    userDAO.updateUniversityIfUnset(authenticatedUserId, requestedUnivId);
                } catch (NumberFormatException e) {
                    return new UpdateResult(false, "Invalid university identifier.", null);
                }
            }
        }

        // 2. Handle Academic info / Full name updates
        if (currentProfile.getRole() == Role.ADMIN || currentProfile.academicInfoLocked()) {
            String normalized = fullName == null ? "" : fullName.trim();
            if (normalized.length() < 2 || normalized.length() > 100) {
                return new UpdateResult(false, "Enter a full name between 2 and 100 characters.", null);
            }
            if (userDAO.updateFullName(authenticatedUserId, normalized) != 1) {
                return new UpdateResult(false, "NOT_FOUND", null);
            }
            UserProfile updated = userDAO.findProfileById(authenticatedUserId).orElse(null);
            String message = currentProfile.getRole() == Role.ADMIN
                    ? "Profile updated successfully."
                    : "Profile name updated. Academic information remains locked.";
            return new UpdateResult(true, message, updated);
        }

        ProfileValidation.Result validation = ProfileValidation.validate(fullName, semester, section);
        if (!validation.valid()) return new UpdateResult(false, validation.error(), null);
        if (userDAO.updateProfile(authenticatedUserId, validation.update()) != 1) {
            return new UpdateResult(false, "NOT_FOUND", null);
        }
        UserProfile updated = userDAO.findProfileById(authenticatedUserId).orElse(null);
        return updated == null ? new UpdateResult(false, "NOT_FOUND", null)
                : new UpdateResult(true, "Profile updated successfully.", updated);
    }

    public UserProfile updateProfileImage(long authenticatedUserId, String filename) throws SQLException {
        if (userDAO.updateProfileImage(authenticatedUserId, filename) != 1) return null;
        return userDAO.findProfileById(authenticatedUserId).orElse(null);
    }

    public UpdateResult updateOwnProfile(long authenticatedUserId, String fullName,
                                         String semester, String section) throws SQLException {
        return updateOwnProfile(authenticatedUserId, fullName, semester, section, null);
    }
}
