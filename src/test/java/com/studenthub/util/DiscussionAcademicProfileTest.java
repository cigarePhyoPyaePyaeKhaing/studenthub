package com.studenthub.util;
import com.studenthub.model.DiscussionScope;import org.junit.jupiter.api.Test;import static org.junit.jupiter.api.Assertions.*;
class DiscussionAcademicProfileTest{
 @Test void missingUniversityIsRejected(){assertNotNull(DiscussionAccess.denialReason(DiscussionScope.SECTION,null,2,"KE"));}
 @Test void missingSemesterIsRejected(){assertNotNull(DiscussionAccess.denialReason(DiscussionScope.SECTION,3L,null,"KE"));}
 @Test void semesterRoomDoesNotRequireSection(){assertNull(DiscussionAccess.denialReason(DiscussionScope.SEMESTER,3L,2,null));}
 @Test void completeProfileIsAccepted(){assertNull(DiscussionAccess.denialReason(DiscussionScope.SECTION,3L,2,"KE"));}
 @Test void targetCarriesTrustedUniversity(){var target=DiscussionTarget.fromAuthenticatedUser(8,DiscussionScope.SECTION,12L,3,"BIS");assertEquals(12L,target.universityId());assertEquals(3,target.semester());assertEquals("BIS",target.sectionName());}
}
