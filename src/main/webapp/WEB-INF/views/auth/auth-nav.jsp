<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<nav class="auth-navbar" aria-label="Authentication navigation">
  <a class="auth-brand" href="${pageContext.request.contextPath}/"><span class="auth-brand-mark"><span>S</span></span><span>StudentHub</span></a>
  <div class="auth-nav-actions">
    <a href="${pageContext.request.contextPath}/">Home</a>
    <a href="${pageContext.request.contextPath}${param.accountPath}"><c:out value="${param.accountLabel}" /></a>
    <button class="simple-theme-toggle auth-inline-theme theme-control" type="button" data-simple-theme-toggle aria-label="Switch color theme"></button>
  </div>
</nav>
