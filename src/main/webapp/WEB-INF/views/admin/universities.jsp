<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Universities | StudentHub Admin</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="${pageContext.request.contextPath}/assets/js/main.js"></script>
    <link href="${pageContext.request.contextPath}/assets/css/main.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/dashboard.css" rel="stylesheet">
</head>
<body class="dashboard-body">
<header class="mobile-header d-lg-none">
    <a class="dashboard-brand" href="${pageContext.request.contextPath}/home"><span>S</span> StudentHub</a>
    <button class="btn btn-light" type="button" data-bs-toggle="offcanvas" data-bs-target="#mobileNavigation">Menu</button>
</header>
<div class="offcanvas offcanvas-start" tabindex="-1" id="mobileNavigation">
    <div class="offcanvas-header">
        <h2 class="offcanvas-title h5">StudentHub</h2>
        <button type="button" class="btn-close" data-bs-dismiss="offcanvas"></button>
    </div>
    <div class="offcanvas-body">
        <jsp:include page="../partials/sidebar.jsp" />
    </div>
</div>

<div class="dashboard-shell admin-shell">
    <aside class="dashboard-sidebar d-none d-lg-flex">
        <jsp:include page="../partials/sidebar.jsp" />
    </aside>
    <main class="admin-column">
        <section class="welcome-panel">
            <div>
                <p class="eyebrow mb-2">Administration</p>
                <h1>University Management</h1>
                <p class="mb-0 text-secondary">Manage universities eligible for student registration and community grouping.</p>
            </div>
            <button class="btn btn-primary" type="button" data-bs-toggle="collapse" data-bs-target="#addUniversityCard">Add University</button>
        </section>

        <c:if test="${not empty message}">
            <div class="alert alert-success"><c:out value="${message}" /></div>
        </c:if>
        <c:if test="${not empty error}">
            <div class="alert alert-warning"><c:out value="${error}" /></div>
        </c:if>

        <div class="collapse mb-4" id="addUniversityCard">
            <section class="admin-card">
                <div class="admin-card-heading">
                    <div>
                        <p class="eyebrow mb-1">New institution</p>
                        <h2>Add University</h2>
                    </div>
                </div>
                <form method="post" action="${pageContext.request.contextPath}/admin/universities" class="profile-form">
                    <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />">
                    <input type="hidden" name="action" value="create">
                    <div>
                        <label for="uniName">University Full Name</label>
                        <input class="form-control" id="uniName" name="name" maxlength="180" placeholder="e.g. University of Information Technology" required>
                    </div>
                    <div>
                        <label for="uniShort">Abbreviation / Short Name</label>
                        <input class="form-control" id="uniShort" name="shortName" maxlength="30" placeholder="e.g. UIT">
                    </div>
                    <div class="profile-form-actions">
                        <button class="btn btn-primary" type="submit">Save & Activate</button>
                        <button class="btn btn-light" type="button" data-bs-toggle="collapse" data-bs-target="#addUniversityCard">Cancel</button>
                    </div>
                </form>
            </section>
        </div>

        <section class="admin-card">
            <div class="admin-card-heading">
                <div>
                    <p class="eyebrow mb-1">Registered Institutions</p>
                    <h2>Universities</h2>
                </div>
            </div>
            <div class="admin-table-wrap">
                <table class="admin-table">
                    <thead>
                        <tr>
                            <th>University Name</th>
                            <th>Abbreviation</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="uni" items="${universities}">
                            <tr>
                                <td><strong><c:out value="${uni.name}" /></strong></td>
                                <td><c:out value="${empty uni.shortName ? '—' : uni.shortName}" /></td>
                                <td>
                                    <span class="role-badge ${uni.status eq 'APPROVED' ? 'role-STUDENT' : 'role-CR'}">
                                        <c:out value="${uni.status}" />
                                    </span>
                                </td>
                                <td>
                                    <div class="d-flex gap-2">
                                        <c:choose>
                                            <c:when test="${uni.status eq 'APPROVED'}">
                                                <form method="post" action="${pageContext.request.contextPath}/admin/universities" onsubmit="return confirm('Deactivate this university? Inactive universities cannot be selected during new registration.');">
                                                    <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />">
                                                    <input type="hidden" name="action" value="deactivate">
                                                    <input type="hidden" name="id" value="${uni.universityId}">
                                                    <button type="submit" class="btn btn-light btn-sm">Deactivate</button>
                                                </form>
                                            </c:when>
                                            <c:otherwise>
                                                <form method="post" action="${pageContext.request.contextPath}/admin/universities">
                                                    <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />">
                                                    <input type="hidden" name="action" value="activate">
                                                    <input type="hidden" name="id" value="${uni.universityId}">
                                                    <button type="submit" class="btn btn-primary btn-sm">Activate</button>
                                                </form>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty universities}">
                            <tr>
                                <td colspan="4">No universities configured.</td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </section>
    </main>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
