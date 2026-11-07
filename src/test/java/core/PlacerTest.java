package core;

import model.Schedule;
import model.ScheduledBlock;
import model.Task;
import model.TimeBlock;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlacerTest {

    private static final LocalDate MONDAY = LocalDate.of(2026, 11, 2);

    private final Placer placer = new Placer();

    // ---------- helpers ----------

    /** Monday at hour:minute */
    private LocalDateTime t(int hour, int minute) {
        return MONDAY.atTime(hour, minute);
    }

    /** Monday at hour:00 */
    private LocalDateTime t(int hour) {
        return t(hour, 0);
    }

    private TimeBlock at(int fromHour, int toHour) {
        return new TimeBlock(t(fromHour), t(toHour));
    }

    /** The usual free time: 9-12 and 13-18 */
    private List<TimeBlock> defaultWindows() {
        return List.of(at(9, 12), at(13, 18));
    }

    /** Task that can't be split, deadline Monday 23:00 */
    private Task whole(String title, int hours) {
        return wholeDue(title, hours, 23);
    }

    /** Task that can't be split, with a deadline at deadlineHour on Monday */
    private Task wholeDue(String title, int hours, int deadlineHour) {
        return new Task(title, title, Duration.ofHours(hours), t(deadlineHour), 3, false, Duration.ZERO);
    }

    /** Splittable task, deadline Monday 23:00 */
    private Task splittable(String title, Duration estimate, Duration minChunk) {
        return new Task(title, title, estimate, t(23), 3, true, minChunk);
    }

    private void assertBlock(ScheduledBlock block, String title, LocalDateTime start, LocalDateTime end) {
        assertEquals(title, block.task().title());
        assertEquals(start, block.block().start());
        assertEquals(end, block.block().end());
    }

    /** The first block of the task with this title */
    private ScheduledBlock blockOf(Schedule schedule, String title) {
        for (ScheduledBlock b : schedule.getScheduledBlocks()) {
            if (b.task().title().equals(title)) {
                return b;
            }
        }
        throw new AssertionError("No block found for task " + title);
    }

    // ---------- tests ----------

    @Test
    void noTasks_emptySchedule() {
        Schedule schedule = placer.place(List.of(), defaultWindows());

        assertTrue(schedule.getScheduledBlocks().isEmpty());
        assertTrue(schedule.getUnscheduledTasks().isEmpty());
    }

    @Test
    void oneTask_goesToStartOfFirstWindow() {
        Schedule schedule = placer.place(List.of(whole("A", 2)), defaultWindows());

        assertEquals(1, schedule.getScheduledBlocks().size());
        assertBlock(schedule.getScheduledBlocks().get(0), "A", t(9), t(11));
    }

    @Test
    void twoTasks_secondStartsWhereFirstEnds() {
        Schedule schedule = placer.place(List.of(whole("A", 2), whole("B", 1)), defaultWindows());

        assertEquals(2, schedule.getScheduledBlocks().size());
        assertBlock(schedule.getScheduledBlocks().get(0), "A", t(9), t(11));
        assertBlock(schedule.getScheduledBlocks().get(1), "B", t(11), t(12));
    }

    @Test
    void windowTooSmall_isSkipped() {
        List<TimeBlock> windows = List.of(at(9, 10), at(13, 18));

        Schedule schedule = placer.place(List.of(whole("A", 2)), windows);

        assertEquals(1, schedule.getScheduledBlocks().size());
        assertBlock(schedule.getScheduledBlocks().get(0), "A", t(13), t(15));
    }

    @Test
    void taskBiggerThanEveryWindow_isUnscheduled() {
        Schedule schedule = placer.place(List.of(whole("Big", 6)), defaultWindows());

        assertTrue(schedule.getScheduledBlocks().isEmpty());
        assertEquals(1, schedule.getUnscheduledTasks().size());
        assertEquals("Big", schedule.getUnscheduledTasks().get(0).title());
    }

    @Test
    void deadlineTooEarly_isUnscheduled() {
        Schedule schedule = placer.place(List.of(wholeDue("A", 2, 10)), defaultWindows());

        assertTrue(schedule.getScheduledBlocks().isEmpty());
        assertEquals(1, schedule.getUnscheduledTasks().size());
    }

    @Test
    void deadlineJustEnough_isPlaced() {
        Schedule schedule = placer.place(List.of(wholeDue("A", 2, 11)), defaultWindows());

        assertEquals(1, schedule.getScheduledBlocks().size());
        assertBlock(schedule.getScheduledBlocks().get(0), "A", t(9), t(11));
    }

    @Test
    void splittableTask_usesTwoWindows() {
        Task task = splittable("B", Duration.ofHours(5), Duration.ofHours(1));

        Schedule schedule = placer.place(List.of(task), defaultWindows());

        assertEquals(2, schedule.getScheduledBlocks().size());
        assertBlock(schedule.getScheduledBlocks().get(0), "B", t(9), t(12));
        assertBlock(schedule.getScheduledBlocks().get(1), "B", t(13), t(15));
    }

    @Test
    void minChunk_skipsTinyWindow() {
        List<TimeBlock> windows = List.of(new TimeBlock(t(9, 0), t(9, 30)), at(13, 18));
        Task task = splittable("B", Duration.ofHours(2), Duration.ofHours(1));

        Schedule schedule = placer.place(List.of(task), windows);

        assertEquals(1, schedule.getScheduledBlocks().size());
        assertBlock(schedule.getScheduledBlocks().get(0), "B", t(13), t(15));
    }

    @Test
    void finalPiece_mayBeShorterThanMinChunk() {
        List<TimeBlock> windows = List.of(at(9, 10), at(13, 18));
        Task task = splittable("B", Duration.ofMinutes(90), Duration.ofHours(1));

        Schedule schedule = placer.place(List.of(task), windows);

        assertEquals(2, schedule.getScheduledBlocks().size());
        assertBlock(schedule.getScheduledBlocks().get(0), "B", t(9), t(10));
        assertBlock(schedule.getScheduledBlocks().get(1), "B", t(13), t(13, 30));
    }

    @Test
    void splittableTaskThatDoesNotFit_leavesNoBlocksBehind() {
        Task task = splittable("X", Duration.ofHours(10), Duration.ofHours(1));

        Schedule schedule = placer.place(List.of(task), defaultWindows());

        assertTrue(schedule.getScheduledBlocks().isEmpty());
        assertEquals(1, schedule.getUnscheduledTasks().size());
    }

    @Test
    void failedTask_doesNotConsumeAnyTime() {
        Task x = splittable("X", Duration.ofHours(10), Duration.ofHours(1));   // fails
        Task y = whole("Y", 2);                                                  // must still get 9-11

        Schedule schedule = placer.place(List.of(x, y), defaultWindows());

        assertEquals(1, schedule.getScheduledBlocks().size());
        assertBlock(schedule.getScheduledBlocks().get(0), "Y", t(9), t(11));
        assertEquals(1, schedule.getUnscheduledTasks().size());
        assertEquals("X", schedule.getUnscheduledTasks().get(0).title());
    }

    @Test
    void noTwoBlocksOverlap() {
        List<Task> tasks = List.of(
                whole("A", 2),
                whole("B", 3),
                splittable("C", Duration.ofHours(2), Duration.ofMinutes(30)),
                whole("D", 1),
                whole("E", 2));

        Schedule schedule = placer.place(tasks, defaultWindows());
        List<ScheduledBlock> blocks = schedule.getScheduledBlocks();

        assertFalse(blocks.isEmpty());
        for (int i = 0; i < blocks.size(); i++) {
            for (int j = i + 1; j < blocks.size(); j++) {
                TimeBlock a = blocks.get(i).block();
                TimeBlock b = blocks.get(j).block();
                boolean overlap = a.start().isBefore(b.end()) && b.start().isBefore(a.end());
                assertFalse(overlap, "Blocks overlap: " + a + " and " + b);
            }
        }
    }

    @Test
    void orderOfTasksChangesTheSchedule() {
        Task a = whole("A", 2);
        Task b = whole("B", 3);

        Schedule ab = placer.place(List.of(a, b), defaultWindows());
        Schedule ba = placer.place(List.of(b, a), defaultWindows());

        assertEquals(t(9), blockOf(ab, "A").block().start());    // A first: A takes 9-11
        assertEquals(t(13), blockOf(ba, "A").block().start());   // B first: B takes 9-12, A goes to 13
    }
}