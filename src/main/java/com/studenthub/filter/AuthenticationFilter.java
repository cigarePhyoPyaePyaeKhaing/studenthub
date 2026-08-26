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
import com.studenthub.dao.UserDAO;
import com.studenthub.dao.PrivateMessageDAO;
import com.studenthub.util.CsrfToken;
import com.studenthub.util.NavigationSection;
import java.sql.SQLException;
import java.util.Set;

@WebFilter(urlPatterns = "/*")
public class AuthenticationFilter implements Filter {
    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/", "/public-home", "/features", "/how-it-works", "/about",
            "/login", "/register", "/verify-email", "/resend-verification",
            "/forgot-password", "/verify-reset-code", "/reset-password", "/health");
    private static final long UNREAD_COUNT_CACHE_TTL_MS = 30000L;
    private final NotificationDAO notificationDAO=new NotificationDAO();
    private final UserDAO userDAO = new UserDAO();
    private final PrivateMessageDAO privateMessageDAO = new PrivateMessageDAO();
    @Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest http = (HttpServletRequest) request;
        String path = requestPath(http.getContextPath(), http.getRequestURI());
        if (!path.startsWith("/assets/")) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setHeader("Cache-Control", "private, no-cache, must-revalidate");
            httpResponse.setHeader("Pragma", "no-cache");
        }
        if (isPublicPath(path) || !isProtectedPath(path)) { chain.doFilter(request, response); return; }
        HttpSession session = http.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            HttpServletResponse output = (HttpServletResponse) response;
            if (path.equals("/messages/delete")) {
                output.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                output.setContentType("application/json");
                output.setCharacterEncoding("UTF-8");
                output.getWriter().write("{\"success\":false,\"code\":\"DELETE_UNAUTHENTICATED\",\"message\":\"Sign in again to continue.\"}");
            } else output.sendRedirect(http.getContextPath() + "/login");
            return;
        }
        request.setAttribute("csrfToken", CsrfToken.getOrCreate(session));
        request.setAttribute("activeNav", NavigationSection.resolve(http));
        touchPresence(session, (Long) session.getAttribute("userId"), http);
        if (needsUnreadCount(http.getMethod(), http.getContextPath(), http.getRequestURI())) {
            long userId = (Long) session.getAttribute("userId");
            Long cachedCount = (Long) session.getAttribute("cachedUnreadCount");
            Long cachedTime = (Long) session.getAttribute("cachedUnreadTime");
            long now = System.currentTimeMillis();

            if (cachedCount != null && cachedTime != null && (now - cachedTime < UNREAD_COUNT_CACHE_TTL_MS)) {
                request.setAttribute("unreadNotificationCount", cachedCount);
            } else {
                try {
                    long count = notificationDAO.countUnread(userId);
                    session.setAttribute("cachedUnreadCount", count);
                    session.setAttribute("cachedUnreadTime", now);
                    request.setAttribute("unreadNotificationCount", count);
                } catch (SQLException exception) {
                    request.getServletContext().log("Unread notification count failed: " + exception.getClass().getName());
                    request.setAttribute("unreadNotificationCount", cachedCount != null ? cachedCount : 0L);
                }
            }
            try { request.setAttribute("unreadPrivateMessageCount", privateMessageDAO.unread(userId)); }
            catch (SQLException privateException) { request.setAttribute("unreadPrivateMessageCount", 0L); }
        }
        chain.doFilter(request, response);
    }

    private void touchPresence(HttpSession session, long userId, HttpServletRequest request) {
        long now = System.currentTimeMillis();
        Object previous = session.getAttribute("lastPresenceTouch");
        if (previous instanceof Long last && now - last < 60000L) return;
        try {
            userDAO.touchLastActive(userId);
            session.setAttribute("lastPresenceTouch", now);
        } catch (SQLException exception) {
            if (request.getServletContext() != null) {
                request.getServletContext().log("Presence update failed: " + exception.getClass().getName());
            }
        }
    }

    static boolean isPublicPath(String path) {
        return PUBLIC_PATHS.contains(path) || path.startsWith("/assets/");
    }

    static boolean isProtectedPath(String path) {
        return path.equals("/home") || path.equals("/announcements") || path.equals("/notifications")
                || path.equals("/deadlines") || path.startsWith("/deadlines/")
                || path.equals("/discussions") || path.startsWith("/discussions/")
                || path.equals("/messages") || path.startsWith("/messages/")
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
                || path.equals("/deadlines") || path.equals("/discussions") || path.equals("/messages") || path.equals("/profile")
                || path.equals("/posts/comments") || path.equals("/admin") || path.startsWith("/admin/");
    }
}
