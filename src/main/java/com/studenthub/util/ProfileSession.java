package com.studenthub.util;

import com.studenthub.model.UserProfile;
import jakarta.servlet.http.HttpSession;

public final class ProfileSession {
    private ProfileSession() {}

    public record Values(String fullName, Integer semester, String sectionName) {}

    public static Values values(UserProfile profile) {
        return new Values(profile.fullName(), profile.semester(), profile.sectionName());
    }

    public static void refresh(HttpSession session, UserProfile profile) {
        Values values = values(profile);
        session.setAttribute("fullName", values.fullName());
        if (values.semester() == null) session.removeAttribute("semester");
        else session.setAttribute("semester", values.semester());
        if (values.sectionName() == null) session.removeAttribute("sectionName");
        else session.setAttribute("sectionName", values.sectionName());
    }
}
