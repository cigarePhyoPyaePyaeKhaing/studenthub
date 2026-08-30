<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Profile | StudentHub</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="${pageContext.request.contextPath}/assets/js/main.js?v=${applicationScope.assetVersion}" defer></script>
    <script src="${pageContext.request.contextPath}/assets/js/profile-photo.js?v=${applicationScope.assetVersion}" defer></script>
    <script src="${pageContext.request.contextPath}/assets/js/account-deletion.js?v=${applicationScope.assetVersion}" defer></script>
    <link href="${pageContext.request.contextPath}/assets/css/main.css?v=${applicationScope.assetVersion}" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/dashboard.css?v=${applicationScope.assetVersion}" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/dashboard-refined.css?v=${applicationScope.assetVersion}" rel="stylesheet">
</head>
<body class="dashboard-body">
<header class="mobile-header">
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
    <aside class="dashboard-sidebar">
        <jsp:include page="partials/sidebar.jsp" />
    </aside>
    <main class="profile-column">
        <c:if test="${not empty message}"><div class="alert alert-success"><c:out value="${message}" /></div></c:if>
        <c:if test="${not empty error}"><div class="alert alert-warning"><c:out value="${error}" /></div></c:if>
        <c:if test="${not empty profile}">
            <c:if test="${not empty profile.avatarUrl}"><c:url var="profilePhotoUrl" value="/profile/photo/${profile.avatarUrl}"><c:param name="v" value="${profile.avatarUrl}"/></c:url></c:if>
            <section class="profile-hero">
                <div class="profile-avatar"><span class="avatar-fallback"><c:out value="${profile.initial}"/></span><c:if test="${not empty profile.avatarUrl}"><img src="${profilePhotoUrl}" alt="" onerror="this.hidden=true;this.previousElementSibling.hidden=false" onload="this.previousElementSibling.hidden=true"></c:if></div>
                <div class="profile-identity">
                    <p class="eyebrow mb-1">${publicProfile ? 'StudentHub profile' : 'My StudentHub account'}</p>
                    <h1><c:out value="${profile.fullName}" /></h1>
                    <p>
                        <c:choose>
                            <c:when test="${not empty profile.studentId}"><c:out value="${profile.studentId}" /></c:when>
                            <c:otherwise>Not assigned</c:otherwise>
                        </c:choose>
                    </p><p class="presence-status ${activeNow ? 'is-active' : ''}"><span aria-hidden="true"></span><c:out value="${presenceLabel}" /></p><c:if test="${publicProfile}"><form method="post" action="${pageContext.request.contextPath}/messages/start" class="profile-message-action"><input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}'/>"><input type="hidden" name="targetUserId" value="${profile.userId}"><button class="btn btn-primary" type="submit" aria-label="Start private conversation"><svg aria-hidden="true" viewBox="0 0 24 24"><path d="M21 11.5a8.4 8.4 0 0 1-9 8.4 9.6 9.6 0 0 1-3.8-.8L3 21l1.8-4.8A8.4 8.4 0 1 1 21 11.5Z"/></svg><span>Message</span></button></form></c:if>
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
                        <form method="post" action="${pageContext.request.contextPath}/profile" class="profile-form" enctype="multipart/form-data" data-profile-photo-form>
                            <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />">
                            <fieldset class="profile-photo-editor">
                                <legend>Profile Photo</legend>
                                <div class="profile-photo-row">
                                    <div class="profile-photo-preview" data-photo-preview data-fallback="<c:out value='${profile.initial}' />"><span class="avatar-fallback"><c:out value="${profile.initial}"/></span><c:if test="${not empty profile.avatarUrl}"><img src="${profilePhotoUrl}" alt="" data-current-photo onerror="this.hidden=true;this.previousElementSibling.hidden=false" onload="this.previousElementSibling.hidden=true"></c:if></div>
                                    <div class="profile-photo-controls"><label class="profile-photo-button" for="profilePhoto">Choose Image</label><input class="visually-hidden" type="file" id="profilePhoto" name="profilePhoto" accept="image/jpeg,image/png,image/webp" data-photo-input><p>JPG, PNG, or WEBP · Max 2 MB</p><p class="profile-photo-error" data-photo-error role="alert"></p><c:if test="${not empty profile.avatarUrl}"><label class="remove-photo-control"><input type="checkbox" name="removePhoto" value="true" data-remove-photo> Remove photo</label></c:if></div>
                                </div>
                            </fieldset>
                            <div>
                                <label for="fullName">Full name</label>
                                <input class="form-control" id="fullName" name="fullName" maxlength="100" value="<c:out value='${profile.fullName}' />" required>
                            </div>
                            <c:choose>
                                <c:when test="${profile.universityLocked}">
                                    <div class="locked-field">
                                        <span>University</span>
                                        <strong>
                                            <c:out value="${profile.universityName}" />
                                            <c:if test="${not empty profile.universityShortName}"> (<c:out value="${profile.universityShortName}" />)</c:if>
                                        </strong>
                                    </div>
                                    <p class="profile-security-note">University is locked and cannot be changed.</p>
                                </c:when>
                                <c:otherwise>
                                    <div>
                                        <label for="universityId">University</label>
                                        <select class="form-select" id="universityId" name="universityId">
                                            <option value="">Select your university</option>
                                            <c:forEach var="u" items="${availableUniversities}">
                                                <option value="${u.universityId}" <c:if test="${profile.universityId eq u.universityId}">selected</c:if>>
                                                    <c:out value="${u.displayName}" />
                                                </option>
                                            </c:forEach>
                                        </select>
                                        <p class="profile-security-note">You can choose your University once. Once saved, it will be locked.</p>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                            <c:if test="${profile.role ne 'ADMIN'}"><c:choose>
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
                            </c:choose></c:if>
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
                                <c:if test="${not publicProfile}"><a class="btn btn-primary btn-sm" href="${pageContext.request.contextPath}/profile?edit=true">Edit profile</a></c:if>
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
                                <c:if test="${not publicProfile}"><div><dt>Email</dt><dd><c:out value="${profile.email}" /></dd></div></c:if>
                                <div>
                                    <dt>University</dt>
                                    <dd>
                                        <c:choose>
                                            <c:when test="${not empty profile.universityName}">
                                                <c:out value="${profile.universityName}" /><c:if test="${not empty profile.universityShortName}"> (<c:out value="${profile.universityShortName}" />)</c:if>
                                            </c:when>
                                            <c:otherwise>
                                                <div class="d-flex align-items-center justify-content-between flex-wrap gap-2">
                                                    <span class="text-secondary">Not assigned</span>
                                                    <c:if test="${not publicProfile}"><a class="btn btn-outline-primary btn-sm" href="${pageContext.request.contextPath}/profile?edit=true#universityId">Select University</a></c:if>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                    </dd>
                                </div>
                                <div>
                                    <dt>Role</dt>
                                    <dd><span class="role-badge role-${profile.role}"><c:out value="${profile.role}" /></span></dd>
                                </div>
                                <c:if test="${not publicProfile}"><div><dt>Email status</dt><dd><span class="verification-status ${profile.emailVerified ? 'verified' : 'unverified'}">${profile.emailVerified ? 'Verified' : 'Not verified'}</span></dd></div></c:if>
                            </dl>
                        </section>
                        <c:if test="${profile.role ne 'ADMIN'}"><section class="profile-card">
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
                            <c:if test="${not publicProfile}"><c:choose>
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
                                        <c:when test="${academicRequestUnavailable}">
                                             <div class="alert alert-warning mt-3">
                                                 Academic change request service is temporarily unavailable. Please try again later.
                                             </div>
                                        </c:when>
                                        <c:otherwise>
                                            <details class="request-panel mt-3">
                                                <summary class="btn btn-primary btn-sm">Request Academic Info Change</summary>
                                                <form method="post" action="${pageContext.request.contextPath}/profile/academic-change" class="profile-form mt-3">
                                                    <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />">
                                                    <div class="student-current-academic-card">
                                                        <span class="card-heading-label">Current academic information</span>
                                                        <div class="student-current-academic-grid">
                                                            <div class="student-current-academic-item">
                                                                <span class="item-label">Current semester</span>
                                                                <span class="item-value">Semester <c:out value="${profile.semester}" /></span>
                                                            </div>
                                                            <div class="student-current-academic-item">
                                                                <span class="item-label">Current section</span>
                                                                <span class="item-value"><c:out value="${profile.sectionName}" /></span>
                                                            </div>
                                                        </div>
                                                    </div>
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
                            </c:choose></c:if>
                        </section></c:if>
                    </div>
                </c:otherwise>
            </c:choose>
        </c:if>
        <c:if test="${not publicProfile and not editing and not empty profile}">
            <section class="profile-card profile-danger-zone"><div><p class="eyebrow mb-1">Danger Zone</p><h2>Delete account</h2><p>Permanently delete your StudentHub account and associated personal data. This action cannot be undone.</p></div><button class="btn btn-danger" type="button" data-open-delete-account>Delete Account</button></section>
            <div class="account-delete-modal" data-delete-account-modal hidden role="dialog" aria-modal="true" aria-labelledby="delete-account-title"><div class="account-delete-dialog"><h2 id="delete-account-title">Delete your account?</h2><p>This permanently deletes your StudentHub account and associated personal data. This action cannot be undone.</p><form method="post" action="${pageContext.request.contextPath}/profile/delete-account" data-delete-account-form><input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />"><label for="deleteCurrentPassword">Current password</label><input class="form-control" id="deleteCurrentPassword" name="currentPassword" type="password" autocomplete="current-password" required><p class="account-delete-error" data-delete-account-error role="alert"></p><div class="account-delete-actions"><button class="btn btn-light" type="button" data-close-delete-account>Cancel</button><button class="btn btn-danger" type="submit">Permanently Delete Account</button></div></form></div></div>
        </c:if>
    </main>
</div>
<jsp:include page="partials/mobile-bottom-nav.jsp" />
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
