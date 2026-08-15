package com.studenthub.util;

public final class PostVisibility {
    private PostVisibility() {}
    public static boolean canView(String visibility, Integer authorSemester, String authorSection,
                                  Integer viewerSemester, String viewerSection) {
        if ("ALL".equals(visibility)) return true;
        if (authorSemester == null || viewerSemester == null || !authorSemester.equals(viewerSemester)) return false;
        return "SEMESTER".equals(visibility)
                || "SECTION".equals(visibility) && authorSection != null && authorSection.equals(viewerSection);
    }
}
