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
    public List<Category> findAll() throws SQLException {
        List<Category> categories = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT category_id, category_name FROM categories ORDER BY category_name");
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                categories.add(new Category(results.getLong("category_id"), results.getString("category_name")));
            }
        }
        return categories;
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

    public java.util.Optional<String> findNameById(Connection connection, long categoryId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT category_name FROM categories WHERE category_id = ?")) {
            statement.setLong(1, categoryId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? java.util.Optional.of(result.getString(1)) : java.util.Optional.empty();
            }
        }
    }
}
