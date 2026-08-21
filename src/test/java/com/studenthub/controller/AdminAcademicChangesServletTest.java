package com.studenthub.controller;

import com.studenthub.dao.AcademicChangeDAO;
import com.studenthub.dao.UserDAO;
import com.studenthub.model.Role;
import com.studenthub.util.CsrfToken;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class AdminAcademicChangesServletTest {
    private Map<String, Object> sessionAttributes;
    private Map<String, Object> requestAttributes;
    private Map<String, String> requestParameters;
    private String redirectedUrl;
    private int responseStatus;
    private String forwardedPath;

    @BeforeEach
    void setUp() {
        sessionAttributes = new HashMap<>();
        requestAttributes = new HashMap<>();
        requestParameters = new HashMap<>();
        redirectedUrl = null;
        responseStatus = 200;
        forwardedPath = null;
    }

    @Test
    void nonAdminCannotAccessAcademicChanges() throws Exception {
        AcademicChangeDAO mockDao = new AcademicChangeDAO();
        UserDAO mockUserDao = new UserDAO() {
            @Override
            public Optional<Role> findVerifiedRoleById(long userId) {
                return Optional.of(Role.STUDENT);
            }
        };

        AdminAcademicChangesServlet servlet = new AdminAcademicChangesServlet(mockDao, mockUserDao);

        // Student role
        sessionAttributes.put("userId", 2L);
        sessionAttributes.put("role", "STUDENT");

        HttpServletRequest request = createMockRequest();
        HttpServletResponse response = createMockResponse();

        servlet.doGet(request, response);

        assertEquals(HttpServletResponse.SC_FORBIDDEN, responseStatus);
    }

    @Test
    void adminCanApprovePendingRequest() throws Exception {
        AtomicReference<Long> reviewedId = new AtomicReference<>();
        AtomicReference<Long> reviewerAdminId = new AtomicReference<>();
        AtomicBoolean wasApproved = new AtomicBoolean(false);
        AtomicReference<String> adminNote = new AtomicReference<>();

        AcademicChangeDAO mockDao = new AcademicChangeDAO() {
            @Override
            public boolean review(long requestId, long adminId, boolean approve, String note) {
                reviewedId.set(requestId);
                reviewerAdminId.set(adminId);
                wasApproved.set(approve);
                adminNote.set(note);
                return true;
            }
        };

        UserDAO mockUserDao = new UserDAO() {
            @Override
            public Optional<Role> findVerifiedRoleById(long userId) {
                return Optional.of(Role.ADMIN);
            }
        };

        AdminAcademicChangesServlet servlet = new AdminAcademicChangesServlet(mockDao, mockUserDao);

        sessionAttributes.put("userId", 1L); // Admin user
        sessionAttributes.put("role", "ADMIN");
        sessionAttributes.put(CsrfToken.SESSION_KEY, "admin-csrf");
        requestParameters.put("csrfToken", "admin-csrf");
        requestParameters.put("id", "12");
        requestParameters.put("decision", "approve");
        requestParameters.put("adminNote", "Approved schedule change");

        HttpServletRequest request = createMockRequest();
        HttpServletResponse response = createMockResponse();

        servlet.doPost(request, response);

        assertEquals("/context/admin/academic-changes", redirectedUrl);
        assertEquals(12L, reviewedId.get());
        assertEquals(1L, reviewerAdminId.get());
        assertTrue(wasApproved.get());
        assertEquals("Approved schedule change", adminNote.get());
        assertEquals("Academic change request approved.", sessionAttributes.get("flash"));
    }

    @Test
    void adminCanRejectPendingRequest() throws Exception {
        AtomicReference<Long> reviewedId = new AtomicReference<>();
        AtomicReference<Long> reviewerAdminId = new AtomicReference<>();
        AtomicBoolean wasApproved = new AtomicBoolean(true);
        AtomicReference<String> adminNote = new AtomicReference<>();

        AcademicChangeDAO mockDao = new AcademicChangeDAO() {
            @Override
            public boolean review(long requestId, long adminId, boolean approve, String note) {
                reviewedId.set(requestId);
                reviewerAdminId.set(adminId);
                wasApproved.set(approve);
                adminNote.set(note);
                return true;
            }
        };

        UserDAO mockUserDao = new UserDAO() {
            @Override
            public Optional<Role> findVerifiedRoleById(long userId) {
                return Optional.of(Role.ADMIN);
            }
        };

        AdminAcademicChangesServlet servlet = new AdminAcademicChangesServlet(mockDao, mockUserDao);

        sessionAttributes.put("userId", 1L); // Admin user
        sessionAttributes.put("role", "ADMIN");
        sessionAttributes.put(CsrfToken.SESSION_KEY, "admin-csrf");
        requestParameters.put("csrfToken", "admin-csrf");
        requestParameters.put("id", "15");
        requestParameters.put("decision", "reject");
        requestParameters.put("adminNote", "Class full");

        HttpServletRequest request = createMockRequest();
        HttpServletResponse response = createMockResponse();

        servlet.doPost(request, response);

        assertEquals("/context/admin/academic-changes", redirectedUrl);
        assertEquals(15L, reviewedId.get());
        assertEquals(1L, reviewerAdminId.get());
        assertFalse(wasApproved.get());
        assertEquals("Class full", adminNote.get());
        assertEquals("Academic change request rejected.", sessionAttributes.get("flash"));
    }

    private HttpServletRequest createMockRequest() {
        HttpSession session = (HttpSession) Proxy.newProxyInstance(
                HttpSession.class.getClassLoader(),
                new Class[]{HttpSession.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getAttribute" -> sessionAttributes.get(args[0]);
                    case "setAttribute" -> {
                        sessionAttributes.put((String) args[0], args[1]);
                        yield null;
                    }
                    case "removeAttribute" -> {
                        sessionAttributes.remove(args[0]);
                        yield null;
                    }
                    default -> null;
                });

        RequestDispatcher dispatcher = (RequestDispatcher) Proxy.newProxyInstance(
                RequestDispatcher.class.getClassLoader(),
                new Class[]{RequestDispatcher.class},
                (proxy, method, args) -> {
                    if ("forward".equals(method.getName())) {
                        return null;
                    }
                    return null;
                });

        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class[]{HttpServletRequest.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getSession" -> {
                        boolean create = args == null || args.length == 0 || Boolean.TRUE.equals(args[0]);
                        yield (create || !sessionAttributes.isEmpty()) ? session : null;
                    }
                    case "getParameter" -> requestParameters.get(args[0]);
                    case "getAttribute" -> requestAttributes.get(args[0]);
                    case "setAttribute" -> {
                        requestAttributes.put((String) args[0], args[1]);
                        yield null;
                    }
                    case "getContextPath" -> "/context";
                    case "getRequestDispatcher" -> {
                        forwardedPath = (String) args[0];
                        yield dispatcher;
                    }
                    default -> null;
                });
    }

    private HttpServletResponse createMockResponse() {
        return (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class[]{HttpServletResponse.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "sendRedirect" -> {
                        redirectedUrl = (String) args[0];
                        yield null;
                    }
                    case "sendError" -> {
                        responseStatus = (Integer) args[0];
                        yield null;
                    }
                    case "setStatus" -> {
                        responseStatus = (Integer) args[0];
                        yield null;
                    }
                    default -> null;
                });
    }
}