package com.studenthub.util;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class AdminProfileUiContractTest {
    @Test void adminAcademicControlsAreHiddenWhileStudentAndCrMarkupRemains() throws Exception {
        String jsp = Files.readString(Path.of("src/main/webapp/WEB-INF/views/profile.jsp"));
        assertTrue(jsp.contains("<c:if test=\"${profile.role ne 'ADMIN'}\"><c:choose>"));
        assertTrue(jsp.contains("<c:if test=\"${profile.role ne 'ADMIN'}\"><section class=\"profile-card\">"));
        assertTrue(jsp.contains("Request Academic Info Change"));
        assertTrue(jsp.contains("name=\"semester\""));
        assertTrue(jsp.contains("name=\"sectionName\""));
    }

    @Test void backendDoesNotLoadOrAcceptAcademicRequestsForAdmins() throws Exception {
        String profile = Files.readString(Path.of("src/main/java/com/studenthub/controller/ProfileServlet.java"));
        String request = Files.readString(Path.of("src/main/java/com/studenthub/controller/AcademicChangeRequestServlet.java"));
        String service = Files.readString(Path.of("src/main/java/com/studenthub/service/ProfileService.java"));
        assertTrue(profile.contains("profile.getRole() != com.studenthub.model.Role.ADMIN"));
        assertTrue(request.contains("currentProfile.get().getRole() == Role.ADMIN"));
        assertTrue(request.contains("SC_FORBIDDEN"));
        assertTrue(service.contains("currentProfile.getRole() == Role.ADMIN || currentProfile.academicInfoLocked()"));
    }

    @Test void ownAdminProfileUsesOnlyExistingAuthorizedAdminRoutes() throws Exception {
        String jsp = Files.readString(Path.of("src/main/webapp/WEB-INF/views/profile.jsp"));
        assertTrue(jsp.contains("${not publicProfile and profile.role eq 'ADMIN'}"));
        assertTrue(jsp.contains("<h2>Admin tools</h2>"));
        assertTrue(jsp.contains("${pageContext.request.contextPath}/admin/users"));
        assertTrue(jsp.contains("${pageContext.request.contextPath}/admin/academic-changes"));
        assertTrue(jsp.contains("${pageContext.request.contextPath}/admin"));
    }

    @Test void adminAccountIdAndRealActivityMetadataAreShownOnlyInAccountInformation() throws Exception {
        String jsp = Files.readString(Path.of("src/main/webapp/WEB-INF/views/profile.jsp"));
        String controller = Files.readString(Path.of("src/main/java/com/studenthub/controller/ProfileServlet.java"));
        String dao = Files.readString(Path.of("src/main/java/com/studenthub/dao/UserDAO.java"));
        String hero = jsp.substring(jsp.indexOf("<section class=\"profile-hero\">"),
                jsp.indexOf("</section>", jsp.indexOf("<section class=\"profile-hero\">")));
        assertTrue(!hero.contains("profile.studentId"));
        assertTrue(jsp.contains("${profile.role eq 'ADMIN' ? 'Admin ID' : 'Student ID'}"));
        assertTrue(jsp.contains("${profileDisplayId}"));
        assertTrue(controller.contains("profileService.findAdminDisplayNumber(profile.getUserId())"));
        assertTrue(dao.contains("a.role = 'ADMIN' AND a.user_id <= target.user_id"));
        assertTrue(jsp.contains("<dt>Last active</dt>"));
        assertTrue(jsp.contains("<dt>Joined</dt>"));
        assertTrue(jsp.contains("not empty joinedLabel"));
    }
}
