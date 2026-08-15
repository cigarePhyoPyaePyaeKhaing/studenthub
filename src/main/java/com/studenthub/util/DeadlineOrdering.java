package com.studenthub.util;

import com.studenthub.model.Deadline;
import java.util.Comparator;

public final class DeadlineOrdering {
    private DeadlineOrdering() {
    }
    public static Comparator<Deadline> upcoming() {
        return Comparator.comparing(Deadline::dueDate).thenComparingLong(Deadline::deadlineId);
    }
}
