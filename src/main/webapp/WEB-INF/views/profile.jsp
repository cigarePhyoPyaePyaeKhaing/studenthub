<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="en"><head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Profile | StudentHub</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="${pageContext.request.contextPath}/assets/js/main.js"></script><link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/dashboard.css" rel="stylesheet">
</head><body class="dashboard-body">
<header class="mobile-header d-lg-none"><a class="dashboard-brand" href="${pageContext.request.contextPath}/home"><span>S</span> StudentHub</a><button class="btn btn-light" type="button" data-bs-toggle="offcanvas" data-bs-target="#mobileNavigation">Menu</button></header>
<div class="offcanvas offcanvas-start" tabindex="-1" id="mobileNavigation"><div class="offcanvas-header"><h2 class="offcanvas-title h5">StudentHub</h2><button type="button" class="btn-close" data-bs-dismiss="offcanvas"></button></div><div class="offcanvas-body"><jsp:include page="partials/sidebar.jsp" /></div></div>
<div class="dashboard-shell profile-shell"><aside class="dashboard-sidebar d-none d-lg-flex"><jsp:include page="partials/sidebar.jsp" /></aside>
<main class="profile-column">
    <c:if test="${not empty message}"><div class="alert alert-success"><c:out value="${message}" /></div></c:if>
    <c:if test="${not empty error}"><div class="alert alert-warning"><c:out value="${error}" /></div></c:if>
    <c:if test="${not empty profile}">
    <section class="profile-hero"><div class="profile-avatar"><c:out value="${profile.initial}" /></div><div><p class="eyebrow mb-1">My StudentHub account</p><h1><c:out value="${profile.fullName}" /></h1><p><c:out value="${profile.studentId}" /></p></div><span class="profile-role role-${profile.role}"><c:out value="${profile.role}" /></span></section>
    <c:choose><c:when test="${editing}">
        <section class="profile-card"><div class="profile-card-heading"><div><p class="eyebrow mb-1">Account and academic details</p><h2>Edit profile</h2></div></div>
        <form method="post" action="${pageContext.request.contextPath}/profile" class="profile-form"><input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />">
            <div><label for="fullName">Full name</label><input class="form-control" id="fullName" name="fullName" maxlength="100" value="<c:out value='${profile.fullName}' />" required></div>
            <div><label for="semester">Semester</label><select class="form-select" id="semester" name="semester"><option value="">Not set</option><c:forEach begin="1" end="10" var="number"><option value="${number}" <c:if test="${profile.semester eq number}">selected</c:if>>Semester ${number}</option></c:forEach></select></div>
            <div><label for="sectionName">Section</label><input class="form-control" id="sectionName" name="sectionName" maxlength="20" pattern="[A-Za-z0-9][A-Za-z0-9 -]{0,19}" value="<c:out value='${profile.sectionName}' />" placeholder="For example: B"></div>
            <p class="profile-security-note">Semester and section control access to scoped announcements, deadlines, and discussions.</p>
            <div class="profile-form-actions"><button class="btn btn-primary" type="submit">Save changes</button><a class="btn btn-light" href="${pageContext.request.contextPath}/profile">Cancel</a></div>
        </form></section>
    </c:when><c:otherwise>
        <div class="profile-grid"><section class="profile-card"><div class="profile-card-heading"><div><p class="eyebrow mb-1">Identity</p><h2>Account information</h2></div><a class="btn btn-primary btn-sm" href="${pageContext.request.contextPath}/profile?edit=true">Edit profile</a></div><dl class="profile-details"><div><dt>Full name</dt><dd><c:out value="${profile.fullName}" /></dd></div><div><dt>Student ID</dt><dd><c:out value="${profile.studentId}" /></dd></div><div><dt>Email</dt><dd><c:out value="${profile.email}" /></dd></div><div><dt>Role</dt><dd><span class="role-badge role-${profile.role}"><c:out value="${profile.role}" /></span></dd></div><div><dt>Email status</dt><dd><span class="verification-status ${profile.emailVerified ? 'verified' : 'unverified'}">${profile.emailVerified ? 'Verified' : 'Not verified'}</span></dd></div></dl></section>
        <section class="profile-card"><div class="profile-card-heading"><div><p class="eyebrow mb-1">Study scope</p><h2>Academic information</h2></div></div><dl class="profile-details"><div><dt>Semester</dt><dd><c:choose><c:when test="${empty profile.semester}">Not set</c:when><c:otherwise>Semester <c:out value="${profile.semester}" /></c:otherwise></c:choose></dd></div><div><dt>Section</dt><dd><c:choose><c:when test="${empty profile.sectionName}">Not set</c:when><c:otherwise><c:out value="${profile.sectionName}" /></c:otherwise></c:choose></dd></div></dl><p class="profile-card-note">These details determine which semester and section content you can access.</p></section></div>
    </c:otherwise></c:choose></c:if>
</main></div><script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script></body></html>
