<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Academic Change Requests | StudentHub Administration</title>
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
                <p class="eyebrow mb-1">Administration</p>
                <h1>Academic Change Requests</h1>
                <p class="mb-0 text-secondary">Review and approve student semester and section change requests.</p>
            </div>
        </section>

        <c:if test="${not empty message}"><div class="alert alert-success"><c:out value="${message}" /></div></c:if>
        <c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}" /></div></c:if>

        <div class="d-flex gap-2 mb-3">
            <a class="btn btn-sm ${currentStatus eq 'PENDING' ? 'btn-primary' : 'btn-outline-primary'}" href="${pageContext.request.contextPath}/admin/academic-changes?status=PENDING">Pending</a>
            <a class="btn btn-sm ${currentStatus eq 'APPROVED' ? 'btn-primary' : 'btn-outline-primary'}" href="${pageContext.request.contextPath}/admin/academic-changes?status=APPROVED">Approved</a>
            <a class="btn btn-sm ${currentStatus eq 'REJECTED' ? 'btn-primary' : 'btn-outline-primary'}" href="${pageContext.request.contextPath}/admin/academic-changes?status=REJECTED">Rejected</a>
            <a class="btn btn-sm ${currentStatus eq 'ALL' ? 'btn-primary' : 'btn-outline-primary'}" href="${pageContext.request.contextPath}/admin/academic-changes?status=ALL">All</a>
        </div>

        <section class="admin-card">
            <c:choose>
                <c:when test="${not empty requests}">
                    <div class="d-flex flex-column gap-3">
                        <c:forEach var="item" items="${requests}">
                            <article class="p-3 border rounded bg-light">
                                <div class="d-flex justify-content-between align-items-start flex-wrap gap-2 mb-2">
                                    <div>
                                        <h2 class="h5 mb-1"><c:out value="${item.fullName}" /> <span class="text-secondary small">(<c:out value="${item.studentId}" />)</span></h2>
                                        <div class="small text-secondary"><c:out value="${item.email}" /> · Submitted: <c:out value="${item.createdLabel}" /></div>
                                    </div>
                                    <span class="badge ${item.status eq 'PENDING' ? 'bg-warning text-dark' : (item.status eq 'APPROVED' ? 'bg-success' : 'bg-danger')}">
                                        <c:out value="${item.status}" />
                                    </span>
                                </div>
                                <div class="row g-2 mb-2">
                                    <div class="col-sm-6">
                                        <div class="p-2 bg-white rounded border small">
                                            <strong>Current:</strong>
                                            <c:choose>
                                                <c:when test="${empty item.oldSemester and empty item.oldSection}">Not assigned</c:when>
                                                <c:otherwise>Semester <c:out value="${item.oldSemester}" /> / Section <c:out value="${item.oldSection}" /></c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                    <div class="col-sm-6">
                                        <div class="p-2 bg-white rounded border small text-primary">
                                            <strong>Requested:</strong> Semester <c:out value="${item.requestedSemester}" /> / Section <c:out value="${item.requestedSection}" />
                                        </div>
                                    </div>
                                </div>
                                <div class="p-2 bg-white rounded border small mb-2">
                                    <strong>Reason:</strong> <c:out value="${item.reason}" />
                                </div>
                                <c:if test="${not empty item.adminNote}">
                                    <div class="p-2 bg-white rounded border small mb-2 text-muted">
                                        <strong>Admin note:</strong> <c:out value="${item.adminNote}" />
                                        <c:if test="${not empty item.reviewedLabel}"><span class="ms-2">· Reviewed on <c:out value="${item.reviewedLabel}" /></span></c:if>
                                    </div>
                                </c:if>
                                <c:if test="${item.status eq 'PENDING'}">
                                    <form method="post" action="${pageContext.request.contextPath}/admin/academic-changes" class="mt-2 d-flex flex-wrap gap-2 align-items-center">
                                        <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />">
                                        <input type="hidden" name="id" value="${item.requestId}">
                                        <input class="form-control form-control-sm" style="max-width:300px" name="adminNote" maxlength="1000" placeholder="Optional admin note">
                                        <button class="btn btn-success btn-sm" type="submit" name="decision" value="approve">Approve</button>
                                        <button class="btn btn-outline-danger btn-sm" type="submit" name="decision" value="reject">Reject</button>
                                    </form>
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