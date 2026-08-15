package com.studenthub.util;

import com.studenthub.model.Deadline;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DeadlineOrderingTest {
    @Test void upcomingDeadlinesAreNearestFirst() {
        LocalDateTime now=LocalDateTime.now();
        Deadline later=new Deadline(2,null,null,"Later","Subject",now.plusDays(3),1,null,1,"CR",now);
        Deadline sooner=new Deadline(1,null,null,"Sooner","Subject",now.plusDays(1),1,null,1,"CR",now);
        List<Deadline> deadlines=new ArrayList<>(List.of(later,sooner));deadlines.sort(DeadlineOrdering.upcoming());
        assertEquals(List.of(sooner,later),deadlines);
    }
}
