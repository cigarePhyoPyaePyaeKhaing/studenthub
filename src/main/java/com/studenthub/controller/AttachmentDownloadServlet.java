package com.studenthub.controller;

import com.studenthub.dao.AttachmentDAO;
import com.studenthub.dao.DiscussionDAO;
import com.studenthub.dao.PostDAO;
import com.studenthub.model.Attachment;
import com.studenthub.service.AttachmentStorageService;
import com.studenthub.util.Authorization;
import com.studenthub.util.DiscussionAccess;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet(name = "AttachmentDownloadServlet", urlPatterns = {"/attachments/download", "/attachments/view"})
public class AttachmentDownloadServlet extends HttpServlet {
    private final AttachmentDAO attachmentDAO = new AttachmentDAO();
    private final PostDAO postDAO = new PostDAO();
    private final DiscussionDAO discussionDAO = new DiscussionDAO();
    private final AttachmentStorageService storageService = new AttachmentStorageService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (!Authorization.isAuthenticated(session)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        long userId = (Long) session.getAttribute("userId");
        String idParam = request.getParameter("id");
        if (idParam == null || idParam.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        long attachmentId;
        try {
            attachmentId = Long.parseLong(idParam.trim());
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            Optional<Attachment> found = attachmentDAO.findById(attachmentId);
            if (found.isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            Attachment attachment = found.get();
            boolean authorized = checkAuthorization(userId, attachment);
            if (!authorized) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            File file = storageService.resolveFile(attachment.storedFilename());
            if (file == null || !file.exists()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            String dispositionType = request.getServletPath().contains("view") ? "inline" : "attachment";
            String safeFilename = attachment.originalFilename().replace("\"", "\\\"");

            response.setContentType(attachment.mimeType());
            response.setContentLengthLong(file.length());
            response.setHeader("X-Content-Type-Options", "nosniff");
            response.setHeader("Content-Disposition", dispositionType + "; filename=\"" + safeFilename + "\"");

            Files.copy(file.toPath(), response.getOutputStream());
            response.getOutputStream().flush();

        } catch (SQLException exception) {
            getServletContext().log("Attachment fetch failed: " + exception.getClass().getName(), exception);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private boolean checkAuthorization(long userId, Attachment attachment) throws SQLException {
        if ("POST".equalsIgnoreCase(attachment.entityType())) {
            return postDAO.findVisibleById(attachment.entityId(), userId).isPresent();
        } else if ("MESSAGE".equalsIgnoreCase(attachment.entityType())) {
            DiscussionDAO.MessageRecord messageRecord = discussionDAO.findMessage(attachment.entityId());
            if (messageRecord == null) {
                return false;
            }
            DiscussionDAO.AcademicProfile profile = discussionDAO.findAcademicProfile(userId);
            return DiscussionAccess.canAccess(
                    profile.role(),
                    profile.semester(),
                    profile.sectionName(),
                    messageRecord.scope(),
                    messageRecord.semester(),
                    messageRecord.sectionName());
        }
        return false;
    }
}
