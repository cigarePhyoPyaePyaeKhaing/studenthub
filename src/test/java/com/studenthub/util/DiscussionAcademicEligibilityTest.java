package com.studenthub.util;
import com.studenthub.model.DiscussionScope;import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class DiscussionAcademicEligibilityTest{
 @Test void academicRoomsRequireOnlyTheirOwnScopeFields(){assertNull(DiscussionAccess.denialReason(DiscussionScope.SEMESTER,4,null));assertNotNull(DiscussionAccess.denialReason(DiscussionScope.SECTION,null,"A"));assertNotNull(DiscussionAccess.denialReason(DiscussionScope.SECTION,4," "));assertNull(DiscussionAccess.denialReason(DiscussionScope.SEMESTER,4,"A"));assertNull(DiscussionAccess.denialReason(DiscussionScope.SECTION,4,"A"));}
 @Test void allStudentsRoomRemainsAvailableWithoutAcademicFields(){assertNull(DiscussionAccess.denialReason(DiscussionScope.ALL,null,null));}
}
