<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<link href="${pageContext.request.contextPath}/assets/css/experience-upgrade.css?v=20260822" rel="stylesheet"><nav class="auth-navbar" aria-label="Authentication navigation">
  <a class="auth-brand" href="${pageContext.request.contextPath}/"><span class="auth-brand-mark"><span>S</span></span><span>StudentHub</span></a>
  <div class="auth-nav-actions">
    <a data-i18n="home" href="${pageContext.request.contextPath}/">Home</a>
    <a href="${pageContext.request.contextPath}${param.accountPath}"><c:out value="${param.accountLabel}" /></a>
    <div class="language-switch" aria-label="Language"><button type="button" data-language="en">EN</button><button type="button" data-language="my">မြန်မာ</button></div><button class="simple-theme-toggle auth-inline-theme theme-control" type="button" data-simple-theme-toggle aria-label="Switch color theme"></button>
  </div>
</nav>
<script src="${pageContext.request.contextPath}/assets/js/language.js?v=20260822" defer></script>
