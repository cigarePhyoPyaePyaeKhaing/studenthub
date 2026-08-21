<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Profile | StudentHub</title>
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
        <jsp:include page="partials/sidebar.jsp" />
    </div>
</div>
<div class="dashboard-shell profile-shell">
    <aside class="dashboard-sidebar d-none d-lg-flex">
        <jsp:include page="partials/sidebar.jsp" />
    </aside>
    <main class="profile-column">
        <c:if test="${not empty message}"><div class="alert alert-success"><c:out value="${message}" /></div></c:if>
        <c:if test="${not empty error}"><div class="alert alert-warning"><c:out value="${error}" /></div></c:if>
        <c:if test="${not empty profile}">
            <section class="profile-hero">
                <div class="profile-avatar"><c:out value="${profile.initial}" /></div>
                <div>
                    <p class="eyebrow mb-1">My StudentHub account</p>
                    <h1><c:out value="${profile.fullName}" /></h1>
                    <p>
                        <c:choose>
                            <c:when test="${not empty profile.studentId}"><c:out value="${profile.studentId}" /></c:when>
                            <c:otherwise>Not assigned</c:otherwise>
                        </c:choose>
                    </p>
                </div>
                <span class="profile-role role-${profile.role}"><c:out value="${profile.role}" /></span>
            </section>
            <c:choose>
                <c:when test="${editing}">
                    <section class="profile-card">
                        <div class="profile-card-heading">
                            <div>
                                <p class="eyebrow mb-1">Account and academic details</p>
                                <h2>Edit profile</h2>
                            </div>
                        </div>
                        <form method="post" action="${pageContext.request.contextPath}/profile" class="profile-form">
                            <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />">
                            <div>
                                <label for="fullName">Full name</label>
                                <input class="form-control" id="fullName" name="fullName" maxlength="100" value="<c:out value='${profile.fullName}' />" required>
                            </div>
                            <c:choose>
                                <c:when test="${profile.academicInfoLocked}">
                                    <div class="locked-field">
                                        <span>Semester</span>
                                        <strong>Semester <c:out value="${profile.semester}" /></strong>
                                    </div>
                                    <div class="locked-field">
                                        <span>Section</span>
                                        <strong><c:out value="${profile.sectionName}" /></strong>
                                    </div>
                                    <p class="profile-security-note">Academic information is locked. Submit an academic change request if it needs correction.</p>
                                </c:when>
                                <c:otherwise>
                                    <div>
                                        <label for="semester">Semester</label>
                                        <select class="form-select" id="semester" name="semester">
                                            <option value="">Not assigned</option>
                                            <c:forEach begin="1" end="10" var="number">
                                                <option value="${number}" <c:if test="${profile.semester eq number}">selected</c:if>>Semester ${number}</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                    <div>
                                        <label for="sectionName">Section</label>
                                        <input class="form-control" id="sectionName" name="sectionName" maxlength="20" pattern="[A-Za-z0-9][A-Za-z0-9 -]{0,19}" value="<c:out value='${profile.sectionName}' />" placeholder="For example: B">
                                    </div>
                                    <p class="profile-security-note">Semester and section control access to scoped announcements, deadlines, and discussions. Once saved, they will be locked.</p>
                                </c:otherwise>
                            </c:choose>
                            <div class="profile-form-actions">
                                <button class="btn btn-primary" type="submit">Save changes</button>
                                <a class="btn btn-light" href="${pageContext.request.contextPath}/profile">Cancel</a>
                            </div>
                        </form>
                    </section>
                </c:when>
                <c:otherwise>
                    <div class="profile-grid">
                        <section class="profile-card">
                            <div class="profile-card-heading">
                                <div>
                                    <p class="eyebrow mb-1">Identity</p>
                                    <h2>Account information</h2>
                                </div>
                                <a class="btn btn-primary btn-sm" href="${pageContext.request.contextPath}/profile?edit=true">Edit profile</a>
                            </div>
                            <dl class="profile-details">
                                <div>
                                    <dt>Full name</dt>
                                    <dd><c:out value="${profile.fullName}" /></dd>
                                </div>
                                <div>
                                    <dt>Student ID</dt>
                                    <dd>
                                        <c:choose>
                                            <c:when test="${not empty profile.studentId}"><c:out value="${profile.studentId}" /></c:when>
                                            <c:otherwise>Not assigned</c:otherwise>
                                        </c:choose>
                                    </dd>
                                </div>
                                <div>
                                    <dt>Email</dt>
                                    <dd><c:out value="${profile.email}" /></dd>
                                </div>
                                <div>
                                    <dt>University</dt>
                                    <dd>
                                        <c:choose>
                                            <c:when test="${not empty profile.universityName}">
                                                <c:out value="${profile.universityName}" /> <c:if test="${not empty profile.universityShortName}">(<c:out value="${profile.universityShortName}" />)</c:if>
                                            </c:when>
                                            <c:otherwise>Not assigned</c:otherwise>
                                        </c:choose>
                                    </dd>
                                </div>
                                <div>
                                    <dt>Role</dt>
                                    <dd><span class="role-badge role-${profile.role}"><c:out value="${profile.role}" /></span></dd>
                                </div>
                                <div>
                                    <dt>Email status</dt>
                                    <dd><span class="verification-status ${profile.emailVerified ? 'verified' : 'unverified'}">${profile.emailVerified ? 'Verified' : 'Not verified'}</span></dd>
                                </div>
                            </dl>
                        </section>
                        <section class="profile-card">
                            <div class="profile-card-heading">
                                <div>
                                    <p class="eyebrow mb-1">Study scope</p>
                                    <h2>Academic information</h2>
                                </div>
                            </div>
                            <dl class="profile-details">
                                <div>
                                    <dt>Semester</dt>
                                    <dd>
                                        <c:choose>
                                            <c:when test="${empty profile.semester}">Not assigned</c:when>
                                            <c:otherwise>Semester <c:out value="${profile.semester}" /></c:otherwise>
                                        </c:choose>
                                    </dd>
                                </div>
                                <div>
                                    <dt>Section</dt>
                                    <dd>
                                        <c:choose>
                                            <c:when test="${empty profile.sectionName}">Not assigned</c:when>
                                            <c:otherwise><c:out value="${profile.sectionName}" /></c:otherwise>
                                        </c:choose>
                                    </dd>
                                </div>
                            </dl>
                            <c:choose>
                                <c:when test="${profile.academicInfoLocked}">
                                    <p class="profile-card-note">Academic information is locked. Changes require administrator approval.</p>
                                    <c:choose>
                                        <c:when test="${not empty pendingAcademicRequest}">
                                            <div class="alert alert-info mt-3">
                                                <strong>Academic change request pending:</strong> Requested Semester <c:out value="${pendingAcademicRequest.requestedSemester}" />, Section <c:out value="${pendingAcademicRequest.requestedSection}" />.
                                                <div class="small text-secondary mt-1">Reason: <c:out value="${pendingAcademicRequest.reason}" /></div>
                                                <div class="small text-secondary mt-1">Submitted on <c:out value="${pendingAcademicRequest.createdLabel}" /></div>
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <details class="request-panel mt-3">
                                                <summary class="btn btn-outline-primary btn-sm">Request Academic Info Change</summary>
                                                <form method="post" action="${pageContext.request.contextPath}/profile/academic-change" class="profile-form mt-3">
                                                    <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />">
                                                    <div class="mb-3">
                                                        <label class="form-label" for="reqSemester">Requested semester</label>
                                                        <select class="form-select" id="reqSemester" name="semester" required>
                                                            <c:forEach begin="1" end="10" var="number">
                                                                <option value="${number}" <c:if test="${profile.semester eq number}">selected</c:if>>Semester ${number}</option>
                                                            </c:forEach>
                                                        </select>
                                                    </div>
                                                    <div class="mb-3">
                                                        <label class="form-label" for="reqSection">Requested section</label>
                                                        <input class="form-control" id="reqSection" name="sectionName" maxlength="20" pattern="[A-Za-z0-9][A-Za-z0-9 -]{0,19}" value="<c:out value='${profile.sectionName}' />" placeholder="For example: B" required>
                                                    </div>
                                                    <div class="mb-3">
                                                        <label class="form-label" for="reqReason">Reason</label>
                                                        <textarea class="form-control" id="reqReason" name="reason" minlength="10" maxlength="1000" rows="3" placeholder="Explain why you need to change your semester or section (10-1000 characters)" required></textarea>
                                                    </div>
                                                    <button class="btn btn-primary" type="submit">Submit request</button>
                                                </form>
                                            </details>
                                        </c:otherwise>
                                    </c:choose>
                                </c:when>
                                <c:otherwise>
                                    <p class="profile-card-note text-primary">Academic information is not set yet. Click "Edit profile" above to choose your Semester and Section.</p>
                                </c:otherwise>
                            </c:choose>
                        </section>
                    </div>
                </c:otherwise>
            </c:choose>
        </c:if>
    </main>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>