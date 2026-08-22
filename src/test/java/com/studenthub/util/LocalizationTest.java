package com.studenthub.util;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class LocalizationTest {
    @Test void myanmarResourceLoads(){assertEquals("ပင်မ",Localization.message("my","nav.home"));}
    @Test void unknownLanguageFallsBackToEnglish(){assertEquals("Home",Localization.message("xx","nav.home"));}
    @Test void missingKeyFallsBackToKey(){assertEquals("missing.key",Localization.message("my","missing.key"));}
}
