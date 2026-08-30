<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<nav class="sidebar-content" aria-label="Primary navigation">
    <c:set var="homeActive" value="${activeNav eq 'HOME'}" />
    <c:set var="announcementsActive" value="${activeNav eq 'ANNOUNCEMENTS'}" />
    <c:set var="notificationsActive" value="${activeNav eq 'NOTIFICATIONS'}" />
    <c:set var="discussionsActive" value="${activeNav eq 'DISCUSSIONS'}" />
    <c:set var="profileActive" value="${activeNav eq 'PROFILE'}" />
    <c:set var="adminDashboardActive" value="${activeNav eq 'ADMIN_DASHBOARD'}" />
    <c:set var="adminUsersActive" value="${activeNav eq 'ADMIN_USERS'}" />
    <c:set var="adminAcademicActive" value="${activeNav eq 'ADMIN_ACADEMIC_REQUESTS'}" />
    <a class="dashboard-brand" href="${pageContext.request.contextPath}/home"><jsp:include page="logo.jsp" /></a>
    <div class="sidebar-links">
        <a class="${homeActive ? 'active' : ''}" href="${pageContext.request.contextPath}/home" ${homeActive ? 'aria-current="page"' : ''}>Home</a>
        <a class="${announcementsActive ? 'active' : ''}" href="${pageContext.request.contextPath}/announcements" ${announcementsActive ? 'aria-current="page"' : ''}>Announcements</a>
        <a class="notification-link ${notificationsActive ? 'active' : ''}" href="${pageContext.request.contextPath}/notifications" ${notificationsActive ? 'aria-current="page"' : ''}>Notifications<c:if test="${unreadNotificationCount gt 0}"><span class="notification-badge" aria-label="${unreadNotificationCount} unread"><c:out value="${unreadNotificationCount gt 99 ? '99+' : unreadNotificationCount}" /></span></c:if></a>
        <a class="notification-link ${discussionsActive ? 'active' : ''}" href="${pageContext.request.contextPath}/messages" ${discussionsActive ? 'aria-current="page"' : ''}>Discussions / Chat<c:if test="${unreadPrivateMessageCount gt 0}"><span class="private-message-badge notification-badge"><c:out value="${unreadPrivateMessageCount gt 99 ? '99+' : unreadPrivateMessageCount}"/></span></c:if></a>
        <a class="${profileActive ? 'active' : ''}" href="${pageContext.request.contextPath}/profile" ${profileActive ? 'aria-current="page"' : ''}>Profile</a>
        <c:if test="${sessionScope.role eq 'ADMIN'}">
            <a class="${adminDashboardActive ? 'active' : ''}" href="${pageContext.request.contextPath}/admin" ${adminDashboardActive ? 'aria-current="page"' : ''}>Admin Dashboard</a>
            <a class="${adminUsersActive ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/users" ${adminUsersActive ? 'aria-current="page"' : ''}>Users</a>
            <a class="${adminAcademicActive ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/academic-changes" ${adminAcademicActive ? 'aria-current="page"' : ''}>Academic Requests</a>
        </c:if>
    </div>
    <div class="sidebar-theme" data-dashboard-theme-slot></div>
    <form method="post" action="${pageContext.request.contextPath}/logout" class="sidebar-logout">
        <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />">
        <button type="submit">Logout</button>
    </form>
</nav>
