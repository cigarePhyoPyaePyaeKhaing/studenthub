<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Academic Change Requests | StudentHub Administration</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="${pageContext.request.contextPath}/assets/js/main.js?v=${applicationScope.assetVersion}" defer></script>
    <link href="${pageContext.request.contextPath}/assets/css/main.css?v=${applicationScope.assetVersion}" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/dashboard.css?v=${applicationScope.assetVersion}" rel="stylesheet">
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
                <p class="eyebrow mb-1">Administration</p>
                <h1>Academic Change Requests</h1>
                <p class="mb-0 text-secondary">Review and approve student semester and academic group change requests.</p>
            </div>
        </section>

        <c:if test="${not empty message}"><div class="alert alert-success mb-3"><c:out value="${message}" /></div></c:if>
        <c:if test="${not empty error}"><div class="alert alert-danger mb-3"><c:out value="${error}" /></div></c:if>

        <div class="academic-filter-group" role="group" aria-label="Filter requests by status">
            <a class="academic-filter-btn ${currentStatus eq 'PENDING' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/academic-changes?status=PENDING">Pending</a>
            <a class="academic-filter-btn ${currentStatus eq 'APPROVED' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/academic-changes?status=APPROVED">Approved</a>
            <a class="academic-filter-btn ${currentStatus eq 'REJECTED' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/academic-changes?status=REJECTED">Rejected</a>
            <a class="academic-filter-btn ${currentStatus eq 'ALL' ? 'active' : ''}" href="${pageContext.request.contextPath}/admin/academic-changes?status=ALL">All</a>
        </div>

        <section class="admin-card">
            <c:choose>
                <c:when test="${not empty requests}">
                    <div class="academic-requests-list">
                        <c:forEach var="item" items="${requests}">
                            <article class="academic-request-card">
                                <header class="academic-request-header">
                                    <div>
                                        <h2 class="academic-student-name"><c:out value="${item.fullName}" /></h2>
                                        <div class="academic-student-meta">
                                            <c:if test="${not empty item.studentId}">
                                                <span class="academic-student-id-badge"><c:out value="${item.studentId}" /></span>
                                            </c:if>
                                            <span><c:out value="${item.email}" /></span>
                                        </div>
                                    </div>
                                    <span class="academic-status-badge status-${item.status.toLowerCase()}">
                                        <c:out value="${item.status}" />
                                    </span>
                                </header>

                                <div class="academic-info-grid">
                                    <div class="academic-info-box current-info">
                                        <span class="box-label">Current Academic Info</span>
                                        <div class="box-values">
                                            <c:choose>
                                                <c:when test="${empty item.oldSemester and empty item.oldSection}">
                                                    <span class="text-secondary">Not assigned</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <div>Semester: <span><c:out value="${empty item.oldSemester ? 'Not assigned' : item.oldSemester}" /></span></div>
                                                    <div>${item.oldSemester ge 7 ? 'Major' : 'Section'}: <span><c:out value="${empty item.oldSection ? 'Not assigned' : item.oldSection}" /></span></div>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                    <div class="academic-info-box requested-info">
                                        <span class="box-label">Requested Academic Info</span>
                                        <div class="box-values">
                                            <div>Semester: <span><c:out value="${item.requestedSemester}" /></span></div>
                                            <div>${item.requestedSemester ge 7 ? 'Major' : 'Section'}: <span><c:out value="${item.requestedSection}" /></span></div>
                                        </div>
                                    </div>
                                </div>

                                <div class="academic-reason-box">
                                    <span class="reason-label">Reason</span>
                                    <p><c:out value="${item.reason}" /></p>
                                </div>

                                <c:if test="${not empty item.adminNote}">
                                    <div class="academic-admin-note-box">
                                        <strong>Admin note:</strong> <c:out value="${item.adminNote}" />
                                        <c:if test="${not empty item.reviewedLabel}">
                                            <span class="text-muted ms-2">· Reviewed: <c:out value="${item.reviewedLabel}" /></span>
                                        </c:if>
                                    </div>
                                </c:if>

                                <div class="academic-footer-meta">
                                    <span>Submitted: <c:out value="${item.createdLabel}" /></span>
                                </div>

                                <c:if test="${item.status eq 'PENDING'}">
                                    <div class="academic-actions-bar">
                                        <form method="post" action="${pageContext.request.contextPath}/admin/academic-changes" class="academic-actions-form">
                                            <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />">
                                            <input type="hidden" name="id" value="${item.requestId}">
                                            <input class="form-control" name="adminNote" maxlength="1000" placeholder="Optional admin note">
                                            <button class="btn btn-academic-approve" type="submit" name="decision" value="approve">Approve</button>
                                            <button class="btn btn-academic-reject" type="submit" name="decision" value="reject">Reject</button>
                                        </form>
                                    </div>
                                </c:if>
                            </article>
                        </c:forEach>
                    </div>
                </c:when>
                <c:otherwise>
                    <p class="text-secondary mb-0">No academic change requests found.</p>
                </c:otherwise>
            </c:choose>
        </section>
    </main>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
