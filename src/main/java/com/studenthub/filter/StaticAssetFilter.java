package com.studenthub.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

@WebFilter(urlPatterns = "/assets/*")
public class StaticAssetFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (response instanceof HttpServletResponse httpResponse && request instanceof HttpServletRequest httpRequest) {
            Object configured = httpRequest.getServletContext().getAttribute("assetVersion");
            String version = configured == null ? "dev" : configured.toString();
            String etag = "\"studenthub-" + version.replaceAll("[^A-Za-z0-9._-]", "") + "\"";
            httpResponse.setHeader("Cache-Control", "public, max-age=31536000, immutable");
            httpResponse.setHeader("ETag", etag);
            if (etag.equals(httpRequest.getHeader("If-None-Match"))) {
                httpResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
