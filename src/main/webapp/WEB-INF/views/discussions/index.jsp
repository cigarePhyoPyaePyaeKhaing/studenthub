<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="en"><head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Discussions | StudentHub</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="${pageContext.request.contextPath}/assets/js/main.js?v=${applicationScope.assetVersion}" defer></script><link href="${pageContext.request.contextPath}/assets/css/main.css?v=${applicationScope.assetVersion}" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/dashboard.css?v=${applicationScope.assetVersion}" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/dashboard-refined.css?v=${applicationScope.assetVersion}" rel="stylesheet">
</head><body class="dashboard-body ${sessionScope.role eq 'ADMIN' ? 'discussion-admin-mode' : ''}">
<header class="mobile-header">
    <a class="dashboard-brand" href="${pageContext.request.contextPath}/home"><span>S</span> StudentHub</a>
    <button class="btn btn-light" type="button" data-bs-toggle="offcanvas" data-bs-target="#mobileNavigation">Menu</button>
</header>
<div class="offcanvas offcanvas-start" tabindex="-1" id="mobileNavigation"><div class="offcanvas-header"><h2 class="offcanvas-title h5">StudentHub</h2><button type="button" class="btn-close" data-bs-dismiss="offcanvas"></button></div><div class="offcanvas-body"><jsp:include page="../partials/sidebar.jsp" /></div></div>
<div class="dashboard-shell discussions-shell">
    <aside class="dashboard-sidebar"><jsp:include page="../partials/sidebar.jsp" /></aside>
    <main class="discussion-column">
        <section class="discussion-header ${sessionScope.role eq 'ADMIN' ? 'discussion-header-admin' : ''}">
            <div class="discussion-header-main">
                <div class="discussion-header-titles">
                    <p class="eyebrow mb-1">Student community</p>
                    <h1>Academic Discussions</h1>
                    <p class="discussion-header-desc mb-0"><c:choose><c:when test="${sessionScope.role eq 'ADMIN'}">Supervise academic conversations with existing moderation access.</c:when><c:otherwise>Connect with classmates in securely scoped rooms.</c:otherwise></c:choose></p>
                </div>
                <div class="discussion-header-context">
                    <c:if test="${sessionScope.role eq 'ADMIN'}">
                        <span class="admin-moderation-badge"><span class="admin-moderation-dot" aria-hidden="true"></span><strong>ADMIN</strong><span>Admin Moderation</span></span>
                    </c:if>
                    <c:if test="${not empty room and room.available}">
                        <div class="discussion-scope-pill">
                            <span class="scope-dot" aria-hidden="true"></span>
                            <span class="discussion-scope-label"><c:out value="${room.scopeLabel}" /></span>
                        </div>
                    </c:if>
                </div>
            </div>
        </section>
        <nav class="message-mode-switch discussion-mode-switch" aria-label="Messaging sections">
            <a href="${pageContext.request.contextPath}/messages">Private Messages</a>
            <a class="active" href="${pageContext.request.contextPath}/discussions" aria-current="page">Academic Discussions</a>
        </nav>
        <c:choose>
            <c:when test="${sessionScope.role eq 'ADMIN'}">
                <section class="admin-discussion-controls" aria-label="Admin discussion navigation">
                    <c:set var="isGlobalAllStudents" value="${selectedModerationScope eq 'all_students'}" />
                    <c:set var="isGlobalAllCr" value="${selectedModerationScope eq 'all_cr'}" />
                    <c:set var="isAcademicSemesterActive" value="${not empty selectedModerationSemester and not isGlobalAllStudents and not isGlobalAllCr}" />
                    <c:set var="isAcademicSectionActive" value="${not empty selectedModerationSection and not isGlobalAllStudents and not isGlobalAllCr}" />
                    <nav class="admin-global-scopes" aria-label="Global discussion scopes">
                        <a class="admin-scope-card ${isGlobalAllStudents ? 'active' : ''}" href="${pageContext.request.contextPath}/discussions?moderationScope=all_students" ${isGlobalAllStudents ? 'aria-current="page"' : ''}>
                            <span class="admin-scope-card-icon" aria-hidden="true">A</span><span><strong>All Students</strong><small>University-wide student discussion</small></span>
                        </a>
                        <a class="admin-scope-card ${isGlobalAllCr ? 'active' : ''}" href="${pageContext.request.contextPath}/discussions?moderationScope=all_cr" ${isGlobalAllCr ? 'aria-current="page"' : ''}>
                            <span class="admin-scope-card-icon" aria-hidden="true">CR</span><span><strong>All CRs</strong><small>Authorized class representative discussion</small></span>
                        </a>
                    </nav>
                    <form class="admin-scope-form admin-academic-selectors" action="${pageContext.request.contextPath}/discussions" data-academic-group-picker data-navigation-base="${pageContext.request.contextPath}/discussions">
                        <div class="admin-selector-item ${isAcademicSemesterActive ? 'active' : ''}"><label for="moderation-semester">Semester</label><select class="form-select" id="moderation-semester" data-group-semester><option value="">Select Semester</option><c:forEach var="option" items="${moderationSemesters}"><option value="${option.semester}" ${selectedModerationSemester eq option.semester ? 'selected' : ''}>Semester ${option.semester}</option></c:forEach></select></div>
                        <div class="admin-selector-item ${isAcademicSectionActive ? 'active' : ''}"><label for="section-name" data-group-label>Section</label><select class="form-select" id="section-name" data-group-name aria-describedby="academic-group-help"><option value="">Choose a semester first</option><c:forEach var="option" items="${moderationSections}"><option value="<c:out value='${option.sectionName}'/>" data-semester="${option.semester}" ${selectedModerationScope eq option.key ? 'selected' : ''}><c:out value="${option.sectionName}" /></option></c:forEach></select><span class="visually-hidden" id="academic-group-help">The available section or major options depend on the selected semester.</span></div>
                    </form>
                </section>
            </c:when>
            <c:when test="${sessionScope.role eq 'CR'}">
                <nav class="room-tabs ${not empty room and room.crSemesterRoomAvailable ? 'room-tabs-five' : 'room-tabs-four'}" aria-label="Discussion rooms">
                    <c:if test="${not empty room and room.sectionRoomAvailable}"><a class="${room.scope eq 'SECTION' ? 'active' : ''}" href="${pageContext.request.contextPath}/discussions?scope=SECTION">${room.semester ge 7 ? 'Major' : 'Section'}</a></c:if>
                    <c:if test="${not empty room and room.semesterRoomAvailable}"><a class="${room.scope eq 'SEMESTER' ? 'active' : ''}" href="${pageContext.request.contextPath}/discussions?scope=SEMESTER">Semester</a></c:if>
                    <a class="${room.scope eq 'ALL' ? 'active' : ''}" href="${pageContext.request.contextPath}/discussions?scope=ALL">All Students</a>
                    <c:if test="${room.crSemesterRoomAvailable}"><a class="${room.scope eq 'CR_SEMESTER' ? 'active' : ''}" href="${pageContext.request.contextPath}/discussions?scope=CR_SEMESTER">CR – Same Semester</a></c:if>
                    <a class="${room.scope eq 'CR_ALL' ? 'active' : ''}" href="${pageContext.request.contextPath}/discussions?scope=CR_ALL">CR – All</a>
                </nav>
            </c:when>
            <c:otherwise>
                <nav class="room-tabs ${not empty room and (room.sectionRoomAvailable and room.semesterRoomAvailable) ? 'room-tabs-four' : ((room.sectionRoomAvailable or room.semesterRoomAvailable) ? 'room-tabs-three' : 'room-tabs-two')}" aria-label="Discussion rooms">
                    <c:if test="${not empty room and room.sectionRoomAvailable}"><a class="${room.scope eq 'SECTION' ? 'active' : ''}" href="${pageContext.request.contextPath}/discussions?scope=SECTION">${room.semester ge 7 ? 'Major' : 'Section'}</a></c:if>
                    <c:if test="${not empty room and room.semesterRoomAvailable}"><a class="${room.scope eq 'SEMESTER' ? 'active' : ''}" href="${pageContext.request.contextPath}/discussions?scope=SEMESTER">Semester</a></c:if>
                    <a class="${room.scope eq 'ALL' ? 'active' : ''}" href="${pageContext.request.contextPath}/discussions?scope=ALL">All Students</a>
                </nav>
            </c:otherwise>
        </c:choose>
        <c:if test="${not empty message}"><div class="alert alert-success"><c:out value="${message}" /></div></c:if>
        <c:if test="${not empty error}"><div class="alert alert-warning"><c:out value="${error}" /></div></c:if>
        <section class="chat-panel" aria-live="polite">
            <c:choose>
                <c:when test="${empty room}"><div class="chat-empty"><p>Discussions are temporarily unavailable.</p></div></c:when>
                <c:when test="${not room.available}"><div class="chat-empty"><div class="empty-icon">i</div><h2>Room unavailable</h2><p><c:out value="${room.denialReason}" /></p><a class="btn btn-primary" href="${pageContext.request.contextPath}/profile?edit=true">Complete profile</a></div></c:when>
                <c:when test="${empty room.messages}"><div class="chat-empty"><div class="empty-icon">C</div><h2>No messages yet.</h2><p>Start the conversation.</p></div></c:when>
                <c:otherwise><div class="message-list"><c:set var="lastDate" value="" /><c:forEach var="chatMessage" items="${room.messages}"><c:set var="isOwn" value="${sessionScope.userId eq chatMessage.senderId}" /><c:set var="currentDate" value="${chatMessage.dateGroupLabel}" /><c:if test="${currentDate ne lastDate}"><div class="chat-date-separator"><span><c:out value="${currentDate}" /></span></div><c:set var="lastDate" value="${currentDate}" /></c:if><div class="message-row ${isOwn ? 'outgoing' : 'incoming'}"><c:if test="${not isOwn}"><a class="profile-identity-link message-avatar-link" href="${pageContext.request.contextPath}/profile?userId=${chatMessage.senderId}"><div class="avatar"><jsp:include page="../partials/avatar.jsp"><jsp:param name="photo" value="${chatMessage.authorAvatarUrl}"/><jsp:param name="initial" value="${chatMessage.authorName.substring(0,1)}"/></jsp:include></div></a></c:if><div class="message-bubble ${isOwn ? 'outgoing' : 'incoming'}"><header class="message-header"><c:choose><c:when test="${isOwn}"><strong class="current-user-message-label">You</strong><span class="role-badge role-${sessionScope.role}"><c:out value="${sessionScope.role}" /></span></c:when><c:otherwise><a class="profile-name-link" href="${pageContext.request.contextPath}/profile?userId=${chatMessage.senderId}"><strong><c:out value="${chatMessage.authorName}" /></strong></a><span class="role-badge role-${chatMessage.authorRole}"><c:out value="${chatMessage.authorRole}" /></span><c:if test="${room.scope eq 'CR_SEMESTER' or room.scope eq 'CR_ALL' or room.scope eq 'CR_ADMIN' or room.scope eq 'ALL_STUDENTS_ADMIN'}"><span class="message-scope"><c:if test="${not empty chatMessage.authorSemester}">Sem <c:out value="${chatMessage.authorSemester}" /></c:if><c:if test="${not empty chatMessage.authorSection}"> · <c:out value="${chatMessage.authorSection}" /></c:if></span></c:if></c:otherwise></c:choose></header><c:if test="${not empty chatMessage.message}"><p class="message-text"><c:out value="${chatMessage.message}" /></p></c:if><c:set var="attachment" value="${chatMessage.attachment}" scope="request"/><jsp:include page="../partials/attachment.jsp"/><footer class="message-meta"><time><c:out value="${chatMessage.createdLabel}" /></time><c:if test="${sessionScope.role eq 'ADMIN' or isOwn}"><details class="message-actions-menu ${sessionScope.role eq 'ADMIN' ? 'admin-message-actions' : 'own-message-actions'}"><summary title="Message actions" aria-label="${sessionScope.role eq 'ADMIN' ? 'Open moderation actions' : 'Open message actions'}"><svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="5" cy="12" r="1.7"/><circle cx="12" cy="12" r="1.7"/><circle cx="19" cy="12" r="1.7"/></svg></summary><div class="message-actions-popover"><form class="message-delete-form ${sessionScope.role eq 'ADMIN' ? 'admin-moderation-delete' : 'own-message-delete'}" method="post" action="${pageContext.request.contextPath}/discussions/messages/delete" onsubmit="return confirm('Delete this message?');"><input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />"><input type="hidden" name="scope" value="<c:out value='${room.scope}' />"><input type="hidden" name="id" value="${chatMessage.messageId}"><button type="submit"><c:choose><c:when test="${sessionScope.role eq 'ADMIN'}">Remove as moderator</c:when><c:otherwise>Delete message</c:otherwise></c:choose></button></form></div></details></c:if></footer></div></div></c:forEach></div></c:otherwise>
            </c:choose>
            <c:if test="${not empty room and room.available}"><form class="chat-composer message-composer" method="post" enctype="multipart/form-data" action="${pageContext.request.contextPath}/discussions/messages"><input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />"><input type="hidden" name="scope" value="<c:out value='${room.scope}' />"><c:if test="${sessionScope.role eq 'ADMIN'}"><input type="hidden" name="moderationScope" value="<c:out value='${selectedModerationScope}' />"></c:if><div class="attachment-preview-area" hidden></div><div class="composer-controls-row"><label class="attachment-button" title="Attach image, video, audio or file" aria-label="Attach image, video, audio or file"><svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/></svg><input type="file" name="attachment" accept="image/jpeg,image/png,image/webp,video/mp4,video/webm,audio/mpeg,audio/mp4,audio/aac,audio/wav,audio/ogg,audio/webm,.pdf,.doc,.docx,.ppt,.pptx,.xls,.xlsx,.txt,.zip"></label><span class="composer-input"><label class="visually-hidden" for="message">Type a message</label><textarea id="message" name="message" rows="1" maxlength="2000" placeholder="Message..."></textarea></span><button class="btn btn-primary composer-send-btn" type="submit" aria-label="Send message" title="Send message"><svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor" aria-hidden="true"><path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/></svg></button></div></form></c:if>
        </section>
    </main>
</div>
<jsp:include page="../partials/mobile-bottom-nav.jsp" />
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/message-composer.js?v=${applicationScope.assetVersion}" defer></script>
<script src="${pageContext.request.contextPath}/assets/js/discussion-chat.js?v=${applicationScope.assetVersion}" defer></script>
<script src="${pageContext.request.contextPath}/assets/js/academic-group-selector.js?v=${applicationScope.assetVersion}" defer></script>
<script>
(function() {
    function scrollToBottom() {
        var messageList = document.querySelector('.message-list');
        if (messageList) {
            messageList.scrollTop = messageList.scrollHeight;
        }
    }
    function scrollActiveRoomTab() {
        var activeTab = document.querySelector('.room-tabs a.active');
        if (activeTab && typeof activeTab.scrollIntoView === 'function') {
            activeTab.scrollIntoView({ behavior: 'auto', block: 'nearest', inline: 'center' });
        }
    }
    function initScrolls() {
        scrollToBottom();
        scrollActiveRoomTab();
    }
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initScrolls);
    } else {
        initScrolls();
    }
    window.addEventListener('load', initScrolls);
})();
</script>
</body></html>
