package com.studenthub.util;
import com.studenthub.model.DiscussionScope;import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class DiscussionAcademicEligibilityTest{
 @Test void academicRoomsRequireOnlyTheirOwnScopeFields(){assertNull(DiscussionAccess.denialReason(DiscussionScope.SEMESTER,4,null));assertNotNull(DiscussionAccess.denialReason(DiscussionScope.SECTION,null,"SE"));assertNotNull(DiscussionAccess.denialReason(DiscussionScope.SECTION,4," "));assertNull(DiscussionAccess.denialReason(DiscussionScope.SEMESTER,4,"SE"));assertNull(DiscussionAccess.denialReason(DiscussionScope.SECTION,4,"SE"));}
 @Test void allStudentsRoomRemainsAvailableWithoutAcademicFields(){assertNull(DiscussionAccess.denialReason(DiscussionScope.ALL,null,null));}
}
