package com.studenthub.service;

import com.studenthub.dao.CategoryDAO;
import com.studenthub.dao.DeadlineDAO;
import com.studenthub.dao.PostDAO;
import com.studenthub.model.Category;
import com.studenthub.model.Deadline;
import com.studenthub.model.Post;
import java.sql.SQLException;
import java.util.List;

public class DashboardService {
    public record DashboardData(List<Post> posts, List<Category> categories, List<Deadline> deadlines) {
    }

    private final PostDAO postDAO;
    private final CategoryDAO categoryDAO;
    private final DeadlineDAO deadlineDAO;

    public DashboardService() {
        this(new PostDAO(), new CategoryDAO(), new DeadlineDAO());
    }

    DashboardService(PostDAO postDAO, CategoryDAO categoryDAO, DeadlineDAO deadlineDAO) {
        this.postDAO = postDAO;
        this.categoryDAO = categoryDAO;
        this.deadlineDAO = deadlineDAO;
    }

    public DashboardData load(long userId, Long categoryId) throws SQLException {
        return new DashboardData(postDAO.findVisibleForUser(userId, categoryId),
                categoryDAO.findAll(), deadlineDAO.findUpcomingForUser(userId, 5));
    }
}
