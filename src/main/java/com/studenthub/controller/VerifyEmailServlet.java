package com.studenthub.controller;

import com.studenthub.service.AuthService;
import com.studenthub.service.OtpService;
import com.studenthub.util.CsrfToken;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "VerifyEmailServlet", urlPatterns = "/verify-email")
public class VerifyEmailServlet extends HttpServlet {
    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (request.getSession(false) == null
                || request.getSession(false).getAttribute("pendingVerificationUserId") == null) {
            response.sendRedirect(request.getContextPath() + "/register");
            return;
        }
        request.setAttribute("csrfToken", CsrfToken.getOrCreate(request.getSession()));
        request.getRequestDispatcher("/WEB-INF/views/auth/verify-email.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!CsrfToken.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        Object pending = request.getSession().getAttribute("pendingVerificationUserId");
        if (!(pending instanceof Long userId)) {
            response.sendRedirect(request.getContextPath() + "/register");
            return;
        }
        try {
            OtpService.VerificationResult result = authService.verifyEmail(userId, request.getParameter("code"));
            if (result == OtpService.VerificationResult.SUCCESS) {
                request.getSession().removeAttribute("pendingVerificationUserId");
                request.getSession().setAttribute("flash", "Email verified. You can now sign in.");
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
            request.setAttribute("error", switch (result) {
                case EXPIRED -> "This code has expired.";
                case TOO_MANY_ATTEMPTS -> "Too many incorrect attempts. Request a new code.";
                default -> "The verification code is incorrect.";
            });
        } catch (SQLException exception) {
            getServletContext().log("Email verification database failure: " + exception.getClass().getName());
            request.setAttribute("error", "Verification is temporarily unavailable.");
        }
        doGet(request, response);
    }
}
