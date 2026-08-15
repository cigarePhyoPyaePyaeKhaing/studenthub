package com.studenthub.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@WebFilter(urlPatterns = "/api/*")
public class CorsFilter implements Filter {
    private final Set<String> allowedOrigins = Stream.of(System.getenv("FRONTEND_URL"), System.getenv("LOCAL_FRONTEND_URL"))
            .filter(value -> value != null && !value.isBlank()).collect(Collectors.toUnmodifiableSet());
    @Override public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest http = (HttpServletRequest) request; HttpServletResponse output = (HttpServletResponse) response;
        String origin = http.getHeader("Origin");
        if (origin != null && allowedOrigins.contains(origin)) {
            output.setHeader("Access-Control-Allow-Origin", origin); output.setHeader("Vary", "Origin");
            output.setHeader("Access-Control-Allow-Credentials", "true");
            output.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            output.setHeader("Access-Control-Allow-Headers", "Content-Type, X-CSRF-Token");
        }
        if ("OPTIONS".equalsIgnoreCase(http.getMethod())) { output.setStatus(204); return; }
        chain.doFilter(request, response);
    }
}
