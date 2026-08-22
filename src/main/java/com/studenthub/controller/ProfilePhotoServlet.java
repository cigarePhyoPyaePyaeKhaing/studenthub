package com.studenthub.controller;

import com.studenthub.util.Authorization;
import com.studenthub.util.ProfilePhotoStorage;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@WebServlet(name = "ProfilePhotoServlet", urlPatterns = "/profile/photo/*")
public class ProfilePhotoServlet extends HttpServlet {
    private final ProfilePhotoStorage storage = new ProfilePhotoStorage();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!Authorization.isAuthenticated(request.getSession(false))) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String pathInfo = request.getPathInfo();
        String filename = pathInfo == null || pathInfo.length() < 2 ? null : pathInfo.substring(1);
        Path photo = storage.find(filename).orElse(null);
        if (photo == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setContentType(contentType(filename));
        response.setHeader("Cache-Control", "private, max-age=86400");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setContentLengthLong(Files.size(photo));
        Files.copy(photo, response.getOutputStream());
    }

    private String contentType(String filename) {
        if (filename.endsWith(".png")) return "image/png";
        if (filename.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }
}
