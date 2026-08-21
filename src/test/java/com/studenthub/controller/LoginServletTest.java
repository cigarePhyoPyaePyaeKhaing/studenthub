package com.studenthub.controller;

import com.studenthub.dao.UserDAO;
import com.studenthub.model.Role;
import com.studenthub.model.User;
import com.studenthub.service.AuthService;
import com.studenthub.util.CsrfToken;
import com.studenthub.util.PasswordUtil;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class LoginServletTest {
    private Map<String, Object> sessionAttributes;
    private Map<String, Object> requestAttributes;
    private Map<String, String> requestParameters;
    private String redirectedUrl;
    private int responseStatus;
    private String forwardedPath;
    private boolean sessionInvalidated;

    @BeforeEach
    void setUp() {
        sessionAttributes = new HashMap<>();
        requestAttributes = new HashMap<>();
        requestParameters = new HashMap<>();
        redirectedUrl = null;
        responseStatus = 200;
        forwardedPath = null;
        sessionInvalidated = false;
    }

    @Test
    void unverifiedUserCredentialsRedirectsToVerifyEmailWithPendingUserId() throws Exception {
        User unverifiedUser = new User(10L, "TNT-0010", "Unverified Student", "unverified@uit.edu",
                PasswordUtil.hash("Secret123"), Role.STUDENT, false, null);

        UserDAO mockDao = new UserDAO() {
            @Override
            public Optional<User> findByLogin(String login) {
                if ("TNT-0010".equals(login) || "unverified@uit.edu".equals(login)) {
                    return Optional.of(unverifiedUser);
                }
                return Optional.empty();
            }
        };

        AuthService authService = new AuthService();
        injectField(authService, "userDAO", mockDao);
        LoginServlet servlet = new LoginServlet();
        injectField(servlet, "authService", authService);

        sessionAttributes.put(CsrfToken.SESSION_KEY, "valid-token");
        requestParameters.put("csrfToken", "valid-token");
        requestParameters.put("login", "TNT-0010");
        requestParameters.put("password", "Secret123");

        HttpServletRequest request = createMockRequest();
        HttpServletResponse response = createMockResponse();

        servlet.doPost(request, response);

        assertEquals("/context/verify-email", redirectedUrl);
        assertEquals(10L, sessionAttributes.get("pendingVerificationUserId"));
        assertNull(sessionAttributes.get("userId"));
        assertNull(sessionAttributes.get("role"));
    }

    @Test
    void verifiedUserCredentialsRedirectsToHomeWithAuthenticatedSession() throws Exception {
        User verifiedUser = new User(20L, "TNT-0020", "Verified Student", "verified@uit.edu",
                PasswordUtil.hash("Secret123"), Role.STUDENT, true, null);

        UserDAO mockDao = new UserDAO() {
            @Override
            public Optional<User> findByLogin(String login) {
                if ("TNT-0020".equals(login) || "verified@uit.edu".equals(login)) {
                    return Optional.of(verifiedUser);
                }
                return Optional.empty();
            }
        };

        AuthService authService = new AuthService();
        injectField(authService, "userDAO", mockDao);
        LoginServlet servlet = new LoginServlet();
        injectField(servlet, "authService", authService);

        sessionAttributes.put(CsrfToken.SESSION_KEY, "valid-token");
        requestParameters.put("csrfToken", "valid-token");
        requestParameters.put("login", "TNT-0020");
        requestParameters.put("password", "Secret123");

        HttpServletRequest request = createMockRequest();
        HttpServletResponse response = createMockResponse();

        servlet.doPost(request, response);

        assertEquals("/context/home", redirectedUrl);
        assertEquals(20L, sessionAttributes.get("userId"));
        assertEquals("TNT-0020", sessionAttributes.get("studentId"));
        assertEquals("Verified Student", sessionAttributes.get("fullName"));
        assertEquals("STUDENT", sessionAttributes.get("role"));
        assertNull(sessionAttributes.get("pendingVerificationUserId"));
    }

    @Test
    void invalidCredentialsSetsErrorMessageAndForwards() throws Exception {
        UserDAO mockDao = new UserDAO() {
            @Override
            public Optional<User> findByLogin(String login) {
                return Optional.empty();
            }
        };

        AuthService authService = new AuthService();
        injectField(authService, "userDAO", mockDao);
        LoginServlet servlet = new LoginServlet();
        injectField(servlet, "authService", authService);

        sessionAttributes.put(CsrfToken.SESSION_KEY, "valid-token");
        requestParameters.put("csrfToken", "valid-token");
        requestParameters.put("login", "TNT-9999");
        requestParameters.put("password", "WrongPassword1");

        HttpServletRequest request = createMockRequest();
        HttpServletResponse response = createMockResponse();

        servlet.doPost(request, response);

        assertNull(redirectedUrl);
        assertEquals("/WEB-INF/views/auth/login.jsp", forwardedPath);
        assertEquals("Invalid student ID/email or password.", requestAttributes.get("error"));
    }

    @Test
    void invalidCsrfTokenReturnsForbidden() throws Exception {
        LoginServlet servlet = new LoginServlet();

        sessionAttributes.put(CsrfToken.SESSION_KEY, "valid-token");
        requestParameters.put("csrfToken", "wrong-token");
        requestParameters.put("login", "TNT-0010");
        requestParameters.put("password", "Secret123");

        HttpServletRequest request = createMockRequest();
        HttpServletResponse response = createMockResponse();

        servlet.doPost(request, response);

        assertEquals(HttpServletResponse.SC_FORBIDDEN, responseStatus);
        assertNull(redirectedUrl);
    }

    private void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
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
                    case "invalidate" -> {
                        sessionInvalidated = true;
                        sessionAttributes.clear();
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