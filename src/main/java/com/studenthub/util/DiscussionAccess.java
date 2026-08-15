package com.studenthub.util;

import com.studenthub.model.DiscussionScope;

public final class DiscussionAccess {
    private DiscussionAccess() {}

    public static String denialReason(DiscussionScope scope, Integer semester, String sectionName) {
        if (scope == DiscussionScope.ALL) return null;
        if (semester == null) {
            return scope == DiscussionScope.SECTION
                    ? "Your account needs semester and section information to use Section Chat."
                    : "Your account needs semester information to use Semester Chat.";
        }
        if (scope == DiscussionScope.SECTION && (sectionName == null || sectionName.isBlank())) {
            return "Your account needs semester and section information to use Section Chat.";
        }
        return null;
    }

    public static boolean matches(DiscussionScope scope, Integer viewerSemester, String viewerSection,
                                  Integer roomSemester, String roomSection) {
        if (scope == DiscussionScope.ALL) return true;
        if (viewerSemester == null || !viewerSemester.equals(roomSemester)) return false;
        return scope == DiscussionScope.SEMESTER
                || viewerSection != null && viewerSection.equals(roomSection);
    }
}
