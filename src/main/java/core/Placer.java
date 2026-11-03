package core;

import model.Schedule;
import model.ScheduledBlock;
import model.Task;
import model.TimeBlock;

import java.util.ArrayList;
import java.util.List;

public class Placer {
    public Schedule place(List<Task> orderedTasks, List<TimeBlock> freeSlots) {
        Schedule schedule = new Schedule();
        List<TimeBlock> windows = new ArrayList<>(freeSlots);
        for (Task task : orderedTasks) {
            List<TimeBlock> trial = new ArrayList<>(windows);
        }
        return schedule;
    }
}
