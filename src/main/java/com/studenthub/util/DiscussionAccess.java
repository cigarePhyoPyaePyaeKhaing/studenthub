package com.studenthub.util;

import com.studenthub.model.DiscussionScope;

public final class DiscussionAccess {
    private DiscussionAccess() {}

    public static boolean roleMayAccess(DiscussionScope scope, Object role) {
        if (role == null) return false;
        if (!scope.isCrOnly()) return true;
        String trustedRole = String.valueOf(role);
        return "CR".equals(trustedRole) || "ADMIN".equals(trustedRole);
    }

    public static String denialReason(DiscussionScope scope, Integer semester, String sectionName) {
        return denialReason(scope, 1L, semester, sectionName);
    }
    public static String denialReason(DiscussionScope scope, Long universityId, Integer semester, String sectionName) {
        if (scope == DiscussionScope.ALL || scope == DiscussionScope.CR_ALL) return null;
        if (universityId == null || universityId <= 0 || semester == null || sectionName == null || sectionName.isBlank()) {
            return "Complete your university, semester and section information to join your academic discussion groups.";
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
