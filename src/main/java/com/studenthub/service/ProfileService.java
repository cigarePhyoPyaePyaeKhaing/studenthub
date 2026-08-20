package com.studenthub.service;

import com.studenthub.dao.UserDAO;
import com.studenthub.model.UserProfile;
import com.studenthub.util.ProfileValidation;
import java.sql.SQLException;
import java.util.Optional;

public class ProfileService {
    public record UpdateResult(boolean successful, String message, UserProfile profile) {}
    private final UserDAO userDAO;

    public ProfileService() {
        this(new UserDAO());
    }

    public ProfileService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public Optional<UserProfile> findOwnProfile(long authenticatedUserId) throws SQLException {
        return userDAO.findProfileById(authenticatedUserId);
    }

    public UpdateResult updateOwnProfile(long authenticatedUserId, String fullName,
                                         String semester, String section) throws SQLException {
        Optional<UserProfile> current=userDAO.findProfileById(authenticatedUserId);
        if(current.isEmpty())return new UpdateResult(false,"NOT_FOUND",null);
        if(current.get().academicInfoLocked()){
            String normalized=fullName==null?"":fullName.trim();
            if(normalized.length()<2||normalized.length()>100)return new UpdateResult(false,"Enter a full name between 2 and 100 characters.",null);
            if(userDAO.updateFullName(authenticatedUserId,normalized)!=1)return new UpdateResult(false,"NOT_FOUND",null);
            UserProfile updated=userDAO.findProfileById(authenticatedUserId).orElse(null);
            return new UpdateResult(true,"Profile name updated. Academic information remains locked.",updated);
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
}
