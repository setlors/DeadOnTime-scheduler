package model;

import java.time.Duration;
import java.time.LocalDateTime;

public record Task(
        String id,
        String title,
        Duration estimate,
        LocalDateTime deadline,
        int priority,
        boolean splittable,
        Duration minChunk
) {
    // 1-3, 1 -> more important
    public int weight() { return 4 - priority; }
}