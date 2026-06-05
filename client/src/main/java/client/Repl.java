package client;

import java.util.Scanner;

public class Repl {
    private boolean loggedIn = false;
    private final Scanner scanner = new Scanner(System.in);
    private final ServerFacade facade = new ServerFacade(8080);
    private String authToken;
    private String username;


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
            } else if (command.equalsIgnoreCase("register") && !loggedIn) {
                register();
            } else if (command.equalsIgnoreCase("login") && !loggedIn) {
                login();
            } else {
                System.out.println("Unknown command. Type help to see possible commands.");
            }
        }
    }

    private void register() {
        try{
            System.out.print("Username: ");
            String inputUsername = scanner.nextLine().trim();

            System.out.print("Password: ");
            String password = scanner.nextLine().trim();

            System.out.print("Email: ");
            String email = scanner.nextLine().trim();

            var authData = facade.register(inputUsername, password, email);

            authToken = authData.authToken();
            username = authData.username();
            loggedIn = true;

            System.out.println("Logged in as " + username);
        } catch (ResponseException ex) {
            System.out.println(ex.getMessage());
        }
    }


    private void login() {
        try {
            System.out.print("Username: ");
            String inputUsername = scanner.nextLine().trim();

            System.out.print("Password: ");
            String password = scanner.nextLine().trim();

            var authData = facade.login(username, password);

            authToken = authData.authToken();
            username = authData.username();
            loggedIn = true;
        } catch (ResponseException ex) {
            System.out.println(ex.getMessage());
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
