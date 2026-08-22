<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<nav class="sidebar-content" aria-label="Primary navigation">
    <c:set var="currentPath" value="${pageContext.request.requestURI}" />
    <c:set var="announcementsActive" value="${currentPath eq pageContext.request.contextPath.concat('/announcements') or currentPath.startsWith(pageContext.request.contextPath.concat('/posts/'))}" />
    <c:set var="discussionsActive" value="${currentPath eq pageContext.request.contextPath.concat('/discussions') or currentPath.startsWith(pageContext.request.contextPath.concat('/discussions/'))}" />
    <c:set var="profileActive" value="${currentPath eq pageContext.request.contextPath.concat('/profile') or currentPath.startsWith(pageContext.request.contextPath.concat('/profile/'))}" />
    <a class="dashboard-brand d-none d-lg-flex" href="${pageContext.request.contextPath}/home"><span>S</span> StudentHub</a>
    <div class="sidebar-links">
        <a class="${currentPath eq pageContext.request.contextPath.concat('/home') ? 'active' : ''}" href="${pageContext.request.contextPath}/home">Home</a>
        <a class="${announcementsActive ? 'active' : ''}" href="${pageContext.request.contextPath}/announcements">Announcements</a>
        <a class="notification-link ${currentPath eq pageContext.request.contextPath.concat('/notifications') ? 'active' : ''}" href="${pageContext.request.contextPath}/notifications">Notifications<c:if test="${unreadNotificationCount gt 0}"><span class="notification-badge" aria-label="${unreadNotificationCount} unread"><c:out value="${unreadNotificationCount gt 99 ? '99+' : unreadNotificationCount}" /></span></c:if></a>
        <a class="${discussionsActive ? 'active' : ''}" href="${pageContext.request.contextPath}/discussions">Discussions / Chat</a>
        <a class="${profileActive ? 'active' : ''}" href="${pageContext.request.contextPath}/profile">Profile</a>
        <c:if test="${sessionScope.role eq 'ADMIN'}">
            <a class="${currentPath eq pageContext.request.contextPath.concat('/admin') ? 'active' : ''}" href="${pageContext.request.contextPath}/admin">Admin Dashboard</a>
            <a class="${currentPath.startsWith(pageContext.request.contextPath.concat('/admin/users')) ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/users">Users</a>
            <a class="${currentPath.startsWith(pageContext.request.contextPath.concat('/admin/academic-changes')) ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/academic-changes">Academic Requests</a>
        </c:if>
    </div>
    <div class="sidebar-theme" data-dashboard-theme-slot></div>
    <form method="post" action="${pageContext.request.contextPath}/logout" class="sidebar-logout">
        <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />">
        <button type="submit">Logout</button>
    </form>
</nav>
