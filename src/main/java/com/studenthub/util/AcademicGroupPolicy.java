package com.studenthub.util;

import java.util.List;
import java.util.Locale;

public final class AcademicGroupPolicy {
    public record Option(int semester, String value) {
        public int getSemester() { return semester; }
        public String getValue() { return value; }
    }
    private static final List<String> FIVE_SECTIONS = List.of("A", "B", "C", "D", "E");
    private static final List<String> FOUR_SECTIONS = List.of("A", "B", "C", "D");
    private static final List<String> MAJORS = List.of("SE", "KE", "BIS", "HPC", "CN", "CSec", "ES");

    private AcademicGroupPolicy() {}

    public static List<String> optionsFor(int semester) {
        if (semester >= 1 && semester <= 7) return MAJORS;
        if (semester >= 8 && semester <= 10) return FIVE_SECTIONS;
        return List.of();
    }

    public static boolean isMajorSemester(int semester) {
        return semester >= 1 && semester <= 7;
    }

    public static String groupLabel(int semester) {
        return isMajorSemester(semester) ? "Major" : "Section";
    }

    public static String normalize(int semester, String value) {
        if (value == null || value.isBlank()) return null;
        String candidate = value.trim();
        String normalized = optionsFor(semester).stream()
                .filter(option -> option.equalsIgnoreCase(candidate))
                .findFirst().orElse(null);
        if (normalized != null) return normalized;
        List<String> historicalSections = semester == 3 || semester == 4 ? FOUR_SECTIONS : FIVE_SECTIONS;
        return semester >= 1 && semester <= 7 ? historicalSections.stream()
                .filter(option -> option.equalsIgnoreCase(candidate))
                .findFirst().orElse(null) : null;
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
