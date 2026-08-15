package com.studenthub.controller;

import com.studenthub.service.AuthService;
import com.studenthub.service.EmailServiceException;
import com.studenthub.util.CsrfToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "ForgotPasswordServlet", urlPatterns = "/forgot-password")
public class ForgotPasswordServlet extends HttpServlet {
    private final AuthService authService = new AuthService();

    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("csrfToken", CsrfToken.getOrCreate(request.getSession(true)));
        request.getRequestDispatcher("/WEB-INF/views/auth/forgot-password.jsp").forward(request, response);
    }

    @Override protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!CsrfToken.isValid(request)) { response.sendError(403); return; }
        String login = request.getParameter("login");
        try {
            authService.requestPasswordReset(login);
            authService.resolveUserId(login).ifPresent(id -> request.getSession().setAttribute("pendingResetUserId", id));
        } catch (SQLException | EmailServiceException | IllegalStateException exception) {
            getServletContext().log("Password reset request failed: " + exception.getClass().getName());
        }
        request.setAttribute("message", "If an account matches the information provided, a verification code has been sent.");
        request.setAttribute("showVerifyLink", true);
        doGet(request, response);
    }
}
