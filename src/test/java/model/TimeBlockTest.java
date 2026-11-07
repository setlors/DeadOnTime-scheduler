package model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TimeBlockTest {

    private final LocalDateTime nine = LocalDateTime.of(2026, 11, 2, 9, 0);
    private final LocalDateTime eleven = LocalDateTime.of(2026, 11, 2, 11, 0);

    @Test
    void endBeforeStart_isRejected() {
        assertThrows(IllegalArgumentException.class, () -> new TimeBlock(eleven, nine));
    }

    @Test
    void length_isTimeBetweenStartAndEnd() {
        assertEquals(Duration.ofHours(2), new TimeBlock(nine, eleven).length());
    }
}