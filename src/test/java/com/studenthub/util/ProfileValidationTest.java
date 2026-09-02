package com.studenthub.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProfileValidationTest {
    @Test void validFullNameIsAccepted() { assertTrue(valid("Mya Mya", "4", "B").valid()); }
    @Test void emptyFullNameIsRejected() { assertFalse(valid("", "4", "B").valid()); }
    @Test void whitespaceFullNameIsRejected() { assertFalse(valid("   ", "4", "B").valid()); }
    @Test void oversizedFullNameIsRejected() { assertFalse(valid("x".repeat(101), "4", "B").valid()); }
    @Test void nameIsTrimmed() { assertEquals("Mya Mya", valid("  Mya Mya  ", "4", "B").update().fullName()); }
    @Test void nullSemesterIsAccepted() { assertTrue(valid("Mya Mya", "", "").valid()); }
    @Test void validSemesterRangeIsAccepted() {
        assertTrue(valid("Mya Mya", "1", "A").valid());
        assertTrue(valid("Mya Mya", "10", "ES").valid());
    }
    @Test void semesterBelowRangeIsRejected() { assertFalse(valid("Mya Mya", "0", "A").valid()); }
    @Test void semesterAboveRangeIsRejected() { assertFalse(valid("Mya Mya", "11", "A").valid()); }
    @Test void nonNumericSemesterIsRejected() { assertFalse(valid("Mya Mya", "four", "A").valid()); }
    @Test void validSectionIsAcceptedAndTrimmed() {
        assertEquals("B", valid("Mya Mya", "4", "  b  ").update().sectionName());
    }
    @Test void nullSectionIsAccepted() { assertNull(valid("Mya Mya", "4", "").update().sectionName()); }
    @Test void oversizedSectionIsRejected() { assertFalse(valid("Mya Mya", "4", "A".repeat(21)).valid()); }
    @Test void unsafeSectionCharactersAreRejected() { assertFalse(valid("Mya Mya", "4", "<script>").valid()); }
    @Test void sectionWithoutSemesterIsRejected() { assertFalse(valid("Mya Mya", "", "B").valid()); }

    private ProfileValidation.Result valid(String name, String semester, String section) {
        return ProfileValidation.validate(name, semester, section);
    }
}
