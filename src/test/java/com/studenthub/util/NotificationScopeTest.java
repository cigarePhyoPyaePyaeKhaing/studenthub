package com.studenthub.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NotificationScopeTest {
    @Test void allIsVisibleWithoutAcademicProfile(){assertTrue(NotificationScope.isVisible(null,7,"ALL",null,null,null,null));}
    @Test void semesterRequiresExactDatabaseScope(){assertTrue(NotificationScope.isVisible(null,7,"SEMESTER",3,null,3,"A"));assertFalse(NotificationScope.isVisible(null,7,"SEMESTER",3,null,4,"A"));}
    @Test void sectionRequiresSemesterAndSection(){assertTrue(NotificationScope.isVisible(null,7,"SECTION",3,"B",3,"B"));assertFalse(NotificationScope.isVisible(null,7,"SECTION",3,"B",3,"A"));}
    @Test void directNotificationCannotBeReadByAnotherUser(){assertFalse(NotificationScope.isVisible(8L,7,"ALL",null,null,null,null));assertTrue(NotificationScope.isVisible(7L,7,"ALL",null,null,null,null));}
    @Test void nullScopesNeverMatchRestrictedNotification(){assertFalse(NotificationScope.isVisible(null,7,"SEMESTER",3,null,null,null));assertFalse(NotificationScope.isVisible(null,7,"SECTION",3,"A",3,null));}
}
