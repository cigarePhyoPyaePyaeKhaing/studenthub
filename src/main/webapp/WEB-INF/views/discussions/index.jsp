<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="en"><head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Discussions | StudentHub</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <script src="${pageContext.request.contextPath}/assets/js/main.js?v=${applicationScope.assetVersion}" defer></script><link href="${pageContext.request.contextPath}/assets/css/main.css?v=${applicationScope.assetVersion}" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/assets/css/dashboard.css?v=${applicationScope.assetVersion}" rel="stylesheet">
</head><body class="dashboard-body">
<header class="mobile-header d-lg-none">
    <a class="dashboard-brand" href="${pageContext.request.contextPath}/home"><span>S</span> StudentHub</a>
    <button class="btn btn-light" type="button" data-bs-toggle="offcanvas" data-bs-target="#mobileNavigation">Menu</button>
</header>
<div class="offcanvas offcanvas-start" tabindex="-1" id="mobileNavigation"><div class="offcanvas-header"><h2 class="offcanvas-title h5">StudentHub</h2><button type="button" class="btn-close" data-bs-dismiss="offcanvas"></button></div><div class="offcanvas-body"><jsp:include page="../partials/sidebar.jsp" /></div></div>
<div class="dashboard-shell discussions-shell">
    <aside class="dashboard-sidebar d-none d-lg-flex"><jsp:include page="../partials/sidebar.jsp" /></aside>
    <main class="discussion-column">
        <section class="discussion-header">
            <div><p class="eyebrow mb-2">Student community</p><h1>Discussions</h1><p>Connect with classmates in securely scoped rooms.</p></div>
            <c:if test="${not empty room and room.available}"><span class="discussion-scope-label"><c:out value="${room.scopeLabel}" /></span></c:if>
        </section>
        <nav class="message-mode-switch discussion-mode-switch" aria-label="Messaging sections">
            <a href="${pageContext.request.contextPath}/messages">Private Messages</a>
            <a class="active" href="${pageContext.request.contextPath}/discussions" aria-current="page">Academic Discussions</a>
        </nav>
        <nav class="room-tabs" aria-label="Discussion rooms">
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
                <c:otherwise><div class="message-list"><c:forEach var="chatMessage" items="${room.messages}"><c:set var="isOwn" value="${sessionScope.userId eq chatMessage.senderId}" /><div class="message-row ${isOwn ? 'outgoing' : 'incoming'}">
                    <a class="profile-identity-link message-avatar-link" href="${pageContext.request.contextPath}/profile?userId=${chatMessage.senderId}"><div class="avatar"><jsp:include page="../partials/avatar.jsp"><jsp:param name="photo" value="${chatMessage.authorAvatarUrl}"/><jsp:param name="initial" value="${chatMessage.authorName.substring(0,1)}"/></jsp:include></div></a>
                    <div class="message-bubble">
                        <header>
                            <a class="profile-name-link" href="${pageContext.request.contextPath}/profile?userId=${chatMessage.senderId}"><strong><c:out value="${chatMessage.authorName}" /></strong></a>
                            <span class="role-badge role-${chatMessage.authorRole}"><c:out value="${chatMessage.authorRole}" /></span>
                            <c:if test="${room.scope eq 'CR_SEMESTER' or room.scope eq 'CR_ALL'}">
                                <span class="message-scope">
                                    <c:if test="${not empty chatMessage.authorSemester}">Sem <c:out value="${chatMessage.authorSemester}" /></c:if>
                                    <c:if test="${not empty chatMessage.authorSection}"> · <c:out value="${chatMessage.authorSection}" /></c:if>
                                </span>
                            </c:if>
                            <time><c:out value="${chatMessage.createdLabel}" /></time>
                        </header>
                        <p><c:out value="${chatMessage.message}" /></p>
                        <c:set var="attachment" value="${chatMessage.attachment}" scope="request"/><jsp:include page="../partials/attachment.jsp"/>
                        <c:if test="${sessionScope.role eq 'ADMIN' or isOwn}">
                            <form method="post" action="${pageContext.request.contextPath}/discussions/messages/delete" onsubmit="return confirm('Delete this message?');">
                                <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />">
                                <input type="hidden" name="scope" value="<c:out value='${room.scope}' />">
                                <input type="hidden" name="id" value="${chatMessage.messageId}">
                                <button type="submit" title="Delete message">Delete</button>
                            </form>
                        </c:if>
                    </div>
                </div></c:forEach></div></c:otherwise>
            </c:choose>
            <c:if test="${not empty room and room.available}"><form class="chat-composer" method="post" enctype="multipart/form-data" action="${pageContext.request.contextPath}/discussions/messages"><input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />"><input type="hidden" name="scope" value="<c:out value='${room.scope}' />"><label class="attachment-button" title="Attach image, video, audio or file">📎<input type="file" name="attachment" accept="image/jpeg,image/png,image/webp,video/mp4,video/webm,audio/mpeg,audio/mp4,audio/aac,audio/wav,audio/ogg,audio/webm,.pdf,.doc,.docx,.ppt,.pptx,.xls,.xlsx,.txt,.zip"></label><label class="visually-hidden" for="message">Type a message</label><textarea id="message" name="message" rows="2" maxlength="2000" placeholder="Type a message..."></textarea><button class="btn btn-primary" type="submit">Send</button></form></c:if>
        </section>
    </main>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
(function() {
    function scrollToBottom() {
        var messageList = document.querySelector('.message-list');
        if (messageList) {
            messageList.scrollTop = messageList.scrollHeight;
        }
    }
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', scrollToBottom);
    } else {
        scrollToBottom();
    }
    window.addEventListener('load', scrollToBottom);
})();
</script>
</body></html>
