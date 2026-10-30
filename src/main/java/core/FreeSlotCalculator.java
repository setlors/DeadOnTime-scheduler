package core;

import model.TimeBlock;
import model.UserSettings;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FreeSlotCalculator {
    public List<TimeBlock> calculate(UserSettings userSettings, LocalDate start, int days) {
        List<TimeBlock> result = new ArrayList<>();
        for(int i = 0; i < days; i++) {
            LocalDate currDay = start.plusDays(i);
            LocalDateTime workdayStart = currDay.atTime(userSettings.workStart());
            LocalDateTime workdayEnd = currDay.atTime(userSettings.workEnd());

            List<TimeBlock> busyToday = new ArrayList<TimeBlock>();
            for(TimeBlock slot : userSettings.busySlots()){
                if (slot.start().isBefore(workdayEnd) && slot.end().isAfter(workdayStart)) {
                    LocalDateTime newStart = slot.start().isAfter(workdayStart) ? workdayStart : slot.start();
                    LocalDateTime newEnd = slot.end().isBefore(workdayEnd) ? workdayEnd : slot.end();
                    busyToday.add(new TimeBlock(newStart, newEnd));
                }
            }
        }
        return result;
    }
}
