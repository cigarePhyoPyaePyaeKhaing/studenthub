package com.studenthub.controller;

import com.studenthub.dao.UserDAO;
import com.studenthub.model.ProfileUpdate;
import com.studenthub.model.Role;
import com.studenthub.model.UserProfile;
import com.studenthub.service.ProfileService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ProfileServletTest {
    private Map<String, Object> sessionAttributes;
    private Map<String, Object> requestAttributes;
    private Map<String, String> requestParameters;
    private String redirectedUrl;
    private int responseStatus;
    private String forwardedPath;
    private String loggedMessage;
    private Throwable loggedException;
    private boolean sessionInvalidated;

    @BeforeEach
    void setUp() {
        sessionAttributes = new HashMap<>();
        requestAttributes = new HashMap<>();
        requestParameters = new HashMap<>();
        redirectedUrl = null;
        responseStatus = 200;
        forwardedPath = null;
        loggedMessage = null;
        loggedException = null;
        sessionInvalidated = false;
    }

    @Test
    void unauthenticatedAccessToProfileIsRedirectedToLogin() throws Exception {
        ProfileServlet servlet = createServlet(new FakeProfileDAO(null));
        HttpServletRequest request = createRequest("GET", false);
        HttpServletResponse response = createResponse();

        servlet.doGet(request, response);

        assertEquals("/context/login", redirectedUrl);
        assertNull(forwardedPath);
        assertNull(requestAttributes.get("profile"));
    }

    @Test
    void unauthenticatedPostToProfileIsRedirectedToLogin() throws Exception {
        ProfileServlet servlet = createServlet(new FakeProfileDAO(null));
        HttpServletRequest request = createRequest("POST", false);
        HttpServletResponse response = createResponse();

        servlet.doPost(request, response);

        assertEquals("/context/login", redirectedUrl);
    }

    @Test
    void authenticatedUserLoadsProfileSuccessfully() throws Exception {
        UserProfile mockProfile = new UserProfile(42L, "UIT-0042", "Mg Mg", "mgmg@uit.edu",
                Role.STUDENT, true, 4, "A", 1L, "University of Information Technology", "UIT", true, true);
        FakeProfileDAO dao = new FakeProfileDAO(mockProfile);
        ProfileServlet servlet = createServlet(dao);

        sessionAttributes.put("userId", 42L);
        HttpServletRequest request = createRequest("GET", true);
        HttpServletResponse response = createResponse();

        servlet.doGet(request, response);

        assertEquals("/WEB-INF/views/profile.jsp", forwardedPath);
        UserProfile loadedProfile = (UserProfile) requestAttributes.get("profile");
        assertNotNull(loadedProfile);
        assertEquals(42L, loadedProfile.getUserId());
        assertEquals("UIT-0042", loadedProfile.getStudentId());
        assertEquals("Mg Mg", loadedProfile.getFullName());
        assertEquals("mgmg@uit.edu", loadedProfile.getEmail());
        assertEquals(Role.STUDENT, loadedProfile.getRole());
        assertTrue(loadedProfile.isEmailVerified());
        assertEquals(4, loadedProfile.getSemester());
        assertEquals("A", loadedProfile.getSectionName());
        assertEquals("University of Information Technology", loadedProfile.getUniversityName());
        assertEquals("UIT", loadedProfile.getUniversityShortName());
        assertTrue(loadedProfile.isUniversityLocked());
        assertTrue(loadedProfile.isAcademicInfoLocked());
        assertNull(requestAttributes.get("error"));
    }

    @Test
    void authenticatedUserLoadsAnotherUsersPublicProfileSuccessfully() throws Exception {
        UserProfile publicProfile = new UserProfile(77L, "UIT-0077", "Public Student", "private@example.com",
                Role.STUDENT, true, 4, "B", 1L, "University of Information Technology", "UIT", true, true);
        ProfileServlet servlet = createServlet(new FakeProfileDAO(publicProfile));
        sessionAttributes.put("userId", 42L);
        requestParameters.put("userId", "77");

        servlet.doGet(createRequest("GET", true), createResponse());

        assertEquals(200, responseStatus);
        assertEquals("/WEB-INF/views/profile.jsp", forwardedPath);
        assertSame(publicProfile, requestAttributes.get("profile"));
        assertEquals(true, requestAttributes.get("publicProfile"));
        assertEquals(false, requestAttributes.get("editing"));
    }

    @Test
    void invalidPublicProfileIdReturnsNotFoundWithoutInvalidatingSession() throws Exception {
        ProfileServlet servlet = createServlet(new FakeProfileDAO(null));
        sessionAttributes.put("userId", 42L);
        requestParameters.put("userId", "not-a-number");

        servlet.doGet(createRequest("GET", true), createResponse());

        assertEquals(HttpServletResponse.SC_NOT_FOUND, responseStatus);
        assertFalse(sessionInvalidated);
        assertNull(forwardedPath);
    }

    @Test
    void missingOptionalAcademicInformationDoesNotCrashPage() throws Exception {
        UserProfile unassignedProfile = new UserProfile(99L, null, "New Student", "new@uit.edu",
                Role.STUDENT, false, null, null, null, null, null, false, false);
        FakeProfileDAO dao = new FakeProfileDAO(unassignedProfile);
        ProfileServlet servlet = createServlet(dao);

        sessionAttributes.put("userId", 99L);
        HttpServletRequest request = createRequest("GET", true);
        HttpServletResponse response = createResponse();

        servlet.doGet(request, response);

        assertEquals("/WEB-INF/views/profile.jsp", forwardedPath);
        UserProfile loadedProfile = (UserProfile) requestAttributes.get("profile");
        assertNotNull(loadedProfile);
        assertNull(loadedProfile.getStudentId());
        assertNull(loadedProfile.getSemester());
        assertNull(loadedProfile.getSectionName());
        assertNull(loadedProfile.getUniversityName());
        assertNull(loadedProfile.getUniversityShortName());
        assertFalse(loadedProfile.isAcademicInfoLocked());
        assertFalse(loadedProfile.isUniversityLocked());
        assertEquals("N", loadedProfile.getInitial());
    }

    @Test
    void nonexistentUserInvalidatesSessionAndRedirectsToLogin() throws Exception {
        FakeProfileDAO dao = new FakeProfileDAO(null);
        ProfileServlet servlet = createServlet(dao);

        sessionAttributes.put("userId", 999L);
        HttpServletRequest request = createRequest("GET", true);
        HttpServletResponse response = createResponse();

        servlet.doGet(request, response);

        assertTrue(sessionInvalidated);
        assertEquals("/context/login", redirectedUrl);
        assertNull(forwardedPath);
    }

    @Test
    void databaseFailureIsHandledSafelyWithDiagnosticLogging() throws Exception {
        FakeProfileDAO dao = new FakeProfileDAO(null);
        dao.failWithSqlException = true;
        ProfileServlet servlet = createServlet(dao);

        sessionAttributes.put("userId", 42L);
        HttpServletRequest request = createRequest("GET", true);
        HttpServletResponse response = createResponse();

        servlet.doGet(request, response);

        assertEquals("/WEB-INF/views/profile.jsp", forwardedPath);
        assertEquals("Your profile is temporarily unavailable.", requestAttributes.get("error"));
        assertNull(requestAttributes.get("profile"));
        assertNotNull(loggedMessage);
        assertTrue(loggedMessage.contains("Profile load failed"));
        assertTrue(loggedMessage.contains("SQLState="));
        assertNotNull(loggedException);
    }

    @Test
    void updateWithoutCsrfFailsWithForbidden() throws Exception {
        ProfileServlet servlet = createServlet(new FakeProfileDAO(null));
        sessionAttributes.put("userId", 42L);
        sessionAttributes.put("csrfToken", "valid-token");
        requestParameters.put("csrfToken", "invalid-token");

        HttpServletRequest request = createRequest("POST", true);
        HttpServletResponse response = createResponse();

        servlet.doPost(request, response);

        assertEquals(403, responseStatus);
    }

    @Test
    void successfulProfileUpdateRefreshesSession() throws Exception {
        UserProfile initial = new UserProfile(42L, "UIT-0042", "Mg Mg", "mgmg@uit.edu",
                Role.STUDENT, true, null, null, 1L, "UIT", "UIT", true, false);
        UserProfile updated = new UserProfile(42L, "UIT-0042", "Mg Mg Updated", "mgmg@uit.edu",
                Role.STUDENT, true, 2, "B", 1L, "UIT", "UIT", true, true);

        FakeProfileDAO dao = new FakeProfileDAO(initial);
        dao.updatedProfile = updated;
        ProfileServlet servlet = createServlet(dao);

        sessionAttributes.put("userId", 42L);
        sessionAttributes.put("csrfToken", "token123");
        requestParameters.put("csrfToken", "token123");
        requestParameters.put("fullName", "Mg Mg Updated");
        requestParameters.put("semester", "2");
        requestParameters.put("sectionName", "B");

        HttpServletRequest request = createRequest("POST", true);
        HttpServletResponse response = createResponse();

        servlet.doPost(request, response);

        assertEquals("/context/profile", redirectedUrl);
        assertEquals("Mg Mg Updated", sessionAttributes.get("fullName"));
        assertEquals(2, sessionAttributes.get("semester"));
        assertEquals("B", sessionAttributes.get("sectionName"));
        assertEquals("Profile updated successfully.", sessionAttributes.get("flash"));
    }

    @Test
    void databaseFailureOnUpdateSetsFlashErrorAndRedirectsToEdit() throws Exception {
        UserProfile initial = new UserProfile(42L, "UIT-0042", "Mg Mg", "mgmg@uit.edu",
                Role.STUDENT, true, null, null, 1L, "UIT", "UIT", true, false);
        FakeProfileDAO dao = new FakeProfileDAO(initial);
        dao.failOnUpdate = true;
        ProfileServlet servlet = createServlet(dao);

        sessionAttributes.put("userId", 42L);
        sessionAttributes.put("csrfToken", "token123");
        requestParameters.put("csrfToken", "token123");
        requestParameters.put("fullName", "Mg Mg New");
        requestParameters.put("semester", "2");
        requestParameters.put("sectionName", "B");

        HttpServletRequest request = createRequest("POST", true);
        HttpServletResponse response = createResponse();

        servlet.doPost(request, response);

        assertEquals("/context/profile?edit=true", redirectedUrl);
        assertEquals("Your profile could not be updated right now.", sessionAttributes.get("flashError"));
        assertNotNull(loggedMessage);
        assertTrue(loggedMessage.contains("Profile update failed"));
    }

    private ProfileServlet createServlet(UserDAO dao) throws Exception {
        ProfileService service = new ProfileService(dao);
        ProfileServlet servlet = new ProfileServlet(service);

        ServletContext servletContext = (ServletContext) Proxy.newProxyInstance(
                ServletContext.class.getClassLoader(),
                new Class[]{ServletContext.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "log" -> {
                        if (args.length >= 1) loggedMessage = (String) args[0];
                        if (args.length >= 2 && args[1] instanceof Throwable t) loggedException = t;
                        yield null;
                    }
                    case "getContextPath" -> "/context";
                    default -> primitiveDefault(method.getReturnType());
                });

        ServletConfig servletConfig = (ServletConfig) Proxy.newProxyInstance(
                ServletConfig.class.getClassLoader(),
                new Class[]{ServletConfig.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getServletContext" -> servletContext;
                    case "getServletName" -> "ProfileServlet";
                    default -> primitiveDefault(method.getReturnType());
                });

        servlet.init(servletConfig);
        return servlet;
    }

    private HttpServletRequest createRequest(String method, boolean hasSession) {
        HttpSession session = hasSession ? (HttpSession) Proxy.newProxyInstance(
                HttpSession.class.getClassLoader(),
                new Class[]{HttpSession.class},
                (proxy, m, args) -> switch (m.getName()) {
                    case "getAttribute" -> sessionAttributes.get((String) args[0]);
                    case "setAttribute" -> {
                        sessionAttributes.put((String) args[0], args[1]);
                        yield null;
                    }
                    case "removeAttribute" -> {
                        sessionAttributes.remove((String) args[0]);
                        yield null;
                    }
                    case "invalidate" -> {
                        sessionInvalidated = true;
                        sessionAttributes.clear();
                        yield null;
                    }
                    default -> primitiveDefault(m.getReturnType());
                }) : null;

        RequestDispatcher dispatcher = (RequestDispatcher) Proxy.newProxyInstance(
                RequestDispatcher.class.getClassLoader(),
                new Class[]{RequestDispatcher.class},
                (proxy, m, args) -> null);

        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class[]{HttpServletRequest.class},
                (proxy, m, args) -> switch (m.getName()) {
                    case "getMethod" -> method;
                    case "getContextPath" -> "/context";
                    case "getRequestURI" -> "/context/profile";
                    case "getParameter" -> requestParameters.get((String) args[0]);
                    case "getAttribute" -> requestAttributes.get((String) args[0]);
                    case "setAttribute" -> {
                        requestAttributes.put((String) args[0], args[1]);
                        yield null;
                    }
                    case "removeAttribute" -> {
                        requestAttributes.remove((String) args[0]);
                        yield null;
                    }
                    case "getSession" -> {
                        if (args == null || args.length == 0) yield session;
                        boolean create = (Boolean) args[0];
                        yield create ? session : session;
                    }
                    case "getRequestDispatcher" -> {
                        forwardedPath = (String) args[0];
                        yield dispatcher;
                    }
                    case "setCharacterEncoding" -> null;
                    default -> primitiveDefault(m.getReturnType());
                });
    }

    private HttpServletResponse createResponse() {
        return (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class[]{HttpServletResponse.class},
                (proxy, m, args) -> switch (m.getName()) {
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
                    case "getWriter" -> new PrintWriter(new StringWriter());
                    default -> primitiveDefault(m.getReturnType());
                });
    }

    private static Object primitiveDefault(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0F;
        if (type == double.class) return 0D;
        if (type == char.class) return '\0';
        return null;
    }

    private static class FakeProfileDAO extends UserDAO {
        private UserProfile profile;
        UserProfile updatedProfile;
        boolean failWithSqlException = false;
        boolean failOnUpdate = false;

        FakeProfileDAO(UserProfile profile) {
            this.profile = profile;
        }

        @Override
        public Optional<UserProfile> findProfileById(long userId) throws SQLException {
            if (failWithSqlException) {
                throw new SQLException("Database query execution error", "42S02", 1146);
            }
            if (profile != null && profile.getUserId() == userId) {
                return Optional.of(profile);
            }
            return Optional.empty();
        }

        @Override
        public int updateProfile(long userId, ProfileUpdate update) throws SQLException {
            if (failOnUpdate) {
                throw new SQLException("Update lock acquisition timed out", "HY000", 1205);
            }
            if (updatedProfile != null) {
                this.profile = updatedProfile;
            }
            return 1;
        }

        @Override
        public int updateFullName(long userId, String fullName) throws SQLException {
            if (failOnUpdate) {
                throw new SQLException("Update lock acquisition timed out", "HY000", 1205);
            }
            if (updatedProfile != null) {
                this.profile = updatedProfile;
            }
            return 1;
        }
    }
}
