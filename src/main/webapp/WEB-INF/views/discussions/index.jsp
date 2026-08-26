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
</head><body class="dashboard-body">
<header class="mobile-header">
    <a class="dashboard-brand" href="${pageContext.request.contextPath}/home"><span>S</span> StudentHub</a>
    <button class="btn btn-light" type="button" data-bs-toggle="offcanvas" data-bs-target="#mobileNavigation">Menu</button>
</header>
<div class="offcanvas offcanvas-start" tabindex="-1" id="mobileNavigation"><div class="offcanvas-header"><h2 class="offcanvas-title h5">StudentHub</h2><button type="button" class="btn-close" data-bs-dismiss="offcanvas"></button></div><div class="offcanvas-body"><jsp:include page="../partials/sidebar.jsp" /></div></div>
<div class="dashboard-shell discussions-shell">
    <aside class="dashboard-sidebar"><jsp:include page="../partials/sidebar.jsp" /></aside>
    <main class="discussion-column">
        <section class="discussion-header">
            <div class="discussion-header-main">
                <div class="discussion-header-titles">
                    <p class="eyebrow mb-1">Student community</p>
                    <h1>Academic Discussions</h1>
                    <p class="discussion-header-desc mb-0">Connect with classmates in securely scoped rooms.</p>
                </div>
                <c:if test="${not empty room and room.available}">
                    <div class="discussion-scope-pill">
                        <span class="scope-dot" aria-hidden="true"></span>
                        <span class="discussion-scope-label"><c:out value="${room.scopeLabel}" /></span>
                    </div>
                </c:if>
            </div>
        </section>
        <nav class="message-mode-switch discussion-mode-switch" aria-label="Messaging sections">
            <a href="${pageContext.request.contextPath}/messages">Private Messages</a>
            <a class="active" href="${pageContext.request.contextPath}/discussions" aria-current="page">Academic Discussions</a>
        </nav>
        <nav class="room-tabs ${not empty room and room.crRoomsVisible ? 'room-tabs-five' : 'room-tabs-three'}" aria-label="Discussion rooms">
            <a class="${room.scope eq 'SECTION' ? 'active' : ''}" href="${pageContext.request.contextPath}/discussions?scope=SECTION">Section</a>
            <a class="${room.scope eq 'SEMESTER' ? 'active' : ''}" href="${pageContext.request.contextPath}/discussions?scope=SEMESTER">Semester</a>
            <a class="${room.scope eq 'ALL' ? 'active' : ''}" href="${pageContext.request.contextPath}/discussions?scope=ALL">All Students</a>
            <c:if test="${not empty room and room.crRoomsVisible}">
                <a class="${room.scope eq 'CR_SEMESTER' ? 'active' : ''}" href="${pageContext.request.contextPath}/discussions?scope=CR_SEMESTER">CR – Same Semester</a>
                <a class="${room.scope eq 'CR_ALL' ? 'active' : ''}" href="${pageContext.request.contextPath}/discussions?scope=CR_ALL">CR – All</a>
            </c:if>
        </nav>
        <c:if test="${not empty message}"><div class="alert alert-success"><c:out value="${message}" /></div></c:if>
        <c:if test="${not empty error}"><div class="alert alert-warning"><c:out value="${error}" /></div></c:if>
        <section class="chat-panel" aria-live="polite">
            <c:choose>
                <c:when test="${empty room}"><div class="chat-empty"><p>Discussions are temporarily unavailable.</p></div></c:when>
                <c:when test="${not room.available}"><div class="chat-empty"><div class="empty-icon">i</div><h2>Room unavailable</h2><p><c:out value="${room.denialReason}" /></p><a class="btn btn-primary" href="${pageContext.request.contextPath}/profile?edit=true">Complete profile</a></div></c:when>
                <c:when test="${empty room.messages}"><div class="chat-empty"><div class="empty-icon">C</div><h2>No messages yet.</h2><p>Start the conversation.</p></div></c:when>
                <c:otherwise><div class="message-list"><c:set var="lastDate" value="" /><c:forEach var="chatMessage" items="${room.messages}"><c:set var="isOwn" value="${sessionScope.userId eq chatMessage.senderId}" /><c:set var="currentDate" value="${chatMessage.dateGroupLabel}" /><c:if test="${currentDate ne lastDate}"><div class="chat-date-separator"><span><c:out value="${currentDate}" /></span></div><c:set var="lastDate" value="${currentDate}" /></c:if><div class="message-row ${isOwn ? 'outgoing' : 'incoming'}"><c:if test="${not isOwn}"><a class="profile-identity-link message-avatar-link" href="${pageContext.request.contextPath}/profile?userId=${chatMessage.senderId}"><div class="avatar"><jsp:include page="../partials/avatar.jsp"><jsp:param name="photo" value="${chatMessage.authorAvatarUrl}"/><jsp:param name="initial" value="${chatMessage.authorName.substring(0,1)}"/></jsp:include></div></a></c:if><div class="message-bubble ${isOwn ? 'outgoing' : 'incoming'}"><c:if test="${not isOwn}"><header class="message-header"><a class="profile-name-link" href="${pageContext.request.contextPath}/profile?userId=${chatMessage.senderId}"><strong><c:out value="${chatMessage.authorName}" /></strong></a><span class="role-badge role-${chatMessage.authorRole}"><c:out value="${chatMessage.authorRole}" /></span><c:if test="${room.scope eq 'CR_SEMESTER' or room.scope eq 'CR_ALL'}"><span class="message-scope"><c:if test="${not empty chatMessage.authorSemester}">Sem <c:out value="${chatMessage.authorSemester}" /></c:if><c:if test="${not empty chatMessage.authorSection}"> · <c:out value="${chatMessage.authorSection}" /></c:if></span></c:if></header></c:if><c:if test="${not empty chatMessage.message}"><p class="message-text"><c:out value="${chatMessage.message}" /></p></c:if><c:set var="attachment" value="${chatMessage.attachment}" scope="request"/><jsp:include page="../partials/attachment.jsp"/><footer class="message-meta"><time><c:out value="${chatMessage.createdLabel}" /></time><c:if test="${sessionScope.role eq 'ADMIN' or isOwn}"><form class="message-delete-form" method="post" action="${pageContext.request.contextPath}/discussions/messages/delete" onsubmit="return confirm('Delete this message?');"><input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />"><input type="hidden" name="scope" value="<c:out value='${room.scope}' />"><input type="hidden" name="id" value="${chatMessage.messageId}"><button type="submit" title="Delete message" aria-label="Delete message">Delete</button></form></c:if></footer></div></div></c:forEach></div></c:otherwise>
            </c:choose>
            <c:if test="${not empty room and room.available}"><form class="chat-composer message-composer" method="post" enctype="multipart/form-data" action="${pageContext.request.contextPath}/discussions/messages"><input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />"><input type="hidden" name="scope" value="<c:out value='${room.scope}' />"><div class="attachment-preview-area" hidden></div><div class="composer-controls-row"><label class="attachment-button" title="Attach image, video, audio or file" aria-label="Attach image, video, audio or file"><svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/></svg><input type="file" name="attachment" accept="image/jpeg,image/png,image/webp,video/mp4,video/webm,audio/mpeg,audio/mp4,audio/aac,audio/wav,audio/ogg,audio/webm,.pdf,.doc,.docx,.ppt,.pptx,.xls,.xlsx,.txt,.zip"></label><span class="composer-input"><label class="visually-hidden" for="message">Type a message</label><textarea id="message" name="message" rows="1" maxlength="2000" placeholder="Message..."></textarea></span><button class="btn btn-primary composer-send-btn" type="submit" aria-label="Send message" title="Send message"><svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor" aria-hidden="true"><path d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/></svg></button></div></form></c:if>
        </section>
    </main>
</div>
<jsp:include page="../partials/mobile-bottom-nav.jsp" />
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/message-composer.js?v=${applicationScope.assetVersion}" defer></script>
<script src="${pageContext.request.contextPath}/assets/js/discussion-chat.js?v=${applicationScope.assetVersion}" defer></script>
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
