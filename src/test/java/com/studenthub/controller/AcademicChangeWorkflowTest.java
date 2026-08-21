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

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class AcademicChangeWorkflowTest {
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
    void studentCanSubmitValidAcademicChangeRequest() throws Exception {
        AtomicReference<Long> capturedUserId = new AtomicReference<>();
        AtomicReference<Integer> capturedSemester = new AtomicReference<>();
        AtomicReference<String> capturedSection = new AtomicReference<>();
        AtomicReference<String> capturedReason = new AtomicReference<>();

        AcademicChangeDAO mockDao = new AcademicChangeDAO() {
            @Override
            public void create(long userId, int semester, String section, String reason) {
                capturedUserId.set(userId);
                capturedSemester.set(semester);
                capturedSection.set(section);
                capturedReason.set(reason);
            }
        };

        AcademicChangeRequestServlet servlet = new AcademicChangeRequestServlet(mockDao);

        sessionAttributes.put("userId", 55L);
        sessionAttributes.put(CsrfToken.SESSION_KEY, "valid-csrf");
        requestParameters.put("csrfToken", "valid-csrf");
        requestParameters.put("semester", "5");
        requestParameters.put("sectionName", "C");
        requestParameters.put("reason", "Transferred to new major and section");

        HttpServletRequest request = createMockRequest();
        HttpServletResponse response = createMockResponse();

        servlet.doPost(request, response);

        assertEquals("/context/profile", redirectedUrl);
        assertEquals(55L, capturedUserId.get());
        assertEquals(5, capturedSemester.get());
        assertEquals("C", capturedSection.get());
        assertEquals("Transferred to new major and section", capturedReason.get());
        assertEquals("Academic change request submitted for administrator review.", sessionAttributes.get("flash"));
    }

    @Test
    void studentCannotSubmitDuplicatePendingRequest() throws Exception {
        AcademicChangeDAO mockDao = new AcademicChangeDAO() {
            @Override
            public void create(long userId, int semester, String section, String reason) {
                throw new IllegalStateException("You already have a pending academic change request.");
            }
        };

        AcademicChangeRequestServlet servlet = new AcademicChangeRequestServlet(mockDao);

        sessionAttributes.put("userId", 55L);
        sessionAttributes.put(CsrfToken.SESSION_KEY, "valid-csrf");
        requestParameters.put("csrfToken", "valid-csrf");
        requestParameters.put("semester", "5");
        requestParameters.put("sectionName", "C");
        requestParameters.put("reason", "Transferred to new major and section");

        HttpServletRequest request = createMockRequest();
        HttpServletResponse response = createMockResponse();

        servlet.doPost(request, response);

        assertEquals("/context/profile", redirectedUrl);
        assertEquals("You already have a pending academic change request.", sessionAttributes.get("flashError"));
    }

    @Test
    void studentRequestFailsWhenCsrfInvalid() throws Exception {
        AcademicChangeDAO mockDao = new AcademicChangeDAO();
        AcademicChangeRequestServlet servlet = new AcademicChangeRequestServlet(mockDao);

        sessionAttributes.put("userId", 55L);
        sessionAttributes.put(CsrfToken.SESSION_KEY, "valid-csrf");
        requestParameters.put("csrfToken", "invalid-csrf");
        requestParameters.put("semester", "5");
        requestParameters.put("sectionName", "C");
        requestParameters.put("reason", "Transferred to new major and section");

        HttpServletRequest request = createMockRequest();
        HttpServletResponse response = createMockResponse();

        servlet.doPost(request, response);

        assertEquals(HttpServletResponse.SC_FORBIDDEN, responseStatus);
        assertNull(redirectedUrl);
    }

    @Test
    void unauthenticatedUserCannotSubmitAcademicChange() throws Exception {
        AcademicChangeDAO mockDao = new AcademicChangeDAO();
        AcademicChangeRequestServlet servlet = new AcademicChangeRequestServlet(mockDao);

        // No session attributes (unauthenticated)
        HttpServletRequest request = createMockRequest();
        HttpServletResponse response = createMockResponse();

        servlet.doPost(request, response);

        assertEquals("/context/login", redirectedUrl);
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
                        boolean create = args.length == 0 || Boolean.TRUE.equals(args[0]);
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