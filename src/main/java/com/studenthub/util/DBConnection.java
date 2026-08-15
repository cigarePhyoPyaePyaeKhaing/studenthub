package com.studenthub.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Central JDBC connection factory. Configuration is supplied by the runtime
 * environment so the same application artifact can run locally or in production.
 */
public final class DBConnection {
    private static final String MYSQL_DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
    private static final String URL_VARIABLE = "STUDENTHUB_DB_URL";
    private static final String USER_VARIABLE = "STUDENTHUB_DB_USER";
    private static final String PASSWORD_VARIABLE = "STUDENTHUB_DB_PASSWORD";
    private static final ClassNotFoundException DRIVER_LOADING_FAILURE;

    static {
        ClassNotFoundException loadingFailure = null;
        try {
            Class.forName(MYSQL_DRIVER_CLASS);
        } catch (ClassNotFoundException exception) {
            loadingFailure = exception;
        }
        DRIVER_LOADING_FAILURE = loadingFailure;
    }

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        if (DRIVER_LOADING_FAILURE != null) {
            throw new SQLException(
                    "MySQL JDBC driver could not be loaded. Verify that MySQL Connector/J is available to the application.",
                    "08001",
                    DRIVER_LOADING_FAILURE);
        }

        String url = requireEnvironmentVariable(URL_VARIABLE);
        String user = requireEnvironmentVariable(USER_VARIABLE);
        String password = requireEnvironmentVariable(PASSWORD_VARIABLE);

        return DriverManager.getConnection(url, user, password);
    }

    private static String requireEnvironmentVariable(String variableName) throws SQLException {
        String value = System.getenv(variableName);
        if (value == null || value.isBlank()) {
            throw new SQLException("Required database environment variable is not configured: " + variableName);
        }
        return value;
    }
}
