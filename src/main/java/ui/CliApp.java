package ui;

import java.util.Scanner;

public class CliApp {

    private final Scanner in = new Scanner(System.in);

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
                default -> System.out.println("Unknown command. Type 'help'.");
            }
        }
        System.out.println("Bye!");
    }

    private void printHelp() {
        System.out.println("Commands: help, quit");
    }
}