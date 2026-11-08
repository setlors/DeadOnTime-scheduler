package algorithm;

import model.Schedule;
import model.Task;
import model.UserSettings;

import java.time.LocalDate;
import java.util.List;

public interface Scheduler {
    Schedule build(List<Task> tasks, UserSettings userSettings, LocalDate from, int days);
}
