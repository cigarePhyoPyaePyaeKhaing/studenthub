package com.studenthub.controller;

import com.studenthub.dao.AcademicChangeDAO;
import com.studenthub.dao.NotificationDAO;
import com.studenthub.dao.UniversityDAO;
import com.studenthub.dao.UserDAO;
import com.studenthub.filter.AuthenticationFilter;
import com.studenthub.model.Role;
import com.studenthub.model.University;
import com.studenthub.model.UserProfile;
import com.studenthub.service.ProfileService;
import com.studenthub.util.CsrfToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class PerformanceOptimizationTest {

    private Map<String, Object> sessionAttributes;
    private Map<String, Object> requestAttributes;
    private Map<String, String> requestParameters;
    private String forwardedPath;

    @BeforeEach
    void setUp() {
        sessionAttributes = new HashMap<>();
        requestAttributes = new HashMap<>();
        requestParameters = new HashMap<>();
        forwardedPath = null;
    }

    @Test
    void profileServletDoesNotQueryAvailableUniversitiesInViewMode() throws Exception {
        AtomicInteger universityQueryCount = new AtomicInteger(0);

        UserProfile mockProfile = new UserProfile(42L, "UIT-0042", "Mg Mg", "mgmg@uit.edu",
                Role.STUDENT, true, 4, "A", 1L, "University of Information Technology", "UIT", true, true);

        UserDAO mockUserDAO = new UserDAO() {
            @Override
            public Optional<UserProfile> findProfileById(long userId) {
                return Optional.of(mockProfile);
            }
        };

        UniversityDAO mockUnivDAO = new UniversityDAO() {
            @Override
            public List<University> listApprovedUniversities() {
                universityQueryCount.incrementAndGet();
                return List.of();
            }
        };

        ProfileService profileService = new ProfileService(mockUserDAO, mockUnivDAO);
        ProfileServlet servlet = new ProfileServlet(profileService, new AcademicChangeDAO());

        sessionAttributes.put("userId", 42L);
        // view mode (no edit param)
        HttpServletRequest request = createMockRequest("/profile");
        HttpServletResponse response = createMockResponse();

        servlet.doGet(request, response);

        assertEquals(0, universityQueryCount.get(), "Available universities should not be queried in view mode");
        List<?> list = (List<?>) requestAttributes.get("availableUniversities");
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    void profileServletDoesNotQueryAvailableUniversitiesWhenLockedEvenIfEditMode() throws Exception {
        AtomicInteger universityQueryCount = new AtomicInteger(0);

        // Locked profile
        UserProfile lockedProfile = new UserProfile(42L, "UIT-0042", "Mg Mg", "mgmg@uit.edu",
                Role.STUDENT, true, 4, "A", 1L, "University of Information Technology", "UIT", true, true);

        UserDAO mockUserDAO = new UserDAO() {
            @Override
            public Optional<UserProfile> findProfileById(long userId) {
                return Optional.of(lockedProfile);
            }
        };

        UniversityDAO mockUnivDAO = new UniversityDAO() {
            @Override
            public List<University> listApprovedUniversities() {
                universityQueryCount.incrementAndGet();
                return List.of();
            }
        };

        ProfileService profileService = new ProfileService(mockUserDAO, mockUnivDAO);
        ProfileServlet servlet = new ProfileServlet(profileService, new AcademicChangeDAO());

        sessionAttributes.put("userId", 42L);
        requestParameters.put("edit", "true");
        HttpServletRequest request = createMockRequest("/profile");
        HttpServletResponse response = createMockResponse();

        servlet.doGet(request, response);

        assertEquals(0, universityQueryCount.get(), "Available universities should not be queried when university is locked");
    }

    @Test
    void profileServletQueriesAvailableUniversitiesOnlyWhenUnassignedAndEditMode() throws Exception {
        AtomicInteger universityQueryCount = new AtomicInteger(0);

        // Unassigned profile
        UserProfile unassignedProfile = new UserProfile(42L, "UIT-0042", "Mg Mg", "mgmg@uit.edu",
                Role.STUDENT, true, null, null, null, null, null, false, false);

        UserDAO mockUserDAO = new UserDAO() {
            @Override
            public Optional<UserProfile> findProfileById(long userId) {
                return Optional.of(unassignedProfile);
            }
        };

        UniversityDAO mockUnivDAO = new UniversityDAO() {
            @Override
            public List<University> listApprovedUniversities() {
                universityQueryCount.incrementAndGet();
                return List.of(new University(1L, "UIT", "UIT", "APPROVED"));
            }
        };

        ProfileService profileService = new ProfileService(mockUserDAO, mockUnivDAO);
        ProfileServlet servlet = new ProfileServlet(profileService, new AcademicChangeDAO());

        sessionAttributes.put("userId", 42L);
        requestParameters.put("edit", "true");
        HttpServletRequest request = createMockRequest("/profile");
        HttpServletResponse response = createMockResponse();

        servlet.doGet(request, response);

        assertEquals(1, universityQueryCount.get(), "Available universities should be queried when unassigned in edit mode");
        List<?> list = (List<?>) requestAttributes.get("availableUniversities");
        assertNotNull(list);
        assertEquals(1, list.size());
    }

    @Test
    void authenticationFilterUsesCachedUnreadCountWhenFresh() throws Exception {
        AtomicInteger countQueryCount = new AtomicInteger(0);

        NotificationDAO mockNotificationDAO = new NotificationDAO() {
            @Override
            public long countUnread(long userId) {
                countQueryCount.incrementAndGet();
                return 5L;
            }
        };

        AuthenticationFilter filter = new AuthenticationFilter();
        java.lang.reflect.Field daoField = AuthenticationFilter.class.getDeclaredField("notificationDAO");
        daoField.setAccessible(true);
        daoField.set(filter, mockNotificationDAO);

        sessionAttributes.put("userId", 42L);
        sessionAttributes.put("cachedUnreadCount", 3L);
        sessionAttributes.put("cachedUnreadTime", System.currentTimeMillis() - 5000L); // 5s ago (fresh)

        HttpServletRequest request = createMockRequest("/home");
        HttpServletResponse response = createMockResponse();
        FilterChain chain = (req, res) -> {};

        filter.doFilter(request, response, chain);

        assertEquals(0, countQueryCount.get(), "Should use cached unread count when fresh");
        assertEquals(3L, requestAttributes.get("unreadNotificationCount"));
    }

    @Test
    void authenticationFilterQueriesUnreadCountWhenCacheExpired() throws Exception {
        AtomicInteger countQueryCount = new AtomicInteger(0);

        NotificationDAO mockNotificationDAO = new NotificationDAO() {
            @Override
            public long countUnread(long userId) {
                countQueryCount.incrementAndGet();
                return 7L;
            }
        };

        AuthenticationFilter filter = new AuthenticationFilter();
        java.lang.reflect.Field daoField = AuthenticationFilter.class.getDeclaredField("notificationDAO");
        daoField.setAccessible(true);
        daoField.set(filter, mockNotificationDAO);

        sessionAttributes.put("userId", 42L);
        sessionAttributes.put("cachedUnreadCount", 3L);
        sessionAttributes.put("cachedUnreadTime", System.currentTimeMillis() - 40000L); // 40s ago (expired)

        HttpServletRequest request = createMockRequest("/home");
        HttpServletResponse response = createMockResponse();
        FilterChain chain = (req, res) -> {};

        filter.doFilter(request, response, chain);

        assertEquals(1, countQueryCount.get(), "Should query database when unread count cache is expired");
        assertEquals(7L, requestAttributes.get("unreadNotificationCount"));
        assertEquals(7L, sessionAttributes.get("cachedUnreadCount"));
    }

    private HttpServletRequest createMockRequest(String uri) {
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
                (proxy, method, args) -> null);

        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class[]{HttpServletRequest.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getSession" -> session;
                    case "getParameter" -> requestParameters.get(args[0]);
                    case "getAttribute" -> requestAttributes.get(args[0]);
                    case "setAttribute" -> {
                        requestAttributes.put((String) args[0], args[1]);
                        yield null;
                    }
                    case "getContextPath" -> "";
                    case "getRequestURI" -> uri;
                    case "getMethod" -> "GET";
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
                (proxy, method, args) -> null);
    }
}
