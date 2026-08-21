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
import com.studenthub.dao.NotificationDAO;
import com.studenthub.util.CsrfToken;
import java.sql.SQLException;
import java.util.Set;

@WebFilter(urlPatterns = "/*")
public class AuthenticationFilter implements Filter {
    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/", "/login", "/register", "/verify-email", "/resend-verification",
            "/forgot-password", "/verify-reset-code", "/reset-password", "/health");
    private final NotificationDAO notificationDAO=new NotificationDAO();
    @Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest http = (HttpServletRequest) request;
        String path = requestPath(http.getContextPath(), http.getRequestURI());
        if (isPublicPath(path) || !isProtectedPath(path)) { chain.doFilter(request, response); return; }
        HttpSession session = http.getSession(false);
        if (session == null || session.getAttribute("userId") == null) { ((HttpServletResponse) response).sendRedirect(http.getContextPath() + "/login"); return; }
        request.setAttribute("csrfToken",CsrfToken.getOrCreate(session));
        if (needsUnreadCount(http.getMethod(), http.getContextPath(), http.getRequestURI())) {
            try{request.setAttribute("unreadNotificationCount",notificationDAO.countUnread((Long)session.getAttribute("userId")));}
            catch(SQLException exception){request.getServletContext().log("Unread notification count failed: "+exception.getClass().getName());request.setAttribute("unreadNotificationCount",0L);}
        }
        chain.doFilter(request, response);
    }

    static boolean isPublicPath(String path) {
        return PUBLIC_PATHS.contains(path) || path.startsWith("/assets/");
    }

    static boolean isProtectedPath(String path) {
        return path.equals("/home") || path.equals("/announcements") || path.equals("/notifications")
                || path.equals("/deadlines") || path.startsWith("/deadlines/")
                || path.equals("/discussions") || path.startsWith("/discussions/")
                || path.equals("/profile") || path.startsWith("/profile/")
                || path.equals("/admin") || path.startsWith("/admin/")
                || path.equals("/users") || path.startsWith("/users/")
                || path.equals("/academic-requests") || path.startsWith("/academic-requests/")
                || path.startsWith("/posts/") || path.startsWith("/app/") || path.startsWith("/feed/");
    }

    private static String requestPath(String contextPath, String requestUri) {
        return requestUri.substring(Math.min(contextPath.length(), requestUri.length()));
    }

    static boolean needsUnreadCount(String method, String contextPath, String requestUri) {
        if (!"GET".equalsIgnoreCase(method)) return false;
        String path = requestUri.substring(Math.min(contextPath.length(), requestUri.length()));
        return path.equals("/home") || path.equals("/announcements") || path.equals("/notifications")
                || path.equals("/deadlines") || path.equals("/discussions") || path.equals("/profile")
                || path.equals("/posts/comments") || path.equals("/admin") || path.startsWith("/admin/");
    }
}
