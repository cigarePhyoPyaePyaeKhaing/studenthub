package com.studenthub.service;

import com.studenthub.dao.UserDAO;
import com.studenthub.model.UserProfile;
import com.studenthub.util.ProfileValidation;
import java.sql.SQLException;
import java.util.Optional;

public class ProfileService {
    public record UpdateResult(boolean successful, String message, UserProfile profile) {}
    private final UserDAO userDAO = new UserDAO();

    public Optional<UserProfile> findOwnProfile(long authenticatedUserId) throws SQLException {
        return userDAO.findProfileById(authenticatedUserId);
    }

    public UpdateResult updateOwnProfile(long authenticatedUserId, String fullName,
                                         String semester, String section) throws SQLException {
        ProfileValidation.Result validation = ProfileValidation.validate(fullName, semester, section);
        if (!validation.valid()) return new UpdateResult(false, validation.error(), null);
        if (userDAO.updateProfile(authenticatedUserId, validation.update()) != 1) {
            return new UpdateResult(false, "NOT_FOUND", null);
        }
        UserProfile updated = userDAO.findProfileById(authenticatedUserId).orElse(null);
        return updated == null ? new UpdateResult(false, "NOT_FOUND", null)
                : new UpdateResult(true, "Profile updated successfully.", updated);
    }
}
