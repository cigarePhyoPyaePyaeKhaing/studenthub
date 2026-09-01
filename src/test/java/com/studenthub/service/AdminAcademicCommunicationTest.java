package com.studenthub.service;

import static org.junit.jupiter.api.Assertions.*;

import com.studenthub.dao.DiscussionDAO;
import com.studenthub.model.DiscussionMessage;
import com.studenthub.model.DiscussionScope;
import com.studenthub.util.DiscussionAccess;
import com.studenthub.util.DiscussionTarget;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AdminAcademicCommunicationTest {
    private MockDiscussionDAO dao;
    private DiscussionService service;

    @BeforeEach
    void setUp() {
        dao = new MockDiscussionDAO();
        service = new DiscussionService(dao, AdminAcademicCommunicationTest::createMockConnection);
    }

    private static Connection createMockConnection() {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    if ("close".equals(method.getName())) return null;
                    if ("setAutoCommit".equals(method.getName())) return null;
                    if ("commit".equals(method.getName())) return null;
                    if ("rollback".equals(method.getName())) return null;
                    if ("isClosed".equals(method.getName())) return false;
                    return null;
                }
        );
    }

    // ==========================================
    // ALL STUDENTS - ADMIN TESTS
    // ==========================================

    @Test
    void studentCanEnterAndSendToAllStudentsAdmin() throws Exception {
        dao.registerUser(1L, "STUDENT", null, null, "Alice Student");
        DiscussionService.RoomView view = service.load(1L, "ALL_STUDENTS_ADMIN");
        assertTrue(view.available());
        assertEquals(DiscussionScope.ALL_STUDENTS_ADMIN, view.scope());

        // Send a message
        DiscussionService.OperationResult sendResult = service.send(1L, "ALL_STUDENTS_ADMIN", "Hello Admin from Student");
        assertTrue(sendResult.successful());
        assertEquals(1, dao.storedMessages.size());
        assertEquals("Hello Admin from Student", dao.storedMessages.get(0).message());
    }

    @Test
    void adminCanEnterAndSendToAllStudentsAdmin() throws Exception {
        dao.registerUser(99L, "ADMIN", null, null, "Admin User");
        DiscussionService.RoomView view = service.load(99L, "ALL_STUDENTS_ADMIN");
        assertTrue(view.available());
        assertEquals(DiscussionScope.ALL_STUDENTS_ADMIN, view.scope());

        DiscussionService.OperationResult sendResult = service.send(99L, "ALL_STUDENTS_ADMIN", "Hello Students from Admin");
        assertTrue(sendResult.successful());
        assertEquals(1, dao.storedMessages.size());
        assertEquals("Hello Students from Admin", dao.storedMessages.get(0).message());
    }

    @Test
    void studentCanReadAdminMessageAndAdminCanReadStudentMessageInAllStudentsAdmin() throws Exception {
        dao.registerUser(1L, "STUDENT", 2, "A", "Student A");
        dao.registerUser(99L, "ADMIN", null, null, "Admin Staff");

        // Student sends message
        service.send(1L, "ALL_STUDENTS_ADMIN", "Question for Admin");
        // Admin sends message
        service.send(99L, "ALL_STUDENTS_ADMIN", "Answer from Admin");

        // Admin loads room: sees both messages
        DiscussionService.RoomView adminView = service.load(99L, "ALL_STUDENTS_ADMIN");
        assertEquals(2, adminView.messages().size());
        assertEquals("Question for Admin", adminView.messages().get(0).message());
        assertEquals("Answer from Admin", adminView.messages().get(1).message());

        // Student loads room: sees both messages
        DiscussionService.RoomView studentView = service.load(1L, "ALL_STUDENTS_ADMIN");
        assertEquals(2, studentView.messages().size());
        assertEquals("Question for Admin", studentView.messages().get(0).message());
        assertEquals("Answer from Admin", studentView.messages().get(1).message());
    }

    @Test
    void newStudentWithNullSemesterAndSectionCanAccessAllStudentsAdminAndAll() throws Exception {
        dao.registerUser(10L, "STUDENT", null, null, "Fresh Student");

        // Can access All Students
        DiscussionService.RoomView allView = service.load(10L, "ALL");
        assertTrue(allView.available());

        // Can access All Students - Admin
        DiscussionService.RoomView adminView = service.load(10L, "ALL_STUDENTS_ADMIN");
        assertTrue(adminView.available());

        // Denied semester and section
        DiscussionService.RoomView semesterView = service.load(10L, "SEMESTER");
        assertFalse(semesterView.available());
        DiscussionService.RoomView sectionView = service.load(10L, "SECTION");
        assertFalse(sectionView.available());

        // Denied CR rooms
        assertThrows(SecurityException.class, () -> service.load(10L, "CR_ADMIN"));
        assertThrows(SecurityException.class, () -> service.load(10L, "CR_ALL"));
        assertThrows(SecurityException.class, () -> service.load(10L, "CR_SEMESTER"));
    }

    @Test
    void crCannotAccessAllStudentsAdmin() {
        dao.registerUser(5L, "CR", 3, "B", "CR Bob");
        assertThrows(SecurityException.class, () -> service.load(5L, "ALL_STUDENTS_ADMIN"));
        assertThrows(SecurityException.class, () -> service.send(5L, "ALL_STUDENTS_ADMIN", "CR trying to join"));
    }

    // ==========================================
    // CR - ADMIN TESTS
    // ==========================================

    @Test
    void crCanEnterAndSendToCrAdmin() throws Exception {
        dao.registerUser(5L, "CR", 3, "B", "CR Bob");
        DiscussionService.RoomView view = service.load(5L, "CR_ADMIN");
        assertTrue(view.available());
        assertEquals(DiscussionScope.CR_ADMIN, view.scope());

        DiscussionService.OperationResult result = service.send(5L, "CR_ADMIN", "Message from CR to Admin");
        assertTrue(result.successful());
        assertEquals(1, dao.storedMessages.size());
        assertEquals("Message from CR to Admin", dao.storedMessages.get(0).message());
    }

    @Test
    void adminCanEnterAndSendToCrAdmin() throws Exception {
        dao.registerUser(99L, "ADMIN", null, null, "Admin User");
        DiscussionService.RoomView view = service.load(99L, "CR_ADMIN");
        assertTrue(view.available());
        assertEquals(DiscussionScope.CR_ADMIN, view.scope());

        DiscussionService.OperationResult result = service.send(99L, "CR_ADMIN", "Admin reply to CRs");
        assertTrue(result.successful());
        assertEquals(1, dao.storedMessages.size());
        assertEquals("Admin reply to CRs", dao.storedMessages.get(0).message());
    }

    @Test
    void crAndAdminCanReadEachOtherInCrAdmin() throws Exception {
        dao.registerUser(5L, "CR", 4, "A", "CR Charlie");
        dao.registerUser(99L, "ADMIN", null, null, "Admin Staff");

        service.send(5L, "CR_ADMIN", "CR inquiry");
        service.send(99L, "CR_ADMIN", "Admin response");

        // CR loads messages
        DiscussionService.RoomView crView = service.load(5L, "CR_ADMIN");
        assertEquals(2, crView.messages().size());
        assertEquals("CR inquiry", crView.messages().get(0).message());
        assertEquals("Admin response", crView.messages().get(1).message());

        // Admin loads messages
        DiscussionService.RoomView adminView = service.load(99L, "CR_ADMIN");
        assertEquals(2, adminView.messages().size());
        assertEquals("CR inquiry", adminView.messages().get(0).message());
        assertEquals("Admin response", adminView.messages().get(1).message());
    }

    @Test
    void normalStudentIsDeniedCrAdminServerSide() {
        dao.registerUser(1L, "STUDENT", 4, "A", "Normal Student");
        // Read denied
        assertThrows(SecurityException.class, () -> service.load(1L, "CR_ADMIN"));
        // Direct Send API denied
        assertThrows(SecurityException.class, () -> service.send(1L, "CR_ADMIN", "Student attempting direct send"));
    }

    // ==========================================
    // SENDER VALIDATION & MESSAGE CONTRACT
    // ==========================================

    @Test
    void blankMessageIsRejected() throws Exception {
        dao.registerUser(1L, "STUDENT", 2, "A", "Student A");
        DiscussionService.OperationResult result = service.send(1L, "ALL_STUDENTS_ADMIN", "   ");
        assertFalse(result.successful());
        assertEquals(0, dao.storedMessages.size());
    }

    @Test
    void invalidScopeFallsBackToSectionAndRejectsUnauthorizedRole() {
        dao.registerUser(99L, "ADMIN", null, null, "Admin User");
        // Invalid scope string falls back to SECTION, which Admin is forbidden from
        assertThrows(SecurityException.class, () -> service.load(99L, "NON_EXISTENT_SCOPE"));
    }

    // ==========================================
    // REGRESSION: EXISTING STUDENT SCOPES
    // ==========================================

    @Test
    void existingStudentScopesRemainUnchanged() throws Exception {
        dao.registerUser(2L, "STUDENT", 3, "A", "Student Two");

        // Section discussion
        DiscussionService.RoomView section = service.load(2L, "SECTION");
        assertTrue(section.available());
        assertEquals(DiscussionScope.SECTION, section.scope());

        // Semester discussion
        DiscussionService.RoomView semester = service.load(2L, "SEMESTER");
        assertTrue(semester.available());
        assertEquals(DiscussionScope.SEMESTER, semester.scope());

        // All Students discussion
        DiscussionService.RoomView all = service.load(2L, "ALL");
        assertTrue(all.available());
        assertEquals(DiscussionScope.ALL, all.scope());
    }

    // ==========================================
    // REGRESSION: EXISTING CR SCOPES
    // ==========================================

    @Test
    void existingCrScopesRemainUnchanged() throws Exception {
        dao.registerUser(3L, "CR", 3, "A", "CR Three");

        assertTrue(service.load(3L, "SECTION").available());
        assertTrue(service.load(3L, "SEMESTER").available());
        assertTrue(service.load(3L, "ALL").available());
        assertTrue(service.load(3L, "CR_SEMESTER").available());
        assertTrue(service.load(3L, "CR_ALL").available());
        assertTrue(service.load(3L, "CR_ADMIN").available());
    }

    // ==========================================
    // MOCK DAO IMPLEMENTATION
    // ==========================================

    private static class MockDiscussionDAO extends DiscussionDAO {
        private final Map<Long, AcademicProfile> users = new HashMap<>();
        private final Map<Long, String> userNames = new HashMap<>();
        private final List<StoredMessage> storedMessages = new ArrayList<>();
        private final AtomicLong messageIdGen = new AtomicLong(1);

        record StoredMessage(long messageId, long senderId, DiscussionScope scope, String message, LocalDateTime createdAt) {}

        void registerUser(long userId, String role, Integer semester, String section, String fullName) {
            users.put(userId, new AcademicProfile(role, 1L, semester, section));
            userNames.put(userId, fullName);
        }

        @Override
        public AcademicProfile findAcademicProfile(long userId) {
            return users.getOrDefault(userId, new AcademicProfile(null, null, null, null));
        }

        @Override
        public List<DiscussionMessage> findRecent(DiscussionTarget target, int limit) {
            List<DiscussionMessage> list = new ArrayList<>();
            for (StoredMessage msg : storedMessages) {
                if (msg.scope() == target.scope()) {
                    AcademicProfile profile = users.get(msg.senderId());
                    list.add(new DiscussionMessage(
                            msg.messageId(),
                            msg.senderId(),
                            userNames.getOrDefault(msg.senderId(), "User"),
                            profile != null ? profile.role() : "STUDENT",
                            profile != null ? profile.semester() : null,
                            profile != null ? profile.sectionName() : null,
                            msg.message(),
                            msg.createdAt(),
                            null,
                            null
                    ));
                }
            }
            return list;
        }

        @Override
        public long insert(java.sql.Connection connection, DiscussionTarget target, String message) {
            long id = messageIdGen.getAndIncrement();
            storedMessages.add(new StoredMessage(id, target.authorId(), target.scope(), message, LocalDateTime.now()));
            return id;
        }
    }
}
