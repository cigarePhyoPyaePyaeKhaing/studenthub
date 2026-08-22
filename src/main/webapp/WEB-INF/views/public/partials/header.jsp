<%@ page contentType="text/html;charset=UTF-8" %>
<header class="public-header">
  <div class="public-container public-nav-shell">
    <a class="public-brand" href="${pageContext.request.contextPath}/" aria-label="StudentHub home"><span class="brand-glyph"><span>S</span></span><span>StudentHub</span></a>
    <nav class="public-nav" data-public-nav aria-label="Primary navigation">
      <a class="${publicPage eq 'home' ? 'active' : ''}" href="${pageContext.request.contextPath}/">Home</a>
      <a class="${publicPage eq 'features' ? 'active' : ''}" href="${pageContext.request.contextPath}/features">Features</a>
      <a class="${publicPage eq 'how-it-works' ? 'active' : ''}" href="${pageContext.request.contextPath}/how-it-works">How It Works</a>
      <a class="${publicPage eq 'about' ? 'active' : ''}" href="${pageContext.request.contextPath}/about">About</a>
      <div class="public-nav-mobile-actions"><a href="${pageContext.request.contextPath}/login">Sign In</a><a class="public-button small" href="${pageContext.request.contextPath}/register">Get Started</a></div>
    </nav>
    <div class="public-actions">
      <button class="icon-button" type="button" data-theme-toggle aria-label="Switch color theme">
        <svg class="theme-moon" viewBox="0 0 24 24" aria-hidden="true"><path d="M21 15.2A9 9 0 1 1 8.8 3a7 7 0 0 0 12.2 12.2Z"/></svg>
        <svg class="theme-sun" viewBox="0 0 24 24" aria-hidden="true" hidden><circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.93 4.93l1.42 1.42M17.66 17.66l1.41 1.41M2 12h2M20 12h2M4.93 19.07l1.42-1.42M17.66 6.34l1.41-1.41"/></svg>
      </button>
      <a class="public-sign-in" href="${pageContext.request.contextPath}/login">Sign In</a>
      <a class="public-button small" href="${pageContext.request.contextPath}/register">Get Started <span aria-hidden="true">→</span></a>
      <button class="menu-button" type="button" data-menu-toggle aria-expanded="false" aria-label="Open navigation"><span></span><span></span><span></span></button>
    </div>
  </div>
</header>
