package model;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

public record UserSettings(
        LocalTime workStart,
        LocalTime workEnd,
        Duration workDuration,
        Duration breakAfter,
        Duration breakLength,
        List<TimeBlock> busySlots
) {
}
