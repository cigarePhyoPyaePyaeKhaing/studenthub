package com.studenthub.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Set;

@WebFilter(urlPatterns = {"/cr/*", "/admin/*"})
public class RoleAuthorizationFilter implements Filter {
    @Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest http = (HttpServletRequest) request; HttpSession session = http.getSession(false);
        if (session == null || session.getAttribute("userId") == null) { ((HttpServletResponse) response).sendRedirect(http.getContextPath() + "/login"); return; }
        String role = String.valueOf(session.getAttribute("role"));
        boolean adminPath = isAdminPath(http.getContextPath(), http.getRequestURI());
        if ((adminPath && !"ADMIN".equals(role)) || (!adminPath && !Set.of("CR", "ADMIN").contains(role))) { ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN); return; }
        chain.doFilter(request, response);
    }

    static boolean isAdminPath(String contextPath, String requestUri) {
        String adminRoot = contextPath + "/admin";
        return requestUri.equals(adminRoot) || requestUri.startsWith(adminRoot + "/");
    }
}
