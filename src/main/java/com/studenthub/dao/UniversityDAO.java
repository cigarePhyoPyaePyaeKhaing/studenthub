package com.studenthub.dao;

import com.studenthub.model.University;
import com.studenthub.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class UniversityDAO {

    public List<University> listApprovedUniversities() throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            return listApprovedUniversities(connection);
        }
    }

    public List<University> listApprovedUniversities(Connection connection) throws SQLException {
        String sql = """
                SELECT university_id, name, short_name, status
                FROM universities
                WHERE status = 'APPROVED'
                ORDER BY name ASC
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {
            List<University> list = new ArrayList<>();
            while (results.next()) {
                list.add(new University(
                        results.getLong("university_id"),
                        results.getString("name"),
                        results.getString("short_name"),
                        results.getString("status")
                ));
            }
            return Collections.unmodifiableList(list);
        }
    }

    public Optional<University> findApprovedById(long universityId) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            return findApprovedById(connection, universityId);
        }
    }

    public Optional<University> findApprovedById(Connection connection, long universityId) throws SQLException {
        String sql = """
                SELECT university_id, name, short_name, status
                FROM universities
                WHERE university_id = ? AND status = 'APPROVED'
                LIMIT 1
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, universityId);
            try (ResultSet results = statement.executeQuery()) {
                if (results.next()) {
                    return Optional.of(new University(
                            results.getLong("university_id"),
                            results.getString("name"),
                            results.getString("short_name"),
                            results.getString("status")
                    ));
                }
                return Optional.empty();
            }
        }
    }
}
