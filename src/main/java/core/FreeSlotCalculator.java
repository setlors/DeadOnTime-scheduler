package core;

import model.TimeBlock;
import model.UserSettings;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class FreeSlotCalculator {
    public void calculate(UserSettings userSettings, LocalDate start, int days) {
        for(int i = 0; i < days; i++) {
            LocalDate currDay = start.plusDays(i);
            LocalDateTime workdayStart = currDay.atTime(userSettings.workStart());
            LocalDateTime workdayEnd = currDay.atTime(userSettings.workEnd());

            TimeBlock block = new TimeBlock(workdayStart, workdayEnd);

        }
    }
}
