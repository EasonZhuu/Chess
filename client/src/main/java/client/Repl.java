package client;

import java.util.Scanner;

public class Repl {
    private boolean loggedIn = false;
    private final Scanner scanner = new Scanner(System.in);

    public void run() {
        System.out.println("Welcome to 240 chess. Type help to get started.");
        while (true) {
            System.out.print(prompt());
            String command = scanner.nextLine().trim();

            if (command.equalsIgnoreCase("quit")) {
                System.out.println("Goodbye");
                return;
            }

            if (command.equalsIgnoreCase("help")) {
                System.out.println(help());
            } else {
                System.out.println("Unknown command. Type help to see possible commands.");
            }
        }
    }

    private String prompt() {
        if (loggedIn) {
            return "[LOGGED_IN] >>> ";
        } else {
            return  "[LOGGED_OUT] >>> ";
        }
    }

    private String help () {
        if (loggedIn) {
            return """
                    create - a game
                    list - games
                    play - a game
                    observe - a game
                    logout - when you are done
                    quit - playing chess
                    help - with possible commands
                    """;
        }

        return """
                register - to create an account
                login - to play chess
                quit - playing chess
                help - with possible commands
                """;
    }


}
