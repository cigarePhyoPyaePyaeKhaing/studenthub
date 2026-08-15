package com.studenthub.util;

public final class NotificationScope {
    private NotificationScope() {}
    public static boolean isVisible(Long targetUserId, long viewerId, String visibility,
                                    Integer notificationSemester, String notificationSection,
                                    Integer viewerSemester, String viewerSection) {
        if (targetUserId != null && targetUserId != viewerId) return false;
        if ("ALL".equals(visibility)) return true;
        if (viewerSemester == null || !viewerSemester.equals(notificationSemester)) return false;
        return "SEMESTER".equals(visibility)
                || ("SECTION".equals(visibility) && notificationSection != null
                    && notificationSection.equals(viewerSection));
    }
}
