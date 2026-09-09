package com.studenthub.util;

import java.util.List;

public final class AcademicGroupPolicy {
    public record Option(int semester, String value) {
        public int getSemester() { return semester; }
        public String getValue() { return value; }
    }
    private static final List<String> FIVE_SECTIONS = List.of("A", "B", "C", "D", "E");

    private AcademicGroupPolicy() {}

    public static List<String> optionsFor(int semester) {
        if (semester >= ProfileValidation.MIN_SEMESTER && semester <= ProfileValidation.MAX_SEMESTER) {
            return FIVE_SECTIONS;
        }
        return List.of();
    }

    public static String groupLabel(int semester) {
        return "Section";
    }

    public static String normalize(int semester, String value) {
        if (value == null || value.isBlank()) return null;
        String candidate = value.trim();
        return optionsFor(semester).stream()
                .filter(option -> option.equalsIgnoreCase(candidate))
                .findFirst().orElse(null);
    }

    public static boolean isValid(int semester, String value) {
        return normalize(semester, value) != null;
    }

    public static List<Option> allOptions() {
        return java.util.stream.IntStream.rangeClosed(ProfileValidation.MIN_SEMESTER, ProfileValidation.MAX_SEMESTER)
                .boxed().flatMap(semester -> optionsFor(semester).stream().map(value -> new Option(semester, value)))
                .toList();
    }
}
