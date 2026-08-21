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
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

@WebServlet(name = "RegisterServlet", urlPatterns = "/register")
public class RegisterServlet extends HttpServlet {
    private final AuthService authService = new AuthService();
    private final com.studenthub.dao.UniversityDAO universityDAO = new com.studenthub.dao.UniversityDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("universities", universityDAO.findActive());
        } catch (SQLException exception) {
            getServletContext().log("Failed to load active universities for registration: " + exception.getClass().getName());
            request.setAttribute("universities", java.util.List.of());
        }
        request.setAttribute("csrfToken", CsrfToken.getOrCreate(request.getSession(true)));
        request.getRequestDispatcher("/WEB-INF/views/auth/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!CsrfToken.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        Long universityId = parseLong(request.getParameter("universityId"));
        Integer semester = parseInt(request.getParameter("semester"));
        String sectionName = request.getParameter("sectionName");

        try {
            AuthService.RegistrationResult result = authService.register(request.getParameter("studentId"),
                    request.getParameter("fullName"), request.getParameter("email"),
                    request.getParameter("password"), request.getParameter("confirmPassword"),
                    universityId, semester, sectionName);
            if (result.successful()) {
                request.getSession().setAttribute("pendingVerificationUserId", result.userId());
                response.sendRedirect(request.getContextPath() + "/verify-email");
                return;
            }
            request.setAttribute("error", result.message());
        } catch (SQLException exception) {
            logSQLExceptionChain(exception);
            request.setAttribute("error", "Registration is temporarily unavailable. Please try again.");
        } catch (EmailServiceException exception) {
            logEmailExceptionChain(exception);
            request.setAttribute("error", "Your account was saved, but email could not be sent. Try again later.");
        }
        doGet(request, response);
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            long val = Long.parseLong(value.trim());
            return val > 0 ? val : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Integer parseInt(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            int val = Integer.parseInt(value.trim());
            return val > 0 ? val : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private void logSQLExceptionChain(SQLException exception) {
        SQLException current = exception;
        int position = 1;
        while (current != null) {
            String prefix = "Registration SQLException[" + position + "] ";
            getServletContext().log(prefix + "class=" + current.getClass().getName());
            getServletContext().log(prefix + "SQLState=" + safeLogValue(current.getSQLState()));
            getServletContext().log(prefix + "vendorCode=" + current.getErrorCode());
            getServletContext().log(prefix + "message=" + safeLogValue(current.getMessage()));
            SQLException next = current.getNextException();
            if (next == current) {
                break;
            }
            current = next;
            position++;
        }
    }

    private String safeLogValue(String value) {
        if (value == null) {
            return "(none)";
        }
        String sanitized = value.replace('\r', ' ').replace('\n', ' ');
        sanitized = redactEnvironmentValue(sanitized, "BREVO_API_KEY");
        return sanitized;
    }

    private void logEmailExceptionChain(EmailServiceException exception) {
        Set<Throwable> logged = Collections.newSetFromMap(new IdentityHashMap<>());
        Throwable current = exception;
        int position = 1;
        while (current != null && logged.add(current)) {
            String prefix = "Registration email exception[" + position + "] ";
            getServletContext().log(prefix + "class=" + current.getClass().getName());
            getServletContext().log(prefix + "message=" + safeLogValue(current.getMessage()));

            current = current.getCause();
            position++;
        }
    }

    private String redactEnvironmentValue(String message, String variableName) {
        String secret = System.getenv(variableName);
        if (secret == null || secret.isBlank()) {
            return message;
        }
        return message.replace(secret, "[REDACTED]");
    }
}
