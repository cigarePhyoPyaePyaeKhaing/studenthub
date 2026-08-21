package com.studenthub.service;

import com.studenthub.dao.CategoryDAO;
import com.studenthub.dao.DeadlineDAO;
import com.studenthub.dao.PostDAO;
import com.studenthub.model.Category;
import com.studenthub.model.Deadline;
import com.studenthub.model.Post;
import com.studenthub.model.Role;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DashboardServiceTest {

    @Test
    void loadWithCompleteData() throws SQLException {
        PostDAO postDAO = new PostDAO() {
            @Override
            public List<Post> findVisibleForUser(long viewerId, Long categoryId) {
                return List.of(new Post(1L, 10L, 2L, "Alice", Role.CR, "Academic",
                        "Exam Schedule", "Details here", null, "ALL", LocalDateTime.now(), 5L, 2L, false));
            }
        };
        CategoryDAO categoryDAO = new CategoryDAO() {
            @Override
            public List<Category> findAll() {
                return List.of(new Category(1L, "General"), new Category(2L, "Academic"));
            }
        };
        DeadlineDAO deadlineDAO = new DeadlineDAO() {
            @Override
            public List<Deadline> findUpcomingForUser(long viewerId, int limit) {
                return List.of(new Deadline(1L, 1L, "Exam Schedule", "Math Assignment",
                        "Mathematics", LocalDateTime.now().plusDays(3), 3, "A", 10L, "Alice", LocalDateTime.now()));
            }
        };

        DashboardService service = new DashboardService(postDAO, categoryDAO, deadlineDAO);
        DashboardService.DashboardData data = service.load(100L, null);

        assertNotNull(data);
        assertEquals(1, data.posts().size());
        assertEquals("Exam Schedule", data.posts().get(0).title());
        assertEquals(2, data.categories().size());
        assertEquals(1, data.deadlines().size());
        assertEquals("Math Assignment", data.deadlines().get(0).title());
    }

    @Test
    void loadWithZeroRecordsYieldsEmptyListsNotError() throws SQLException {
        PostDAO postDAO = new PostDAO() {
            @Override
            public List<Post> findVisibleForUser(long viewerId, Long categoryId) {
                return List.of();
            }
        };
        CategoryDAO categoryDAO = new CategoryDAO() {
            @Override
            public List<Category> findAll() {
                return List.of();
            }
        };
        DeadlineDAO deadlineDAO = new DeadlineDAO() {
            @Override
            public List<Deadline> findUpcomingForUser(long viewerId, int limit) {
                return List.of();
            }
        };

        DashboardService service = new DashboardService(postDAO, categoryDAO, deadlineDAO);
        DashboardService.DashboardData data = service.load(200L, null);

        assertNotNull(data);
        assertTrue(data.posts().isEmpty());
        assertTrue(data.categories().isEmpty());
        assertTrue(data.deadlines().isEmpty());
    }

    @Test
    void loadPropagatesSqlExceptionOnFailure() {
        PostDAO postDAO = new PostDAO() {
            @Override
            public List<Post> findVisibleForUser(long viewerId, Long categoryId) {
                return List.of();
            }
        };
        CategoryDAO categoryDAO = new CategoryDAO() {
            @Override
            public List<Category> findAll() {
                return List.of();
            }
        };
        DeadlineDAO deadlineDAO = new DeadlineDAO() {
            @Override
            public List<Deadline> findUpcomingForUser(long viewerId, int limit) throws SQLException {
                throw new SQLException("Illegal mix of collations", "HY000", 1267);
            }
        };

        DashboardService service = new DashboardService(postDAO, categoryDAO, deadlineDAO);

        SQLException ex = assertThrows(SQLException.class, () -> service.load(300L, null));
        assertEquals("HY000", ex.getSQLState());
        assertEquals(1267, ex.getErrorCode());
    }
}
