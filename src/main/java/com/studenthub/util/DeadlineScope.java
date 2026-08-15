package com.studenthub.util;

public final class DeadlineScope {
    public record Resolved(Integer semester, String sectionName) {
    }
    private DeadlineScope() {
    }
    public static Resolved resolve(String requestedScope, Integer authenticatedSemester,
                                   String authenticatedSection) {
        if (authenticatedSemester == null) return new Resolved(null, null);
        if ("SECTION".equals(requestedScope)) {
            String section = authenticatedSection == null || authenticatedSection.isBlank()
                    ? null : authenticatedSection;
            return new Resolved(authenticatedSemester, section);
        }
        return new Resolved(authenticatedSemester, null);
    }
}
