package model;

import java.util.ArrayList;
import java.util.List;

public class Schedule {
    private final List<ScheduledBlock> scheduledBlocks = new ArrayList<>();
    private final List<Task> unscheduledTasks = new ArrayList<>();
    private double score;

    public List<Task> getUnscheduledTasks() {
        return unscheduledTasks;
    }

    public List<ScheduledBlock> getScheduledBlocks() {
        return scheduledBlocks;
    }

    public double getScore() {return score;}
    public void setScore(double score) {this.score = score;}
}
