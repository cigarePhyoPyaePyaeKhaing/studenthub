package com.studenthub.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Central JDBC connection factory backed by HikariCP connection pool.
 */
public final class DBConnection {
    private static final String MYSQL_DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
    private static final String URL_VARIABLE = "STUDENTHUB_DB_URL";
    private static final String USER_VARIABLE = "STUDENTHUB_DB_USER";
    private static final String PASSWORD_VARIABLE = "STUDENTHUB_DB_PASSWORD";
    private static final ClassNotFoundException DRIVER_LOADING_FAILURE;

    private static volatile HikariDataSource dataSource;
    private static final Object LOCK = new Object();

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

        HikariDataSource ds = getOrCreateDataSource();
        if (ds != null) {
            return ds.getConnection();
        }

        // Direct fallback if pooling fails or in lightweight environments
        String url = requireEnvironmentVariable(URL_VARIABLE);
        String user = requireEnvironmentVariable(USER_VARIABLE);
        String password = requireEnvironmentVariable(PASSWORD_VARIABLE);
        return DriverManager.getConnection(url, user, password);
    }

    private static HikariDataSource getOrCreateDataSource() {
        if (dataSource == null) {
            synchronized (LOCK) {
                if (dataSource == null) {
                    try {
                        String url = System.getenv(URL_VARIABLE);
                        String user = System.getenv(USER_VARIABLE);
                        String password = System.getenv(PASSWORD_VARIABLE);
                        if (url != null && !url.isBlank() && user != null) {
                            HikariConfig config = new HikariConfig();
                            config.setJdbcUrl(url);
                            config.setUsername(user);
                            config.setPassword(password != null ? password : "");
                            config.setDriverClassName(MYSQL_DRIVER_CLASS);
                            config.setPoolName("StudentHubHikariPool");
                            config.setMaximumPoolSize(10);
                            config.setMinimumIdle(5);
                            config.setConnectionTimeout(5000);
                            config.setValidationTimeout(2000);
                            config.setKeepaliveTime(30000);
                            config.setIdleTimeout(120000);
                            config.setMaxLifetime(1800000);
                            config.addDataSourceProperty("cachePrepStmts", "true");
                            config.addDataSourceProperty("prepStmtCacheSize", "250");
                            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
                            config.addDataSourceProperty("useServerPrepStmts", "true");
                            config.addDataSourceProperty("useLocalSessionState", "true");
                            config.addDataSourceProperty("rewriteBatchedStatements", "true");
                            config.addDataSourceProperty("cacheResultSetMetadata", "true");
                            config.addDataSourceProperty("cacheServerConfiguration", "true");
                            config.addDataSourceProperty("elideSetAutoCommits", "true");
                            config.addDataSourceProperty("maintainTimeStats", "false");
                            dataSource = new HikariDataSource(config);
                        }
                    } catch (Exception e) {
                        System.err.println("HikariCP initialization note: " + e.getMessage());
                    }
                }
            }
        }
        return dataSource;
    }

    public static void closeDataSource() {
        synchronized (LOCK) {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
                dataSource = null;
            }
        }
    }

    private static String requireEnvironmentVariable(String variableName) throws SQLException {
        String value = System.getenv(variableName);
        if (value == null || value.isBlank()) {
            throw new SQLException("Required database environment variable is not configured: " + variableName);
        }
        return value;
    }
}
