<%@ page contentType="text/html;charset=UTF-8" %>
<link href="${pageContext.request.contextPath}/assets/css/experience-upgrade.css?v=20260822" rel="stylesheet"><header class="public-header">
  <div class="public-container public-nav-shell">
    <a class="public-brand" href="${pageContext.request.contextPath}/" aria-label="StudentHub home"><span class="brand-glyph"><span>S</span></span><span>StudentHub</span></a>
    <nav class="public-nav" data-public-nav aria-label="Primary navigation">
      <a data-i18n="home" class="${publicPage eq 'home' ? 'active' : ''}" href="${pageContext.request.contextPath}/">Home</a>
      <a data-i18n="features" class="${publicPage eq 'features' ? 'active' : ''}" href="${pageContext.request.contextPath}/features">Features</a>
      <a data-i18n="how" class="${publicPage eq 'how-it-works' ? 'active' : ''}" href="${pageContext.request.contextPath}/how-it-works">How It Works</a>
      <a data-i18n="about" class="${publicPage eq 'about' ? 'active' : ''}" href="${pageContext.request.contextPath}/about">About</a>
      <div class="public-nav-mobile-actions"><a href="${pageContext.request.contextPath}/login">Sign In</a><a class="public-button small" href="${pageContext.request.contextPath}/register">Get Started</a></div>
    </nav>
    <div class="public-actions">
      <div class="language-switch" aria-label="Language"><button type="button" data-language="en">EN</button><button type="button" data-language="my">မြန်မာ</button></div><button class="icon-button" type="button" data-theme-toggle aria-label="Switch to dark theme"><svg viewBox="0 0 24 24" aria-hidden="true"><path d="M21 15.2A9 9 0 1 1 8.8 3a7 7 0 0 0 12.2 12.2Z"/></svg></button>
      <a class="public-sign-in" href="${pageContext.request.contextPath}/login">Sign In</a>
      <a class="public-button small" href="${pageContext.request.contextPath}/register">Get Started <span aria-hidden="true">→</span></a>
      <button class="menu-button" type="button" data-menu-toggle aria-expanded="false" aria-label="Open navigation"><span></span><span></span><span></span></button>
    </div>
  </div>
</header>
<script src="${pageContext.request.contextPath}/assets/js/language.js?v=20260822" defer></script>
