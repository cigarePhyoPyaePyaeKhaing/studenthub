package com.studenthub.service;

import com.studenthub.dao.CategoryDAO;
import com.studenthub.dao.PostDAO;
import com.studenthub.model.Category;
import com.studenthub.model.Post;
import java.sql.SQLException;
import java.util.List;

public class DashboardService {
    public record DashboardData(List<Post> posts, List<Category> categories, List<Post> deadlines) {
    }

    private final PostDAO postDAO;
    private final CategoryDAO categoryDAO;

    public DashboardService() {
        this(new PostDAO(), new CategoryDAO());
    }

    DashboardService(PostDAO postDAO, CategoryDAO categoryDAO) {
        this.postDAO = postDAO;
        this.categoryDAO = categoryDAO;
    }

    public DashboardData load(long userId, Long categoryId) throws SQLException {
        return new DashboardData(postDAO.findVisibleForUser(userId, categoryId),
                categoryDAO.findAll(), postDAO.findUpcomingDeadlinesForUser(userId, 5));
    }
}
