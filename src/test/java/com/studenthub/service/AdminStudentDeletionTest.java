package com.studenthub.service;

import com.studenthub.dao.AdminDAO;
import com.studenthub.model.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdminStudentDeletionTest {

    @Test
    void testSelfDeletionIsPrevented() throws Exception {
        FakeAdminDAO dao = new FakeAdminDAO();
        AdminService service = new AdminService(dao);

        AdminService.OperationResult result = service.deleteStudent(1L, "ADMIN", 1L);
        assertFalse(result.successful());
        assertEquals("You cannot delete your own active administrator account.", result.message());
        assertFalse(dao.deleteCalled);
    }

    @Test
    void testNonAdminCannotDeleteStudent() throws Exception {
        FakeAdminDAO dao = new FakeAdminDAO();
        AdminService service = new AdminService(dao);

        AdminService.OperationResult studentResult = service.deleteStudent(2L, "STUDENT", 3L);
        assertFalse(studentResult.successful());
        assertEquals("FORBIDDEN", studentResult.message());

        AdminService.OperationResult crResult = service.deleteStudent(2L, "CR", 3L);
        assertFalse(crResult.successful());
        assertEquals("FORBIDDEN", crResult.message());

        assertFalse(dao.deleteCalled);
    }

    private static class FakeAdminDAO extends AdminDAO {
        boolean deleteCalled = false;
    }
}
