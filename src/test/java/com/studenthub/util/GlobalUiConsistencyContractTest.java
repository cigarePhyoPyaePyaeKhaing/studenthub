package com.studenthub.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class GlobalUiConsistencyContractTest {
    private static String source(String path) throws Exception {
        return Files.readString(Path.of(path));
    }

    @Test void sharedLayoutTokensDriveDashboardSpacingAndCards() throws Exception {
        String main = source("src/main/webapp/assets/css/main.css");
        String refined = source("src/main/webapp/assets/css/dashboard-refined.css");

        assertTrue(main.contains("--layout-page-padding:20px"));
        assertTrue(main.contains("--layout-page-gap:20px"));
        assertTrue(main.contains("--layout-card-gap:16px"));
        assertTrue(main.contains("--layout-card-padding:20px"));
        assertTrue(main.contains("--layout-card-radius:24px"));
        assertTrue(refined.contains(".dashboard-shell{gap:var(--layout-page-gap);padding:var(--layout-page-padding)}"));
        assertTrue(refined.contains("border-radius:var(--layout-card-radius)"));
        assertTrue(refined.contains("padding:var(--layout-card-padding)"));
    }

    @Test void sameRowCardsStretchWithoutFixedHeightsAndEmptyStatesRemainBounded() throws Exception {
        String refined = source("src/main/webapp/assets/css/dashboard-refined.css");

        assertTrue(refined.contains(".profile-grid{align-items:stretch}"));
        assertTrue(refined.lastIndexOf(".profile-grid{align-items:stretch}")
                > refined.lastIndexOf(".profile-grid{align-items:start}"));
        assertTrue(refined.contains(".profile-grid>.profile-card{height:100%}"));
        assertTrue(refined.contains(".deadline-card-grid>.deadline-card{height:100%}"));
        assertTrue(refined.contains(".deadline-card-grid>.empty-state{grid-column:1/-1}"));
        assertTrue(refined.contains(".empty-state,.comments-empty{display:grid;min-height:180px"));
        assertFalse(refined.contains("height:500px"));
    }
}
