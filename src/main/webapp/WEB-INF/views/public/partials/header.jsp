<%@ page contentType="text/html;charset=UTF-8" %>
<header class="public-header">
  <div class="public-container public-nav-shell">
    <a class="public-brand" href="${pageContext.request.contextPath}/" aria-label="StudentHub home"><jsp:include page="../../partials/logo.jsp" /></a>
    <div class="public-actions">
      <button class="icon-button" type="button" data-theme-toggle aria-label="Switch to dark theme"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M21 15.2A9 9 0 1 1 8.8 3a7 7 0 0 0 12.2 12.2Z"/></svg></button>
      <a class="public-sign-in" href="${pageContext.request.contextPath}/login">Sign In</a>
      <a class="public-button small" href="${pageContext.request.contextPath}/register">Get Started <span aria-hidden="true">→</span></a>
    </div>
  </div>
</header>
