package client;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
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
    private ChessGame.TeamColor currentPlayerColor;


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
                currentPlayerColor = ChessGame.TeamColor.BLACK;
            } else {
                currentPerspective = ChessGame.TeamColor.WHITE;
                currentPlayerColor = ChessGame.TeamColor.WHITE;
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
            currentPlayerColor = null;

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
        } else if (command.toLowerCase().startsWith("move ")) {
            makeMove(command);
        } else if (command.equalsIgnoreCase("resign")) {
            resignGame();
        } else if (command.toLowerCase().startsWith("highlight ")) {
            highlightMoves(command);
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

    private void makeMove(String command) {
        if (currentPlayerColor == null) {
            System.out.println("Observers cannot make moves.");
            return;
        }

        if (webSocket == null || currentGameID == null) {
            System.out.println("No active game.");
            return;
        }

        String[] parts = command.trim().split("\\s+");
        if (parts.length != 3 && parts.length != 4) {
            System.out.println("Use: move e2 e4");
            System.out.println("Promotion example: move e7 e8 queen");
            return;
        }

        ChessPosition start = ChessInputParser.parsePosition(parts[1]);
        ChessPosition end = ChessInputParser.parsePosition(parts[2]);

        if (start == null || end == null) {
            System.out.println("Positions must be like e2 or h7.");
            return;
        }

        ChessPiece.PieceType promotionPiece = null;
        if (parts.length == 4) {
            promotionPiece = ChessInputParser.parsePromotionPiece(parts[3]);
            if (promotionPiece == null) {
                System.out.println("Promotion must be queen, rook, bishop, or knight.");
                return;
            }
        }

        ChessMove move = new ChessMove(start, end, promotionPiece);

        try {
            webSocket.makeMove(authToken, currentGameID, move);
        } catch (ResponseException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void resignGame() {
        if (currentPlayerColor == null) {
            System.out.println("Observers cannot resign.");
            return;
        }

        if (webSocket == null || currentGameID == null) {
            System.out.println("No active game.");
            return;
        }

        System.out.print("Are you sure you want to resign? Type yes to confirm: ");
        String answer = scanner.nextLine().trim();

        if (!answer.equalsIgnoreCase("yes")) {
            System.out.println("Resign cancelled.");
            return;
        }

        try {
            webSocket.resign(authToken, currentGameID);
        } catch (ResponseException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void highlightMoves(String command) {
        if (currentGame == null) {
            System.out.println("No game loaded yet.");
            return;
        }

        String[] parts = command.trim().split("\\s+");
        if (parts.length != 2) {
            System.out.println("Use: highlight e2");
            return;
        }

        ChessPosition selectedPosition = ChessInputParser.parsePosition(parts[1]);
        if (selectedPosition == null) {
            System.out.println("Position must be like e2 or h7.");
            return;
        }

        ChessPiece selectedPiece = currentGame.game().getBoard().getPiece(selectedPosition);
        if (selectedPiece == null) {
            System.out.println("No piece at that position.");
            return;
        }

        Collection<ChessMove> validMoves = currentGame.game().validMoves(selectedPosition);
        ArrayList<ChessPosition> highlightedPositions = new ArrayList<>();

        if (validMoves != null) {
            for (ChessMove move : validMoves) {
                highlightedPositions.add(move.getEndPosition());
            }
        }

        if (currentPerspective == null) {
            currentPerspective = ChessGame.TeamColor.WHITE;
        }

        System.out.println(BoardDrawer.drawBoard(
                currentGame.game().getBoard(),
                currentPerspective,
                selectedPosition,
                highlightedPositions
        ));
    }

    private void clearGameState() {
        webSocket = null;
        currentGame = null;
        currentGameID = null;
        currentPerspective = null;
        currentPlayerColor = null;
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
                move e2 e4 - to move a piece
                move e7 e8 queen - to move with promotion
                resign - forfeit the game
                highlight e2 - show legal moves for a piece
                leave - the current game
                help - with possible commands
                """;
    }

}
