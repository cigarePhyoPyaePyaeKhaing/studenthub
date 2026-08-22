<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<link href="${pageContext.request.contextPath}/assets/css/experience-upgrade.css?v=20260822" rel="stylesheet"><nav class="sidebar-content" aria-label="Primary navigation">
    <c:set var="currentPath" value="${pageContext.request.requestURI}" />
    <a class="dashboard-brand d-none d-lg-flex" href="${pageContext.request.contextPath}/home"><span>S</span> StudentHub</a>
    <div class="sidebar-links">
        <a data-i18n="home" class="${currentPath eq pageContext.request.contextPath.concat('/home') ? 'active' : ''}" href="${pageContext.request.contextPath}/home">Home</a>
        <a data-i18n="announcements" class="${currentPath eq pageContext.request.contextPath.concat('/announcements') ? 'active' : ''}" href="${pageContext.request.contextPath}/announcements">Announcements</a>
        <a class="notification-link ${currentPath eq pageContext.request.contextPath.concat('/notifications') ? 'active' : ''}" href="${pageContext.request.contextPath}/notifications"><span data-i18n="notifications">Notifications</span><c:if test="${unreadNotificationCount gt 0}"><span class="notification-badge" aria-label="${unreadNotificationCount} unread"><c:out value="${unreadNotificationCount gt 99 ? '99+' : unreadNotificationCount}" /></span></c:if></a>
        <a data-i18n="discussions" class="${currentPath eq pageContext.request.contextPath.concat('/discussions') ? 'active' : ''}" href="${pageContext.request.contextPath}/discussions">Discussions / Chat</a>
        <a data-i18n="profile" class="${currentPath eq pageContext.request.contextPath.concat('/profile') ? 'active' : ''}" href="${pageContext.request.contextPath}/profile">Profile</a>
        <c:if test="${sessionScope.role eq 'ADMIN'}">
            <a class="${currentPath eq pageContext.request.contextPath.concat('/admin') ? 'active' : ''}" href="${pageContext.request.contextPath}/admin">Admin Dashboard</a>
            <a class="${currentPath.startsWith(pageContext.request.contextPath.concat('/admin/users')) ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/users">Users</a>
            <a class="${currentPath.startsWith(pageContext.request.contextPath.concat('/admin/academic-changes')) ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/academic-changes">Academic Requests</a>
        </c:if>
    </div>
    <div class="sidebar-theme">
        <div class="sidebar-preferences"><div class="language-switch" aria-label="Language"><button type="button" data-language="en">EN</button><button type="button" data-language="my">မြန်မာ</button></div><button class="simple-theme-toggle theme-control" type="button" data-simple-theme-toggle aria-label="Switch color theme"></button></div>
    </div>
    <form method="post" action="${pageContext.request.contextPath}/logout" class="sidebar-logout">
        <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />">
        <button type="submit" data-i18n="logout">Logout</button>
    </form>
</nav>
<script src="${pageContext.request.contextPath}/assets/js/language.js?v=20260822" defer></script>
