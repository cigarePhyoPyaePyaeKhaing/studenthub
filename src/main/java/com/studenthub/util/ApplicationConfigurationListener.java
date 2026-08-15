package com.studenthub.util;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class ApplicationConfigurationListener implements ServletContextListener {
    @Override public void contextInitialized(ServletContextEvent event) {
        SessionCookieConfig cookie = event.getServletContext().getSessionCookieConfig();
        cookie.setHttpOnly(true);
        cookie.setSecure("production".equalsIgnoreCase(System.getenv("APP_ENV")));
        cookie.setName("STUDENTHUB_SESSION");
    }
}
