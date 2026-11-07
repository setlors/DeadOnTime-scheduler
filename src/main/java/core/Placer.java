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
            List<ScheduledBlock> pieces = new ArrayList<>();
            boolean placed = false;
            if(task.splittable()) {
                Duration remaining = task.estimate();
                for (int i = 0; i < trial.size() && !remaining.isZero(); i++) {
                    TimeBlock window = trial.get(i);
                    LocalDateTime usableEnd = window.end().isBefore(task.deadline()) ? window.end() : task.deadline();
                    Duration room = Duration.between(window.start(), usableEnd);
                    if (room.isZero() || room.isNegative()) {
                        continue;
                    }
                    Duration chunk = room.compareTo(remaining) < 0 ? room : remaining;
                    LocalDateTime chunkEnd = window.start().plus(chunk);
                    pieces.add(new ScheduledBlock(task, new TimeBlock(window.start(), chunkEnd)));
                    trial.set(i, new TimeBlock(chunkEnd, window.end()));
                    remaining = remaining.minus(chunk);
                }

                placed = remaining.isZero();
            }
            else {
            for(int i = 0; i < trial.size(); i++) {
                TimeBlock block = trial.get(i);
                LocalDateTime usableEnd = block.end().isBefore(task.deadline()) ? block.end() : task.deadline();
                Duration length = Duration.between(block.start(), usableEnd);
                if (length.compareTo(task.estimate()) >= 0) {
                    LocalDateTime blockEnd = block.start().plus(task.estimate());
                    TimeBlock block1 = new TimeBlock(block.start(), blockEnd);
                    pieces.add(new ScheduledBlock(task, block1));
                    trial.set(i, new TimeBlock(blockEnd, block.end()));
                    placed = true;
                    break;
                }
            }
            }
            if(placed){
                windows = trial;
                schedule.getScheduledBlocks().addAll(pieces);
            }
            else {
                schedule.getUnscheduledTasks().add(task);
            }
        }
        return schedule;
    }
}
