package com.studenthub.util;

import com.studenthub.model.DiscussionScope;

public final class DiscussionAccess {
    private DiscussionAccess() {}

    public static boolean roleMayAccess(DiscussionScope scope, Object role) {
        if (role == null || scope == null) return false;
        String trustedRole = String.valueOf(role);
        if ("ADMIN".equals(trustedRole)) {
            return scope == DiscussionScope.ALL_STUDENTS_ADMIN || scope == DiscussionScope.CR_ADMIN;
        }
        if ("STUDENT".equals(trustedRole)) {
            return scope == DiscussionScope.ALL_STUDENTS_ADMIN || !scope.isCrOnly();
        }
        if ("CR".equals(trustedRole)) {
            return scope != DiscussionScope.ALL_STUDENTS_ADMIN;
        }
        return false;
    }

    public static String denialReason(DiscussionScope scope, Integer semester, String sectionName) {
        return denialReason(scope, 1L, semester, sectionName);
    }
    public static String denialReason(DiscussionScope scope, Long universityId, Integer semester, String sectionName) {
        if (scope == DiscussionScope.ALL || scope == DiscussionScope.CR_ALL
                || scope == DiscussionScope.CR_ADMIN || scope == DiscussionScope.ALL_STUDENTS_ADMIN) return null;
        if (universityId == null || universityId <= 0) return "Select your university to join its academic discussion groups.";
        if (semester == null || semester < 1 || semester > 10) return "Select a valid semester to join this discussion group.";
        if (scope == DiscussionScope.SECTION && (sectionName == null || sectionName.isBlank()))
            return "Select your section to join this discussion group.";
        return null;
    }

    public static boolean matches(DiscussionScope scope, Integer viewerSemester, String viewerSection,
                                  Integer roomSemester, String roomSection) {
        if (scope == DiscussionScope.ALL || scope == DiscussionScope.CR_ALL
                || scope == DiscussionScope.CR_ADMIN || scope == DiscussionScope.ALL_STUDENTS_ADMIN) return true;
        if (viewerSemester == null || !viewerSemester.equals(roomSemester)) return false;
        return scope == DiscussionScope.SEMESTER || scope == DiscussionScope.CR_SEMESTER
                || viewerSection != null && viewerSection.equals(roomSection);
    }
}
