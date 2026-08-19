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

@WebFilter(urlPatterns = {"/home", "/announcements", "/notifications", "/posts/*", "/app/*", "/feed/*", "/deadlines", "/deadlines/*", "/discussions", "/discussions/*", "/profile", "/profile/*", "/admin", "/admin/*"})
public class AuthenticationFilter implements Filter {
    private final NotificationDAO notificationDAO=new NotificationDAO();
    @Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest http = (HttpServletRequest) request; HttpSession session = http.getSession(false);
        if (session == null || session.getAttribute("userId") == null) { ((HttpServletResponse) response).sendRedirect(http.getContextPath() + "/login"); return; }
        request.setAttribute("csrfToken",CsrfToken.getOrCreate(session));
        if (needsUnreadCount(http.getMethod(), http.getContextPath(), http.getRequestURI())) {
            try{request.setAttribute("unreadNotificationCount",notificationDAO.countUnread((Long)session.getAttribute("userId")));}
            catch(SQLException exception){request.getServletContext().log("Unread notification count failed: "+exception.getClass().getName());request.setAttribute("unreadNotificationCount",0L);}
        }
        chain.doFilter(request, response);
    }

    static boolean needsUnreadCount(String method, String contextPath, String requestUri) {
        if (!"GET".equalsIgnoreCase(method)) return false;
        String path = requestUri.substring(Math.min(contextPath.length(), requestUri.length()));
        return path.equals("/home") || path.equals("/announcements") || path.equals("/notifications")
                || path.equals("/deadlines") || path.equals("/discussions") || path.equals("/profile")
                || path.equals("/posts/comments") || path.equals("/admin") || path.startsWith("/admin/users");
    }
}
