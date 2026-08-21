package com.studenthub.controller;

import com.studenthub.model.User;
import com.studenthub.service.AuthService;
import com.studenthub.util.CsrfToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "LoginServlet", urlPatterns = "/login")
public class LoginServlet extends HttpServlet {
    private final AuthService authService;

    public LoginServlet() {
        this(new AuthService());
    }

    public LoginServlet(AuthService authService) {
        this.authService = authService != null ? authService : new AuthService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        request.setAttribute("csrfToken", CsrfToken.getOrCreate(session));
        Object flash = session.getAttribute("flash");
        if (flash != null) {
            request.setAttribute("message", flash);
            session.removeAttribute("flash");
        }
        request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!CsrfToken.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        long startTime = System.currentTimeMillis();
        try {
            AuthService.LoginResult result = authService.login(request.getParameter("login"),
                    request.getParameter("password"));
            logSafe("Authentication processed in " + (System.currentTimeMillis() - startTime) + " ms");
            if (result.status() == AuthService.LoginStatus.SUCCESS) {
                User user = result.user();
                request.getSession().invalidate();
                HttpSession authenticated = request.getSession(true);
                authenticated.setAttribute("userId", user.userId());
                authenticated.setAttribute("studentId", user.studentId());
                authenticated.setAttribute("fullName", user.fullName());
                authenticated.setAttribute("role", user.role().name());
                CsrfToken.getOrCreate(authenticated);
                response.sendRedirect(request.getContextPath() + "/home");
                return;
            }
            if (result.status() == AuthService.LoginStatus.EMAIL_NOT_VERIFIED) {
                User user = result.user();
                request.getSession().invalidate();
                HttpSession verificationSession = request.getSession(true);
                verificationSession.setAttribute("pendingVerificationUserId", user.userId());
                CsrfToken.getOrCreate(verificationSession);
                response.sendRedirect(request.getContextPath() + "/verify-email");
                return;
            }
            request.setAttribute("error", "Invalid student ID/email or password.");
        } catch (SQLException exception) {
            logSafe("Login database failure: " + exception.getClass().getName());
            request.setAttribute("error", "Sign in is temporarily unavailable.");
        }
        doGet(request, response);
    }

    private void logSafe(String message) {
        try {
            if (getServletConfig() != null && getServletContext() != null) {
                getServletContext().log(message);
            }
        } catch (Exception ignored) {
        }
    }
}
