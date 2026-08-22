package com.studenthub.util;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class AttachmentAuthorizationTest {
    private static final String SAFE = "123e4567-e89b-12d3-a456-426614174000.pdf";
    @Test void authenticatedUserCanRequestGeneratedName(){assertTrue(AttachmentAuthorization.canServe(true, SAFE));}
    @Test void loggedOutAndTraversalRequestsAreRejected(){assertFalse(AttachmentAuthorization.canServe(false, SAFE));assertFalse(AttachmentAuthorization.canServe(true,"../secret.pdf"));}
}
