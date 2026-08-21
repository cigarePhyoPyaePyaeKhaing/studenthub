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
    private final AuthService authService = new AuthService();

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
        try {
            AuthService.LoginResult result = authService.login(request.getParameter("login"),
                    request.getParameter("password"));
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
            request.setAttribute("error", result.status() == AuthService.LoginStatus.EMAIL_NOT_VERIFIED
                    ? "Verify your email before signing in." : "Invalid student ID/email or password.");
        } catch (SQLException exception) {
            getServletContext().log("Login database failure: " + exception.getClass().getName());
            request.setAttribute("error", "Sign in is temporarily unavailable.");
        }
        doGet(request, response);
    }
}
