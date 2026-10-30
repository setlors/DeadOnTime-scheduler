package core;

import model.TimeBlock;
import model.UserSettings;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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
                    LocalDateTime newStart = slot.start().isAfter(workdayStart) ? slot.start() : workdayStart;
                    LocalDateTime newEnd = slot.end().isBefore(workdayEnd) ? slot.end() : workdayEnd;
                    busyToday.add(new TimeBlock(newStart, newEnd));
                }
            }
            List<TimeBlock> sorted = new ArrayList<TimeBlock>(busyToday);
            sorted.sort(Comparator.comparing(TimeBlock::start));

            LocalDateTime pointer = workdayStart;
            for (TimeBlock slot : sorted) {
                if (slot.start().isAfter(pointer)) {
                    result.add(new TimeBlock(pointer, slot.start()));
                }
                if(slot.end().isAfter(pointer)) {
                    pointer = slot.end();
                }
                if(slot.end().isBefore(workdayEnd)) {
                    result.add(new TimeBlock(slot.end(), pointer));
                }
            }
        }
        return result;
    }
}
