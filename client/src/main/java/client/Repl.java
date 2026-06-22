package client;

import chess.ChessGame;
import model.GameData;
import ui.BoardDrawer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;

public class Repl implements ServerMessageObserver {
    private boolean loggedIn = false;
    private boolean inGame = false;
    private final Scanner scanner = new Scanner(System.in);
    private final ServerFacade facade = new ServerFacade(8080);
    private String authToken;
    private String username;
    private ArrayList<GameData> currentGames = new ArrayList<>();
    private WebSocketFacade webSocket;
    private GameData currentGame;
    private Integer currentGameID;
    private ChessGame.TeamColor currentPerspective;


    public void run() {
        System.out.println("Welcome to 240 chess. Type help to get started.");
        while (true) {
            System.out.print(prompt());
            String command = scanner.nextLine().trim();

            if (command.equalsIgnoreCase("quit")) {
                if (webSocket != null) {
                    webSocket.close();
                }
                System.out.println("Goodbye");
                return;
            }

            if (inGame) {
                handleGameplayCommand(command);
            } else if (command.equalsIgnoreCase("help")) {
                System.out.println(help());
            } else if (command.equalsIgnoreCase("register") && !loggedIn) {
                register();
            } else if (command.equalsIgnoreCase("login") && !loggedIn) {
                login();
            } else if (command.equalsIgnoreCase("logout") && loggedIn) {
                logout();
            } else if (command.equalsIgnoreCase("create") && loggedIn) {
                createGame();
            } else if (command.equalsIgnoreCase("list") && loggedIn) {
                listGames();
            } else if (command.equalsIgnoreCase("play") && loggedIn) {
                playGame();
            } else if (command.equalsIgnoreCase("observe") && loggedIn) {
                observeGame();
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
            currentGames.clear();

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

            var authData = facade.login(inputUsername, password);

            authToken = authData.authToken();
            username = authData.username();
            loggedIn = true;
            currentGames.clear();

            System.out.println("Logged in as " + username);
        } catch (ResponseException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void logout() {
        try{
            facade.logout(authToken);

            authToken = null;
            username = null;
            loggedIn = false;
            currentGames.clear();

            System.out.println("Logged out.");
        } catch (ResponseException ex) {
            System.out.println(ex.getMessage());
        }

    }

    private void createGame() {
        try {
            System.out.print("Game name: ");
            String gameName = scanner.nextLine().trim();

            facade.createGame(authToken, gameName);

            System.out.println("Created game: " + gameName);
        } catch (ResponseException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void listGames() {
        try {
            Collection<GameData> games = facade.listGames(authToken);
            currentGames = new ArrayList<>(games);

            if (currentGames.isEmpty()){
                System.out.println("No games found.");
                return;
            }

            for (int i = 0; i < currentGames.size(); i++) {
                GameData game = currentGames.get(i);
                System.out.printf("#%d%nName: %s%nWhite: %s%nBlack: %s%n",
                        i + 1,
                        game.gameName(),
                        playerName(game.whiteUsername()),
                        playerName(game.blackUsername()));
            }
        }catch (ResponseException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private String playerName (String username) {
        if (username == null || username.isBlank()){
            return "empty";
        }

        return username;
    }

    private void playGame() {
        if (currentGames.isEmpty()){
            System.out.println("List games before choosing one to play.");
            return;
        }

        try {
            System.out.print("Game number: ");
            int gameNumber = Integer.parseInt(scanner.nextLine().trim());

            if (gameNumber < 1 || gameNumber > currentGames.size()){
                System.out.println("Invalid game number.");
                return;
            }

            System.out.print("Color (WHITE/BLACK): ");
            String color = scanner.nextLine().trim().toUpperCase();

            if (!color.equals("WHITE") && !color.equals("BLACK")) {
                System.out.println("Color must be WHITE or BLACK.");
                return;
            }

            GameData game = currentGames.get(gameNumber - 1);
            int gameID = game.gameID();
            facade.joinGame(authToken, color, gameID);

            currentGameID = gameID;

            if (color.equals("BLACK")) {
                currentPerspective = ChessGame.TeamColor.BLACK;
            } else {
                currentPerspective = ChessGame.TeamColor.WHITE;
            }

            if (webSocket != null) {
                webSocket.close();
            }

            webSocket = new WebSocketFacade(8080, this);
            webSocket.connect(authToken, gameID);
            inGame = true;
        } catch (NumberFormatException ex) {
            System.out.println("Game number must be a number.");
        } catch (ResponseException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void observeGame() {
        if (currentGames.isEmpty()) {
            System.out.println("List games before choosing one to observe.");
            return;
        }

        try {
            System.out.print("Game number: ");
            int gameNumber = Integer.parseInt(scanner.nextLine().trim());

            if (gameNumber < 1 || gameNumber > currentGames.size()){
                System.out.println("Invalid game number.");
                return;
            }

            GameData game = currentGames.get(gameNumber - 1);
            currentGameID = game.gameID();
            currentPerspective = ChessGame.TeamColor.WHITE;

            if (webSocket != null) {
                webSocket.close();
            }

            webSocket = new WebSocketFacade(8080, this);
            webSocket.connect(authToken, currentGameID);
            inGame = true;
        } catch (NumberFormatException ex) {
            System.out.println("Game number must be a number.");
        } catch (ResponseException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void handleGameplayCommand(String command) {
        if (command.equalsIgnoreCase("help")) {
            System.out.println(gameplayHelp());
        } else if (command.equalsIgnoreCase("redraw")) {
            redrawGame();
        } else if (command.equalsIgnoreCase("leave")) {
            leaveGame();
        } else {
            System.out.println("Unknown command. Type help to see possible commands.");
        }
    }

    private void redrawGame() {
        if (currentGame == null) {
            System.out.println("No game loaded yet.");
            return;
        }

        if (currentPerspective == null) {
            currentPerspective = ChessGame.TeamColor.WHITE;
        }

        System.out.println(BoardDrawer.drawBoard(currentGame.game().getBoard(), currentPerspective));
    }

    private void leaveGame() {
        if (webSocket == null || currentGameID == null) {
            clearGameState();
            System.out.println("Left game.");
            return;
        }

        try {
            webSocket.leave(authToken, currentGameID);
            webSocket.close();
            clearGameState();
            System.out.println("Left game.");
        } catch (ResponseException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void clearGameState() {
        webSocket = null;
        currentGame = null;
        currentGameID = null;
        currentPerspective = null;
        inGame = false;
    }

    @Override
    public void loadGame(GameData game) {
        currentGame = game;

        if (currentPerspective == null) {
            currentPerspective = ChessGame.TeamColor.WHITE;
        }

        System.out.println();
        System.out.println(BoardDrawer.drawBoard(game.game().getBoard(), currentPerspective));
    }

    @Override
    public void showNotification(String message) {
        System.out.println();
        System.out.println(message);
    }

    @Override
    public void showError(String errorMessage) {
        System.out.println();
        System.out.println(errorMessage);
    }

    private String prompt() {
        if (inGame) {
            return "[GAME] >>> ";
        } else if (loggedIn) {
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

    private String gameplayHelp() {
        return """
                redraw - the chess board
                leave - the current game
                help - with possible commands
                """;
    }

}
