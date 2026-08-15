package com.studenthub.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DiscussionValidationTest {
    @Test void emptyMessageIsRejected() { assertNotNull(DiscussionValidation.validate("")); }
    @Test void whitespaceOnlyMessageIsRejected() { assertNotNull(DiscussionValidation.validate("  \r\n ")); }
    @Test void oversizedMessageIsRejected() {
        assertNotNull(DiscussionValidation.validate("x".repeat(DiscussionValidation.MAX_MESSAGE_LENGTH + 1)));
    }
    @Test void unicodeAndEmojiAreAcceptedAndTrimmed() {
        String input = "  မင်္ဂလာပါ 👋  ";
        assertNull(DiscussionValidation.validate(input));
        assertEquals("မင်္ဂလာပါ 👋", DiscussionValidation.normalize(input));
    }
}
