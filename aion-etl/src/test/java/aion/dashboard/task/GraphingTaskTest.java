package aion.dashboard.task;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraphingTaskTest {
    private final ZoneId UTCZoneID = ZoneId.of("UTC");
    @Test
    void generatedThisHour() {


        ZonedDateTime timeNow = Instant.now().atZone(UTCZoneID);
        ZonedDateTime timeHourAgo = timeNow.minusHours(1);
        ZonedDateTime timeDayAgo = timeNow.minusDays(1);


        assertTrue(GraphingTask.generatedThisHour(timeNow));
        assertFalse(GraphingTask.generatedThisHour(timeHourAgo));
        assertFalse(GraphingTask.generatedThisHour(timeDayAgo));
    }
}