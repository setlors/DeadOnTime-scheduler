package model;

import java.time.Duration;
import java.time.LocalDateTime;

public record TimeBlock(LocalDateTime start, LocalDateTime end) {

    public TimeBlock {
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("end must not be before start");
        }
    }

    public Duration length() {
        return Duration.between(start, end);
    }
}