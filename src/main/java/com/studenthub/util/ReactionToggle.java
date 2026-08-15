package com.studenthub.util;

public final class ReactionToggle {
    private ReactionToggle() {}
    public static boolean nextState(boolean currentlyReacted) { return !currentlyReacted; }
}
