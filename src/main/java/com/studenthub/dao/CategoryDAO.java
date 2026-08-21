package com.studenthub.dao;

import com.studenthub.model.Category;
import com.studenthub.util.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    private static volatile List<Category> cachedCategories = null;
    private static volatile long lastFetchTime = 0;
    private static final long CACHE_TTL_MS = 300_000; // 5 minutes

    public List<Category> findAll() throws SQLException {
        List<Category> local = cachedCategories;
        long now = System.currentTimeMillis();
        if (local != null && (now - lastFetchTime < CACHE_TTL_MS)) {
            return local;
        }

        List<Category> categories = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT category_id, category_name FROM categories ORDER BY category_name");
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                categories.add(new Category(results.getLong("category_id"), results.getString("category_name")));
            }
        }
        cachedCategories = categories;
        lastFetchTime = now;
        return categories;
    }

    public static void invalidateCache() {
        cachedCategories = null;
        lastFetchTime = 0;
    }

    public boolean exists(Connection connection, long categoryId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM categories WHERE category_id = ?")) {
            statement.setLong(1, categoryId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    public String findNameById(Connection connection, long categoryId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT category_name FROM categories WHERE category_id = ?")) {
            statement.setLong(1, categoryId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getString("category_name") : null;
            }
        }
    }

    public String findNameById(long categoryId) throws SQLException {
        for (Category cat : findAll()) {
            if (cat.getCategoryId() == categoryId) {
                return cat.getCategoryName();
            }
        }
        try (Connection connection = DBConnection.getConnection()) {
            return findNameById(connection, categoryId);
        }
    }
}
