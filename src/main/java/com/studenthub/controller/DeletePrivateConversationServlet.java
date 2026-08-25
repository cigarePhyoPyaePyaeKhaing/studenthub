package com.studenthub.controller;

import com.studenthub.dao.PrivateMessageDAO;
import com.studenthub.util.CsrfToken;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "DeletePrivateConversationServlet", urlPatterns = "/messages/delete")
public class DeletePrivateConversationServlet extends HttpServlet {
    private final PrivateMessageDAO dao;

    public DeletePrivateConversationServlet() {
        this(new PrivateMessageDAO());
    }

    DeletePrivateConversationServlet(PrivateMessageDAO dao) {
        this.dao = dao;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!CsrfToken.isValid(request)) {
            getServletContext().log("Private conversation delete rejected: CSRF_INVALID");
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        Object sessionUser = request.getSession(false).getAttribute("userId");
        if (!(sessionUser instanceof Long userId)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        final long conversationId;
        try {
            conversationId = Long.parseLong(request.getParameter("conversationId"));
        } catch (RuntimeException exception) {
            getServletContext().log("Private conversation delete rejected: INVALID_CONVERSATION_ID");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try {
            dao.hide(conversationId, userId);
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (SecurityException exception) {
            getServletContext().log("Private conversation delete rejected: NOT_PARTICIPANT");
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        } catch (SQLException exception) {
            getServletContext().log("Private conversation delete failed: DATABASE_ERROR, SQLState="
                    + safeSqlState(exception), exception);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private String safeSqlState(SQLException exception) {
        String state = exception.getSQLState();
        return state == null || !state.matches("[A-Za-z0-9]{1,8}") ? "unknown" : state;
    }
}
