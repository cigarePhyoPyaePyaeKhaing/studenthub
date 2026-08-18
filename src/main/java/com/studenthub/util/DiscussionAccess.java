package com.studenthub.util;

import com.studenthub.model.DiscussionScope;

public final class DiscussionAccess {
    private DiscussionAccess() {}

    public static boolean roleMayAccess(DiscussionScope scope, Object role) {
        if (!scope.isCrOnly()) return true;
        String trustedRole = String.valueOf(role);
        return "CR".equals(trustedRole) || "ADMIN".equals(trustedRole);
    }

    public static String denialReason(DiscussionScope scope, Integer semester, String sectionName) {
        if (scope == DiscussionScope.ALL || scope == DiscussionScope.CR_ALL) return null;
        if (semester == null) {
            return scope == DiscussionScope.SECTION
                    ? "Your account needs semester and section information to use Section Chat."
                    : "Your account needs semester information to use this semester room.";
        }
        if (scope == DiscussionScope.SECTION && (sectionName == null || sectionName.isBlank())) {
            return "Your account needs semester and section information to use Section Chat.";
        }
        return null;
    }

    public static boolean matches(DiscussionScope scope, Integer viewerSemester, String viewerSection,
                                  Integer roomSemester, String roomSection) {
        if (scope == DiscussionScope.ALL || scope == DiscussionScope.CR_ALL) return true;
        if (viewerSemester == null || !viewerSemester.equals(roomSemester)) return false;
        return scope == DiscussionScope.SEMESTER || scope == DiscussionScope.CR_SEMESTER
                || viewerSection != null && viewerSection.equals(roomSection);
    }
}
