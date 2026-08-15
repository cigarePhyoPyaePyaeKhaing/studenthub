package com.studenthub.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CommentValidationTest {
    @Test void emptyCommentRejected() { assertNotNull(CommentValidation.validate("")); }
    @Test void whitespaceCommentRejected() { assertNotNull(CommentValidation.validate(" \r\n ")); }
    @Test void oversizedCommentRejected() { assertNotNull(CommentValidation.validate("x".repeat(5001))); }
    @Test void validCommentAcceptedAndTrimmed() {
        assertNull(CommentValidation.validate("  Helpful comment  "));
        assertEquals("Helpful comment", CommentValidation.normalize("  Helpful comment  "));
    }
    @Test void unicodeMyanmarAndEmojiAccepted() {
        assertNull(CommentValidation.validate("ကျေးဇူးတင်ပါတယ် 😊"));
    }
}
