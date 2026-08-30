package com.studenthub.controller;

import com.studenthub.dao.PrivateMessageDAO;
import com.studenthub.service.PrivateConversationDeletionService;
import com.studenthub.util.CsrfToken;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "DeletePrivateConversationServlet", urlPatterns = "/messages/delete")
public class DeletePrivateConversationServlet extends HttpServlet {
    private final PrivateConversationDeletionService service;

    public DeletePrivateConversationServlet() {
        this(new PrivateConversationDeletionService());
    }

    DeletePrivateConversationServlet(PrivateMessageDAO dao) {
        this(new PrivateConversationDeletionService(dao));
    }

    DeletePrivateConversationServlet(PrivateConversationDeletionService service) {
        this.service = service;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        auditLog("Private conversation delete: REQUEST_REACHED");
        boolean csrfValid = CsrfToken.isValid(request);
        auditLog("Private conversation delete: CSRF_VALID=" + csrfValid);
        if (!csrfValid) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "DELETE_CSRF_INVALID");
            return;
        }
        Object sessionUser = request.getSession(false) == null ? null : request.getSession(false).getAttribute("userId");
        if (!(sessionUser instanceof Long userId)) {
            auditLog("Private conversation delete rejected: AUTHENTICATED=false");
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "DELETE_UNAUTHENTICATED");
            return;
        }
        auditLog("Private conversation delete: AUTHENTICATED=true, userId=" + userId);
        final long conversationId;
        try {
            conversationId = Long.parseLong(request.getParameter("conversationId"));
            if (conversationId <= 0) throw new IllegalArgumentException("Conversation ID must be positive.");
        } catch (RuntimeException exception) {
            auditLog("Private conversation delete rejected: INVALID_CONVERSATION_ID");
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "DELETE_INVALID_ID");
            return;
        }
        auditLog("Private conversation delete: conversationId=" + conversationId + ", DAO_CALLED=true");
        try {
            PrivateConversationDeletionService.DeleteResult result = service.deleteForUser(conversationId, userId);
            auditLog("Private conversation delete: conversationId=" + conversationId
                    + ", userId=" + userId + ", MEMBERSHIP=" + result.participant()
                    + ", affectedRows=" + result.affectedRows() + ", code=" + result.code());
            if (!result.participant()) {
                writeError(response, HttpServletResponse.SC_FORBIDDEN, result.code());
                return;
            }
            writeJson(response, HttpServletResponse.SC_OK,
                    "{\"success\":true,\"code\":\"DELETE_OK\"}");
        } catch (SQLException exception) {
            auditLog("Private conversation delete failed: DATABASE_ERROR, conversationId="
                    + conversationId + ", userId=" + userId + ", exceptionClass="
                    + exception.getClass().getName() + ", SQLState=" + safeSqlState(exception)
                    + ", vendorCode=" + exception.getErrorCode());
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "DELETE_DB_ERROR");
        } catch (RuntimeException exception) {
            auditLog("Private conversation delete failed: SERVER_ERROR, conversationId="
                    + conversationId + ", userId=" + userId + ", exceptionClass="
                    + exception.getClass().getName());
            writeError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "DELETE_SERVER_ERROR");
        }
    }

    private void writeError(HttpServletResponse response, int status, String code) throws IOException {
        writeJson(response, status, "{\"success\":false,\"code\":\"" + code
                + "\",\"message\":\"Could not delete this conversation.\"}");
    }

    private void writeJson(HttpServletResponse response, int status, String json) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }

    private void auditLog(String message) {
        if (getServletConfig() != null) getServletContext().log(message);
    }

    private String safeSqlState(SQLException exception) {
        String state = exception.getSQLState();
        return state == null || !state.matches("[A-Za-z0-9]{1,8}") ? "unknown" : state;
    }
}
