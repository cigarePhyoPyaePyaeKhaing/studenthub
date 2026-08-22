package com.studenthub.controller;

import com.studenthub.util.AttachmentStorage;
import com.studenthub.util.AttachmentAuthorization;
import com.studenthub.util.Authorization;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.nio.file.*;

@WebServlet(name="AttachmentServlet", urlPatterns="/attachments/*")
public class AttachmentServlet extends HttpServlet {
    private final AttachmentStorage storage = new AttachmentStorage();
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!Authorization.isAuthenticated(request.getSession(false))) { response.sendError(404); return; }
        String info = request.getPathInfo();
        String storedName = info == null || info.length()<2 ? null : info.substring(1);
        if (!AttachmentAuthorization.canServe(true, storedName)) { response.sendError(404); return; }
        Path file = storage.find(storedName).orElse(null);
        if (file == null) { response.sendError(404); return; }
        String mime = Files.probeContentType(file); response.setContentType(mime == null ? "application/octet-stream" : mime);
        response.setHeader("X-Content-Type-Options","nosniff"); response.setHeader("Cache-Control","private, max-age=86400");
        if (request.getParameter("download") != null) response.setHeader("Content-Disposition","attachment");
        response.setContentLengthLong(Files.size(file)); Files.copy(file,response.getOutputStream());
    }
}
