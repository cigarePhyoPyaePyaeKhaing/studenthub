<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<nav class="mobile-bottom-nav" aria-label="Mobile navigation">
    <a href="${pageContext.request.contextPath}/home" class="${activeNav eq 'HOME' ? 'active' : ''}" aria-label="Home" ${activeNav eq 'HOME' ? 'aria-current="page"' : ''}>
        <span class="mobile-nav-icon" aria-hidden="true">
            <svg class="mobile-nav-svg" viewBox="0 0 24 24" aria-hidden="true"><path d="M3 9.5L12 3l9 7.5V20a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>
        </span>
        <span class="mobile-nav-label">Home</span>
    </a>
    <a href="${pageContext.request.contextPath}/announcements" class="${activeNav eq 'ANNOUNCEMENTS' ? 'active' : ''}" aria-label="Announcements" ${activeNav eq 'ANNOUNCEMENTS' ? 'aria-current="page"' : ''}>
        <span class="mobile-nav-icon" aria-hidden="true">
            <svg class="mobile-nav-svg nav-icon-announcements" viewBox="0 0 24 24" aria-hidden="true"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><line x1="10" y1="9" x2="8" y2="9"/></svg>
        </span>
        <span class="mobile-nav-label">Announcements</span>
    </a>
    <a href="${pageContext.request.contextPath}/notifications" class="${activeNav eq 'NOTIFICATIONS' ? 'active' : ''}" aria-label="Notifications" ${activeNav eq 'NOTIFICATIONS' ? 'aria-current="page"' : ''}>
        <span class="mobile-nav-icon" aria-hidden="true">
            <svg class="mobile-nav-svg nav-icon-bell" viewBox="0 0 24 24" aria-hidden="true"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
            <c:if test="${unreadNotificationCount gt 0}"><span class="notification-badge" aria-label="${unreadNotificationCount} unread"><c:out value="${unreadNotificationCount gt 99 ? '99+' : unreadNotificationCount}" /></span></c:if>
        </span>
        <span class="mobile-nav-label">Notifications</span>
    </a>
    <a href="${pageContext.request.contextPath}/messages" class="${activeNav eq 'DISCUSSIONS' ? 'active' : ''}" aria-label="Discussions" ${activeNav eq 'DISCUSSIONS' ? 'aria-current="page"' : ''}>
        <span class="mobile-nav-icon" aria-hidden="true">
            <svg class="mobile-nav-svg nav-icon-chat" viewBox="0 0 24 24" aria-hidden="true"><path d="M21 11.5a8.38 8.38 0 0 1-.9 3.8 8.5 8.5 0 0 1-7.6 4.7 8.38 8.38 0 0 1-3.8-.9L3 21l1.9-5.7a8.38 8.38 0 0 1-.9-3.8 8.5 8.5 0 0 1 4.7-7.6 8.38 8.38 0 0 1 3.8-.9h.5a8.48 8.48 0 0 1 8 8v.5z"/></svg>
            <c:if test="${unreadPrivateMessageCount gt 0}"><span class="notification-badge" aria-label="${unreadPrivateMessageCount} unread"><c:out value="${unreadPrivateMessageCount gt 99 ? '99+' : unreadPrivateMessageCount}" /></span></c:if>
        </span>
        <span class="mobile-nav-label">Discussions</span>
    </a>
    <a href="${pageContext.request.contextPath}/profile" class="${activeNav eq 'PROFILE' ? 'active' : ''}" aria-label="Profile" ${activeNav eq 'PROFILE' ? 'aria-current="page"' : ''}>
        <span class="mobile-nav-icon" aria-hidden="true">
            <svg class="mobile-nav-svg nav-icon-user" viewBox="0 0 24 24" aria-hidden="true"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
        </span>
        <span class="mobile-nav-label">Profile</span>
    </a>
</nav>
