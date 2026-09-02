package com.studenthub.util;

import static org.junit.jupiter.api.Assertions.*;
import com.studenthub.model.DiscussionScope;
import java.nio.file.Files;
import java.nio.file.Path;
import java.beans.Introspector;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class AdminAcademicDiscussionUiTest {
    @Test void adminUsesModerationSelectorAndServerBackedMessageActions() throws Exception {
        String jsp = Files.readString(Path.of("src/main/webapp/WEB-INF/views/discussions/index.jsp"));
        assertTrue(jsp.contains("class=\"admin-discussion-controls\""));
        assertTrue(jsp.contains("class=\"admin-global-scopes\""));
        assertTrue(jsp.contains("items=\"${moderationSemesters}\""));
        assertTrue(jsp.contains("items=\"${moderationSections}\""));
        assertTrue(jsp.contains("name=\"moderationScope\""));
        assertTrue(jsp.contains("moderationScope=all_students"));
        assertTrue(jsp.contains("moderationScope=all_cr"));
        assertTrue(jsp.contains("name=\"sectionSemester\""));
        assertTrue(jsp.contains("name=\"sectionName\""));
        assertTrue(jsp.contains("data-section-semester"));
        assertTrue(jsp.contains("data-section-name"));
        assertFalse(jsp.contains("name=\"roomId\""));
        assertTrue(jsp.contains("Admin Moderation"));
        assertTrue(jsp.contains("admin-moderation-delete"));
        assertTrue(jsp.contains("/discussions/messages/delete"));
        assertFalse(jsp.contains("scope=ALL_STUDENTS_ADMIN\">All Students – Admin"));
        assertFalse(jsp.contains("scope=CR_ADMIN\">CR – Admin</a>"));
    }
    @Test void roleMatrixMatchesAcademicMembershipAndModerationPolicy() {
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.ALL, "STUDENT"));
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.SEMESTER, "STUDENT"));
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.SECTION, "STUDENT"));
        assertFalse(DiscussionAccess.roleMayAccess(DiscussionScope.CR_ALL, "STUDENT"));
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.ALL, "CR"));
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.CR_SEMESTER, "CR"));
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.CR_ALL, "CR"));
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.ALL, "ADMIN"));
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.SEMESTER, "ADMIN"));
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.SECTION, "ADMIN"));
        assertTrue(DiscussionAccess.roleMayAccess(DiscussionScope.CR_ALL, "ADMIN"));
        assertFalse(DiscussionAccess.roleMayAccess(DiscussionScope.ALL_STUDENTS_ADMIN, "ADMIN"));
        assertFalse(DiscussionAccess.roleMayAccess(DiscussionScope.CR_ADMIN, "ADMIN"));
    }

    @Test void moderationScopeRecordExposesPropertiesRequiredByJakartaEl() throws Exception {
        var properties = Arrays.stream(Introspector.getBeanInfo(com.studenthub.service.DiscussionService.ModerationScopeOption.class)
                .getPropertyDescriptors()).map(descriptor -> descriptor.getName()).toList();
        assertTrue(properties.contains("key"));
        assertTrue(properties.contains("group"));
        assertTrue(properties.contains("label"));
    }

    @Test void canonicalScopesDeduplicatePhysicalRoomsAndPreserveEmptyState() throws Exception {
        String jsp = Files.readString(Path.of("src/main/webapp/WEB-INF/views/discussions/index.jsp"));
        String dao = Files.readString(Path.of("src/main/java/com/studenthub/dao/DiscussionDAO.java"));
        String service = Files.readString(Path.of("src/main/java/com/studenthub/service/DiscussionService.java"));
        assertFalse(jsp.contains("StudentHub ·"));
        assertFalse(jsp.contains("UIT ·"));
        assertTrue(jsp.contains("No messages yet."));
        assertTrue(dao.contains("GROUP BY source.room_type, source.semester, source.section_name"));
        assertTrue(dao.contains("findRecentForModeration"));
        assertTrue(service.contains("\"all_students\""));
        assertTrue(service.contains("\"all_cr\""));
        assertTrue(service.contains("\"semester:\" + semester.getKey()"));
        assertTrue(service.contains("\"section:\" + room.semester() + \":\" + room.sectionName()"));
    }
}
