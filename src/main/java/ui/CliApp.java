package ui;

import java.util.Scanner;
import model.Task;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import model.TimeBlock;
import java.time.LocalTime;
import core.FreeSlotCalculator;
import core.Placer;
import model.Schedule;
import model.ScheduledBlock;
import model.UserSettings;
import java.time.LocalDate;
import java.util.Comparator;

// --- will be changed later ---
public class CliApp {

    private final Scanner in = new Scanner(System.in);

    private static final DateTimeFormatter DATE_TIME_INPUT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_TIME_SHOW = DateTimeFormatter.ofPattern("EEE dd.MM HH:mm", Locale.ENGLISH);
    private final List<TimeBlock> busySlots = new ArrayList<>();
    private LocalTime workStart = LocalTime.of(9, 0);
    private LocalTime workEnd = LocalTime.of(18, 0);
    private final List<Task> tasks = new ArrayList<>();
    private int nextId = 1;
    private static final DateTimeFormatter DAY_SHOW = DateTimeFormatter.ofPattern("EEEE dd.MM.yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_SHOW = DateTimeFormatter.ofPattern("HH:mm");
    private final FreeSlotCalculator calculator = new FreeSlotCalculator();
    private final Placer placer = new Placer();

    private void printHelp() {
        System.out.println("Commands: add, list, remove, busy, hours, generate, demo, help, quit");
    }

    public void run() {
        System.out.println("=== DeadOnTime ===");
        printHelp();

        boolean running = true;
        while (running) {
            System.out.print("> ");
            String command = in.nextLine().trim().toLowerCase();

            switch (command) {
                case "help" -> printHelp();
                case "quit" -> running = false;
                case "" -> { }
                case "add" -> addTask();
                case "list" -> listTasks();
                case "remove" -> removeTask();
                case "busy" -> addBusySlot();
                case "hours" -> setWorkingHours();
                case "generate" -> generate();
                case "demo" -> loadDemo();
                default -> System.out.println("Unknown command. Type 'help'.");
            }
        }
        System.out.println("Bye!");
    }

    private void addTask() {
        String title = askText("Title:");
        Duration estimate = askMinutes("Estimate in minutes:");
        LocalDateTime deadline = askDateTime("Deadline (yyyy-MM-dd HH:mm):");
        int priority = askInt("Priority (1 = high, 2 = medium, 3 = low):", 1, 3);
        boolean splittable = askYesNo("Can it be split into parts? (y/n):");
        Duration minChunk = splittable ? askMinutes("Smallest allowed part in minutes:") : Duration.ZERO;

        String id = String.valueOf(nextId++);
        tasks.add(new Task(id, title, estimate, deadline, priority, splittable, minChunk));
        System.out.println("Added task #" + id);
    }

    private void listTasks() {
        if (tasks.isEmpty()) {
            System.out.println("No tasks yet. Type 'add'.");
            return;
        }
        System.out.printf("%-3s %-20s %-9s %-14s %-5s %s%n", "ID", "Title", "Estimate", "Deadline", "Priority", "Type");
        for (Task t : tasks) {
            System.out.printf("%-3s %-20s %-9s %-14s %-5d %s%n",
                    t.id(),
                    t.title(),
                    formatDuration(t.estimate()),
                    DATE_TIME_SHOW.format(t.deadline()),
                    t.priority(),
                    t.splittable() ? "split" : "whole");
        }
    }

    private String formatDuration(Duration d) {
        long hours = d.toHours();
        long minutes = d.toMinutesPart();
        if (hours == 0) {
            return minutes + "m";
        }
        if (minutes == 0) {
            return hours + "h";
        }
        return hours + "h " + minutes + "m";
    }
    private String askText(String question) {
        while (true) {
            System.out.print(question + " ");
            String text = in.nextLine().trim();
            if (!text.isEmpty()) {
                return text;
            }
            System.out.println("Please enter something.");
        }
    }

    private int askInt(String question, int min, int max) {
        while (true) {
            System.out.print(question + " ");
            try {
                int value = Integer.parseInt(in.nextLine().trim());
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("Please enter a number from " + min + " to " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    private Duration askMinutes(String question) {
        return Duration.ofMinutes(askInt(question, 1, 10_000));
    }

    private LocalDateTime askDateTime(String question) {
        while (true) {
            System.out.print(question + " ");
            try {
                return LocalDateTime.parse(in.nextLine().trim(), DATE_TIME_INPUT);
            } catch (DateTimeParseException e) {
                System.out.println("Use the format yyyy-MM-dd HH:mm, for example 2026-11-09 18:00.");
            }
        }
    }

    private boolean askYesNo(String question) {
        while (true) {
            System.out.print(question + " ");
            String answer = in.nextLine().trim().toLowerCase();
            if (answer.equals("y") || answer.equals("yes")) {
                return true;
            }
            if (answer.equals("n") || answer.equals("no")) {
                return false;
            }
            System.out.println("Please answer y or n.");
        }
    }

    private void removeTask() {
        String id = askText("Id to remove:");
        boolean removed = tasks.removeIf(t -> t.id().equals(id));
        System.out.println(removed ? "Removed." : "No task with id " + id);
    }

    private void addBusySlot() {
        LocalDateTime start = askDateTime("Busy from (yyyy-MM-dd HH:mm):");
        LocalDateTime end = askDateTime("Busy until (yyyy-MM-dd HH:mm):");
        try {
            busySlots.add(new TimeBlock(start, end));
            System.out.println("Busy slot added.");
        } catch (IllegalArgumentException e) {
            System.out.println("The end must not be before the start.");
        }
    }

    private void setWorkingHours() {
        LocalTime start = askTime("Work starts (HH:mm):");
        LocalTime end = askTime("Work ends (HH:mm):");
        if (!end.isAfter(start)) {
            System.out.println("Work must end after it starts.");
            return;
        }
        workStart = start;
        workEnd = end;
        System.out.println("Working hours: " + workStart + "-" + workEnd);
    }

    private LocalTime askTime(String question) {
        while (true) {
            System.out.print(question + " ");
            try {
                return LocalTime.parse(in.nextLine().trim());
            } catch (DateTimeParseException e) {
                System.out.println("Use the format HH:mm, for example 09:00.");
            }
        }
    }
    private void generate() {
        if (tasks.isEmpty()) {
            System.out.println("No tasks yet. Type 'add'.");
            return;
        }
        LocalDate from = askDate("Start date (yyyy-MM-dd):");
        int days = askInt("Number of days (1-14):", 1, 14);

        List<TimeBlock> free = calculator.calculate(currentSettings(), from, days);
        Schedule schedule = placer.place(tasks, free);
        printSchedule(schedule);
    }

    private UserSettings currentSettings() {
        return new UserSettings(workStart, workEnd,
                Duration.ofHours(6), Duration.ofMinutes(90), Duration.ofMinutes(15),
                busySlots);
    }

    private void printSchedule(Schedule schedule) {
        List<ScheduledBlock> blocks = new ArrayList<>(schedule.getScheduledBlocks());
        blocks.sort(Comparator.comparing(b -> b.block().start()));

        LocalDate lastDay = null;
        for (ScheduledBlock b : blocks) {
            LocalDate day = b.block().start().toLocalDate();
            if (!day.equals(lastDay)) {
                System.out.println();
                System.out.println(DAY_SHOW.format(day));
                lastDay = day;
            }
            System.out.printf("  %s-%s  %s%n",
                    TIME_SHOW.format(b.block().start()),
                    TIME_SHOW.format(b.block().end()),
                    b.task().title());
        }

        for (Task t : schedule.getUnscheduledTasks()) {
            System.out.println();
            System.out.println("NOT SCHEDULED: " + t.title() + " (deadline " + DATE_TIME_SHOW.format(t.deadline()) + ")");
        }

        int unscheduled = schedule.getUnscheduledTasks().size();
        System.out.println();
        System.out.println("Placed: " + (tasks.size() - unscheduled) + " of " + tasks.size() + " tasks");
    }
    private LocalDate askDate(String question) {
        while (true) {
            System.out.print(question + " ");
            try {
                return LocalDate.parse(in.nextLine().trim());
            } catch (DateTimeParseException e) {
                System.out.println("Use the format yyyy-MM-dd, for example 2026-11-09.");
            }
        }
    }
    private void loadDemo() {
        tasks.clear();
        busySlots.clear();
        nextId = 1;
        workStart = LocalTime.of(9, 0);
        workEnd = LocalTime.of(18, 0);

        LocalDate monday = LocalDate.of(2026, 11, 9);    // fixed date, so the demo never changes

        addDemoTask("Email backlog", 60,  monday.atTime(12, 0),              1, false, 0);
        addDemoTask("Essay",         120, monday.plusDays(1).atTime(12, 0),  5, false, 0);
        addDemoTask("Report",        180, monday.atTime(18, 0),              3, false, 0);
        addDemoTask("Coursework",    300, monday.plusDays(4).atTime(23, 59), 5, true, 60);
        addDemoTask("Read papers",   240, monday.plusDays(3).atTime(18, 0),  2, true, 30);

        for (int i = 0; i < 5; i++) {                    // lunch on every weekday
            LocalDate day = monday.plusDays(i);
            busySlots.add(new TimeBlock(day.atTime(12, 0), day.atTime(13, 0)));
        }

        System.out.println("Demo loaded. Try 'list', then 'generate' from 2026-11-09 for 5 days.");
    }

    private void addDemoTask(String title, int minutes, LocalDateTime deadline,
                             int priority, boolean splittable, int minChunkMinutes) {
        tasks.add(new Task(String.valueOf(nextId++), title, Duration.ofMinutes(minutes),
                deadline, priority, splittable, Duration.ofMinutes(minChunkMinutes)));
    }
}