package com.studenthub.controller;

import com.studenthub.dao.AcademicChangeDAO;
import com.studenthub.model.Role;
import com.studenthub.model.UserProfile;
import com.studenthub.service.ProfileService;
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

import static org.junit.jupiter.api.Assertions.*;

class ProfileServletResilienceTest {
    private Map<String, Object> sessionAttributes;
    private Map<String, Object> requestAttributes;
    private String forwardedPath;
    private int responseStatus;

    @BeforeEach
    void setUp() {
        sessionAttributes = new HashMap<>();
        requestAttributes = new HashMap<>();
        forwardedPath = null;
        responseStatus = 200;
    }

    @Test
    void profileRendersWhenNoAcademicRequestExists() throws Exception {
        UserProfile profile = new UserProfile(55L, "TNT-0055", "Test Student", "test55@uit.edu",
                Role.STUDENT, true, 4, "C");
        ProfileService mockProfileService = new ProfileService(null) {
            @Override
            public Optional<UserProfile> findOwnProfile(long userId) {
                return Optional.of(profile);
            }
        };

        AcademicChangeDAO mockDao = new AcademicChangeDAO() {
            @Override
            public Optional<Item> findPendingForUser(long userId) {
                return Optional.empty();
            }
        };

        ProfileServlet servlet = new ProfileServlet(mockProfileService, mockDao);
        sessionAttributes.put("userId", 55L);
        sessionAttributes.put("role", "STUDENT");

        HttpServletRequest request = createMockRequest();
        HttpServletResponse response = createMockResponse();

        servlet.doGet(request, response);

        assertEquals("/WEB-INF/views/profile.jsp", forwardedPath);
        assertEquals(profile, requestAttributes.get("profile"));
        assertNull(requestAttributes.get("pendingAcademicRequest"));
        assertNull(requestAttributes.get("error"));
    }

    @Test
    void profileRendersWhenPendingAcademicRequestExists() throws Exception {
        UserProfile profile = new UserProfile(55L, "TNT-0055", "Test Student", "test55@uit.edu",
                Role.STUDENT, true, 4, "C");
        ProfileService mockProfileService = new ProfileService(null) {
            @Override
            public Optional<UserProfile> findOwnProfile(long userId) {
                return Optional.of(profile);
            }
        };

        AcademicChangeDAO.Item pendingItem = new AcademicChangeDAO.Item(
                10L, 55L, "TNT-0055", "Test Student", "test55@uit.edu",
                4, "C", 5, "B", "Change major section", "PENDING", null,
                LocalDateTime.now(), null);

        AcademicChangeDAO mockDao = new AcademicChangeDAO() {
            @Override
            public Optional<Item> findPendingForUser(long userId) {
                return Optional.of(pendingItem);
            }
        };

        ProfileServlet servlet = new ProfileServlet(mockProfileService, mockDao);
        sessionAttributes.put("userId", 55L);
        sessionAttributes.put("role", "STUDENT");

        HttpServletRequest request = createMockRequest();
        HttpServletResponse response = createMockResponse();

        servlet.doGet(request, response);

        assertEquals("/WEB-INF/views/profile.jsp", forwardedPath);
        assertEquals(profile, requestAttributes.get("profile"));
        assertEquals(pendingItem, requestAttributes.get("pendingAcademicRequest"));
        assertNull(requestAttributes.get("error"));
    }

    @Test
    void profileDegradesGracefullyWhenAcademicRequestLookupThrowsSQLException() throws Exception {
        UserProfile profile = new UserProfile(55L, "TNT-0055", "Test Student", "test55@uit.edu",
                Role.STUDENT, true, 4, "C");
        ProfileService mockProfileService = new ProfileService(null) {
            @Override
            public Optional<UserProfile> findOwnProfile(long userId) {
                return Optional.of(profile);
            }
        };

        AcademicChangeDAO mockDao = new AcademicChangeDAO() {
            @Override
            public Optional<Item> findPendingForUser(long userId) throws SQLException {
                throw new SQLException("Table 'academic_change_requests' doesn't exist", "42S02", 1146);
            }
        };

        ProfileServlet servlet = new ProfileServlet(mockProfileService, mockDao);
        sessionAttributes.put("userId", 55L);
        sessionAttributes.put("role", "STUDENT");

        HttpServletRequest request = createMockRequest();
        HttpServletResponse response = createMockResponse();

        servlet.doGet(request, response);

        assertEquals("/WEB-INF/views/profile.jsp", forwardedPath);
        assertEquals(profile, requestAttributes.get("profile"));
        assertNull(requestAttributes.get("pendingAcademicRequest"));
        assertEquals(true, requestAttributes.get("academicRequestUnavailable"));
        assertNull(requestAttributes.get("error"));
    }

    @Test
    void profileDegradesGracefullyWhenAcademicRequestLookupThrowsRuntimeException() throws Exception {
        UserProfile profile = new UserProfile(55L, "TNT-0055", "Test Student", "test55@uit.edu",
                Role.STUDENT, true, 4, "C");
        ProfileService mockProfileService = new ProfileService(null) {
            @Override
            public Optional<UserProfile> findOwnProfile(long userId) {
                return Optional.of(profile);
            }
        };

        AcademicChangeDAO mockDao = new AcademicChangeDAO() {
            @Override
            public Optional<Item> findPendingForUser(long userId) {
                throw new RuntimeException("Unexpected transient error");
            }
        };

        ProfileServlet servlet = new ProfileServlet(mockProfileService, mockDao);
        sessionAttributes.put("userId", 55L);
        sessionAttributes.put("role", "STUDENT");

        HttpServletRequest request = createMockRequest();
        HttpServletResponse response = createMockResponse();

        servlet.doGet(request, response);

        assertEquals("/WEB-INF/views/profile.jsp", forwardedPath);
        assertEquals(profile, requestAttributes.get("profile"));
        assertNull(requestAttributes.get("pendingAcademicRequest"));
        assertEquals(true, requestAttributes.get("academicRequestUnavailable"));
        assertNull(requestAttributes.get("error"));
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
                    case "getSession" -> session;
                    case "getParameter" -> null;
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
                    case "sendRedirect" -> null;
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