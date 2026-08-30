<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<aside class="auth-brand-panel" aria-label="About StudentHub">
    <div class="auth-brand-panel-copy">
        <jsp:include page="../partials/logo.jsp" />
        <p class="auth-brand-kicker">Your academic community</p>
        <c:choose><c:when test="${param.mode eq 'login'}"><h2>Welcome back.</h2><p>Your academic community, announcements, deadlines, and discussions are all waiting in one focused place.</p></c:when><c:otherwise><h2>Join StudentHub.</h2><p>Create your account and connect with the academic community that matters to you.</p></c:otherwise></c:choose>
    </div>
    <div class="auth-brand-orbit" aria-hidden="true"><jsp:include page="../partials/logo.jsp"><jsp:param name="compact" value="true"/></jsp:include></div>
</aside>
