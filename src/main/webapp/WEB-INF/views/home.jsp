<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Home | StudentHub</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="${pageContext.request.contextPath}/assets/js/main.js?v=${applicationScope.assetVersion}" defer></script><link href="${pageContext.request.contextPath}/assets/css/main.css?v=${applicationScope.assetVersion}" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/dashboard.css?v=${applicationScope.assetVersion}" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/dashboard-refined.css?v=${applicationScope.assetVersion}" rel="stylesheet">
</head>
<body class="dashboard-body">
<header class="mobile-header">
    <a class="dashboard-brand" href="${pageContext.request.contextPath}/home"><span>S</span> StudentHub</a>
    <button class="btn btn-light" type="button" data-bs-toggle="offcanvas" data-bs-target="#mobileNavigation">Menu</button>
</header>
<div class="offcanvas offcanvas-start" tabindex="-1" id="mobileNavigation"><div class="offcanvas-header"><h2 class="offcanvas-title h5">StudentHub</h2><button type="button" class="btn-close" data-bs-dismiss="offcanvas"></button></div><div class="offcanvas-body"><jsp:include page="partials/sidebar.jsp" /></div></div>
<div class="dashboard-shell">
    <aside class="dashboard-sidebar"><jsp:include page="partials/sidebar.jsp" /></aside>
    <main class="feed-column">
        <section class="welcome-panel"><div><p class="eyebrow mb-2">University of Information Technology</p><h1>Welcome back, <c:out value="${sessionScope.fullName}" /></h1><p class="mb-0 text-secondary">Here is what is happening in your StudentHub community.</p></div><c:if test="${canCreatePost}"><a class="btn btn-primary create-post-button" href="${pageContext.request.contextPath}/posts/create">Create Post</a></c:if></section>
        <c:if test="${not empty message}"><div class="alert alert-success"><c:out value="${message}" /></div></c:if>
        <c:if test="${not empty dashboardError}"><div class="alert alert-warning"><c:out value="${dashboardError}" /></div></c:if>
        <section class="feed-heading"><div><p class="eyebrow mb-1">Community</p><h2 class="h3 mb-0">Latest updates</h2></div><form method="get" action="${pageContext.request.contextPath}/home" class="category-select-form"><label class="visually-hidden" for="category">Filter by category</label><select class="form-select" id="category" name="category" onchange="this.form.submit()"><option value="">All categories</option><c:forEach var="category" items="${categories}"><option value="${category.categoryId}" <c:if test="${selectedCategory eq category.categoryId}">selected</c:if>><c:out value="${category.categoryName}" /></option></c:forEach></select></form></section>
        <nav class="category-pills" aria-label="Post categories"><a class="category-pill ${empty selectedCategory ? 'active' : ''}" href="${pageContext.request.contextPath}/home">All</a><c:forEach var="category" items="${categories}"><a class="category-pill ${selectedCategory eq category.categoryId ? 'active' : ''}" href="${pageContext.request.contextPath}/home?category=${category.categoryId}"><c:out value="${category.categoryName}" /></a></c:forEach></nav>
        <jsp:include page="partials/post-feed.jsp" />
    </main>
    <aside class="dashboard-right">
        <section class="side-card"><div class="side-card-heading"><h2>Upcoming Deadlines</h2><a href="${pageContext.request.contextPath}/announcements">View all</a></div><c:choose><c:when test="${empty deadlines}"><p class="empty-side-text">No upcoming deadlines.</p></c:when><c:otherwise><div class="deadline-list"><c:forEach var="deadline" items="${deadlines}"><div class="deadline-item"><span class="deadline-dot"></span><div><strong><c:out value="${deadline.title}" /></strong><small><c:out value="${deadline.relativeDueLabel}" /></small></div></div></c:forEach></div></c:otherwise></c:choose></section>
        <section class="side-card account-card"><div class="avatar avatar-large"><jsp:include page="partials/avatar.jsp"><jsp:param name="photo" value="${dashboardProfile.avatarUrl}"/><jsp:param name="initial" value="${sessionScope.fullName.substring(0, 1)}"/></jsp:include></div><h2><c:out value="${sessionScope.fullName}" /></h2><p><c:out value="${sessionScope.studentId}" /></p><span class="account-role role-${sessionScope.role}"><c:out value="${sessionScope.role}" /></span></section>
    </aside>
</div>
<jsp:include page="partials/mobile-bottom-nav.jsp" />
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body></html>
