package com.studenthub.service;

import com.studenthub.dao.AdminDAO;
import com.studenthub.model.*;
import com.studenthub.util.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class AdminService {
    public record DashboardData(AdminDashboardStats stats, List<AdminUserSummary> recentUsers) {}
    public record UserPage(List<AdminUserSummary> users, String search, int page,
                           int totalPages, long totalUsers) {}
    public record OperationResult(boolean successful, String message) {}
    private final AdminDAO dao = new AdminDAO();

    public DashboardData dashboard() throws SQLException {
        return new DashboardData(dao.loadStats(), dao.findRecentUsers(8));
    }
    public UserPage users(String searchInput, String pageInput) throws SQLException {
        if (AdminValidation.searchTooLong(searchInput)) throw new IllegalArgumentException("Search is too long.");
        String search = AdminValidation.normalizeSearch(searchInput);
        long total = dao.countUsers(search);
        int totalPages = AdminValidation.totalPages(total);
        int page = Math.min(AdminValidation.page(pageInput), totalPages);
        int offset = (page - 1) * AdminValidation.PAGE_SIZE;
        return new UserPage(dao.findUsers(search, AdminValidation.PAGE_SIZE, offset),
                search, page, totalPages, total);
    }
    public Optional<AdminUserSummary> user(long userId) throws SQLException { return dao.findUser(userId); }

    public OperationResult changeRole(long actingAdminId, Object actingRole, long targetUserId,
                                      String roleInput) throws SQLException {
        if (!Authorization.isAdmin(actingRole)) return new OperationResult(false, "FORBIDDEN");
        Role newRole = AdminValidation.role(roleInput);
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                List<Long> admins = dao.lockAdminIds(connection);
                Role currentRole = dao.lockUserRole(connection, targetUserId);
                AdminRolePolicy.Decision decision = AdminRolePolicy.decide(true, actingAdminId,
                        targetUserId, currentRole, newRole, admins.size());
                if (decision != AdminRolePolicy.Decision.ALLOWED) {
                    connection.rollback();
                    return new OperationResult(decision == AdminRolePolicy.Decision.NO_CHANGE,
                            message(decision));
                }
                if (dao.updateRole(connection, targetUserId, newRole) != 1) {
                    connection.rollback(); return new OperationResult(false, "NOT_FOUND");
                }
                connection.commit();
                return new OperationResult(true, "User role updated to " + newRole.name() + ".");
            } catch (SQLException exception) {
                connection.rollback(); throw exception;
            } finally { connection.setAutoCommit(true); }
        }
    }
    private String message(AdminRolePolicy.Decision decision) {
        return switch (decision) {
            case NO_CHANGE -> "The user already has that role.";
            case SELF_DEMOTION -> "You cannot demote your own active administrator account.";
            case LAST_ADMIN -> "The final administrator account cannot be demoted.";
            case INVALID_ROLE -> "Select a valid role.";
            case NOT_FOUND -> "NOT_FOUND";
            default -> "FORBIDDEN";
        };
    }
}
