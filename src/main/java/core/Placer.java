package core;

import model.Schedule;
import model.ScheduledBlock;
import model.Task;
import model.TimeBlock;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Placer {
    public Schedule place(List<Task> orderedTasks, List<TimeBlock> freeSlots) {
        Schedule schedule = new Schedule();
        List<TimeBlock> windows = new ArrayList<>(freeSlots);
        for (Task task : orderedTasks) {
            List<TimeBlock> trial = new ArrayList<>(windows);
            boolean placed = false;
            for(int i = 0; i < trial.size(); i++) {
                TimeBlock block = trial.get(i);
                Duration length = block.length();
                if(length.compareTo(task.estimate()) >= 0){
                    LocalDateTime blockEnd = block.start().plus(task.estimate());
                    TimeBlock block1 = new TimeBlock(block.start(), blockEnd);
                    ScheduledBlock scheduledBlock = new ScheduledBlock(task, block1);
                }

            }
        }
        return schedule;
    }
}
