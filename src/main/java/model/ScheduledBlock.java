package model;

public record ScheduledBlock (
        Task task,
        TimeBlock block
) {}
