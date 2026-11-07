package core;

import model.TimeBlock;
import model.UserSettings;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FreeSlotCalculatorTest {

    private static final LocalDate MONDAY = LocalDate.of(2026, 11, 2);

    private final FreeSlotCalculator calculator = new FreeSlotCalculator();

    // ---------- helpers ----------

    /** A TimeBlock on Monday: at(12, 13) means 12:00-13:00 */
    private TimeBlock at(int fromHour, int toHour) {
        return new TimeBlock(MONDAY.atTime(fromHour, 0), MONDAY.atTime(toHour, 0));
    }

    /** Workday 9:00-18:00 with the given busy slots */
    private UserSettings settingsWith(List<TimeBlock> busy) {
        return new UserSettings(
                LocalTime.of(9, 0), LocalTime.of(18, 0),
                Duration.ofHours(6), Duration.ofMinutes(90), Duration.ofMinutes(15),
                busy);
    }

    /** Checks that a window is on (Monday + dayOffset), from fromHour to toHour */
    private void assertWindow(TimeBlock window, int dayOffset, int fromHour, int toHour) {
        LocalDate day = MONDAY.plusDays(dayOffset);
        assertEquals(day.atTime(fromHour, 0), window.start());
        assertEquals(day.atTime(toHour, 0), window.end());
    }

    // ---------- tests ----------

    @Test
    void noBusySlots_wholeWorkdayIsFree() {
        List<TimeBlock> free = calculator.calculate(settingsWith(List.of()), MONDAY, 1);

        assertEquals(1, free.size());
        assertWindow(free.get(0), 0, 9, 18);
    }

    @Test
    void meetingInTheMiddle_splitsDayInTwo() {
        List<TimeBlock> free = calculator.calculate(settingsWith(List.of(at(12, 13))), MONDAY, 1);

        assertEquals(2, free.size());
        assertWindow(free.get(0), 0, 9, 12);
        assertWindow(free.get(1), 0, 13, 18);
    }

    @Test
    void meetingStartsBeforeWork_isTrimmed() {
        List<TimeBlock> free = calculator.calculate(settingsWith(List.of(at(8, 10))), MONDAY, 1);

        assertEquals(1, free.size());
        assertWindow(free.get(0), 0, 10, 18);
    }

    @Test
    void meetingEndsAfterWork_isTrimmed() {
        List<TimeBlock> free = calculator.calculate(settingsWith(List.of(at(17, 19))), MONDAY, 1);

        assertEquals(1, free.size());
        assertWindow(free.get(0), 0, 9, 17);
    }

    @Test
    void wholeDayBusy_noFreeWindows() {
        List<TimeBlock> free = calculator.calculate(settingsWith(List.of(at(9, 18))), MONDAY, 1);

        assertTrue(free.isEmpty());
    }

    @Test
    void overlappingMeetings_actAsOneBusyStretch() {
        List<TimeBlock> busy = List.of(at(12, 14), at(13, 15));

        List<TimeBlock> free = calculator.calculate(settingsWith(busy), MONDAY, 1);

        assertEquals(2, free.size());
        assertWindow(free.get(0), 0, 9, 12);
        assertWindow(free.get(1), 0, 15, 18);
    }

    @Test
    void backToBackMeetings_noZeroLengthWindowBetween() {
        List<TimeBlock> busy = List.of(at(12, 13), at(13, 14));

        List<TimeBlock> free = calculator.calculate(settingsWith(busy), MONDAY, 1);

        assertEquals(2, free.size());
        assertWindow(free.get(0), 0, 9, 12);
        assertWindow(free.get(1), 0, 14, 18);
    }

    @Test
    void meetingInsideAnotherMeeting_cursorDoesNotMoveBackwards() {
        List<TimeBlock> busy = List.of(at(12, 16), at(13, 14));

        List<TimeBlock> free = calculator.calculate(settingsWith(busy), MONDAY, 1);

        assertEquals(2, free.size());
        assertWindow(free.get(0), 0, 9, 12);
        assertWindow(free.get(1), 0, 16, 18);
    }

    @Test
    void busySlotsInWrongOrder_areSorted() {
        List<TimeBlock> busy = List.of(at(15, 16), at(11, 12));

        List<TimeBlock> free = calculator.calculate(settingsWith(busy), MONDAY, 1);

        assertEquals(3, free.size());
        assertWindow(free.get(0), 0, 9, 11);
        assertWindow(free.get(1), 0, 12, 15);
        assertWindow(free.get(2), 0, 16, 18);
    }

    @Test
    void severalDays_eachDayGetsItsOwnWindows() {
        List<TimeBlock> free = calculator.calculate(settingsWith(List.of(at(12, 13))), MONDAY, 3);

        assertEquals(4, free.size());
        assertWindow(free.get(0), 0, 9, 12);    // Monday morning
        assertWindow(free.get(1), 0, 13, 18);   // Monday afternoon
        assertWindow(free.get(2), 1, 9, 18);    // Tuesday
        assertWindow(free.get(3), 2, 9, 18);    // Wednesday
    }

    @Test
    void meetingSpanningTwoDays_affectsBothDays() {
        TimeBlock overnight = new TimeBlock(MONDAY.atTime(17, 0), MONDAY.plusDays(1).atTime(10, 0));

        List<TimeBlock> free = calculator.calculate(settingsWith(List.of(overnight)), MONDAY, 2);

        assertEquals(2, free.size());
        assertWindow(free.get(0), 0, 9, 17);    // Monday until the meeting starts
        assertWindow(free.get(1), 1, 10, 18);   // Tuesday after the meeting ends
    }

    @Test
    void busySlotOnAnotherDay_isIgnored() {
        TimeBlock tuesdayMeeting = new TimeBlock(MONDAY.plusDays(1).atTime(12, 0), MONDAY.plusDays(1).atTime(13, 0));

        List<TimeBlock> free = calculator.calculate(settingsWith(List.of(tuesdayMeeting)), MONDAY, 1);

        assertEquals(1, free.size());
        assertWindow(free.get(0), 0, 9, 18);
    }
}