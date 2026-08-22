package com.studenthub.util;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class Localization {
    private static final Locale MYANMAR = Locale.forLanguageTag("my");
    private Localization() {}
    public static String message(String language, String key) {
        Locale locale = "my".equalsIgnoreCase(language) ? MYANMAR : Locale.ENGLISH;
        try { return ResourceBundle.getBundle("messages", locale).getString(key); }
        catch (MissingResourceException ignored) {
            try { return ResourceBundle.getBundle("messages", Locale.ENGLISH).getString(key); }
            catch (MissingResourceException missing) { return key; }
        }
    }
}
