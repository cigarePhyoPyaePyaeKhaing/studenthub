package com.studenthub.util;

import com.studenthub.model.Role;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AdminValidationTest {
    @Test void emptySearchBecomesNormalListing() { assertNull(AdminValidation.normalizeSearch("  ")); }
    @Test void searchIsTrimmed() { assertEquals("phyo",AdminValidation.normalizeSearch(" phyo ")); }
    @Test void oversizedSearchRejected() { assertTrue(AdminValidation.searchTooLong("x".repeat(101))); }
    @Test void validPageAccepted() { assertEquals(3,AdminValidation.page("3")); }
    @Test void malformedPageFallsBack() { assertEquals(1,AdminValidation.page("abc")); }
    @Test void negativeAndZeroPagesFallBack() { assertEquals(1,AdminValidation.page("-3"));assertEquals(1,AdminValidation.page("0")); }
    @Test void extremePageFallsBack() { assertEquals(1,AdminValidation.page("1000001")); }
    @Test void paginationCountUsesTwentyRows() { assertEquals(3,AdminValidation.totalPages(41)); }
    @Test void allowlistedRolesParse() { assertEquals(Role.STUDENT,AdminValidation.role("student"));assertEquals(Role.CR,AdminValidation.role("CR"));assertEquals(Role.ADMIN,AdminValidation.role("ADMIN")); }
    @Test void arbitraryRoleRejected() { assertNull(AdminValidation.role("SUPERADMIN")); }
}
