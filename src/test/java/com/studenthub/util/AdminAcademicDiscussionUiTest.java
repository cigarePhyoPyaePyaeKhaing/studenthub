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
        assertTrue(jsp.contains("class=\"admin-room-selector\""));
        assertTrue(jsp.contains("items=\"${moderationRooms}\""));
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

    @Test void moderationRoomRecordExposesPropertiesRequiredByJakartaEl() throws Exception {
        var properties = Arrays.stream(Introspector.getBeanInfo(com.studenthub.dao.DiscussionDAO.RoomOption.class)
                .getPropertyDescriptors()).map(descriptor -> descriptor.getName()).toList();
        assertTrue(properties.contains("roomId"));
        assertTrue(properties.contains("roomName"));
        assertTrue(properties.contains("scope"));
    }
}
