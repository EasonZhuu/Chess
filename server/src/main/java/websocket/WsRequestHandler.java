package websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;


import java.io.IOException;

public class WsRequestHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    private final ConnectionManager connections = new ConnectionManager();
    private final Gson gson = new Gson();

    public WsRequestHandler(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext ctx) {
        try {
            UserGameCommand command = gson.fromJson(ctx.message(), UserGameCommand.class);

            if (command == null || command.getCommandType() == null) {
                sendError(ctx.session, "Error: missing command type");
                return;
            }

            switch (command.getCommandType()) {
                case CONNECT -> connect(command, ctx.session);
                case MAKE_MOVE -> {
                    MakeMoveCommand moveCommand = gson.fromJson(ctx.message(), MakeMoveCommand.class);
                    makeMove(moveCommand, ctx.session);
                }
                case LEAVE -> leave(command, ctx.session);
                case RESIGN -> resign(command, ctx.session);
            }
        } catch (Exception ex) {
            sendError(ctx.session, "Error: invalid websocket message");
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("Websocket closed");
        connections.remove(ctx.session);
    }

    private void connect(UserGameCommand command, Session session) throws IOException {
        WsGameContext context = loadGameContext(command, session, "connect to game");
        if (context == null) {
            return;
        }

        GameData gameData = context.gameData();
        String username = context.authData().username();
        String role = "observer";

        if (username.equals(gameData.whiteUsername())) {
            role = "WHITE";
        } else if (username.equals(gameData.blackUsername())) {
            role = "BLACK";
        }

        connections.add(command.getGameID(), username, session);

        connections.send(session, new LoadGameMessage(gameData));

        String notification;
        if (role.equals("observer")) {
            notification = username + " joined as an observer";
        } else {
            notification = username + " joined as " + role;
        }

        connections.broadcastExcept(command.getGameID(), username, new NotificationMessage(notification));
    }

    private void makeMove(MakeMoveCommand command, Session session) throws IOException {
        ChessMove move = command.getMove();
        if (move == null) {
            sendError(session, "Error: missing move");
            return;
        }

        WsGameContext context = loadGameContext(command, session, "make move");
        if (context == null) {
            return;
        }

        AuthData authData = context.authData();
        GameData gameData = context.gameData();
        ChessGame game = gameData.game();

        if (game.isGameOver()) {
            sendError(session, "Error: game is over");
            return;
        }

        String username = authData.username();
        ChessGame.TeamColor playerColor = getPlayerColor(username, gameData);

        if (playerColor == null) {
            sendError(session, "Error: observers cannot make moves");
            return;
        }

        ChessPiece piece = game.getBoard().getPiece(move.getStartPosition());

        if (piece == null) {
            sendError(session, "Error: no piece at start position");
            return;
        }

        if (piece.getTeamColor() != playerColor) {
            sendError(session, "Error: cannot move opponent piece");
            return;
        }

        if (game.getTeamTurn() != playerColor) {
            sendError(session, "Error: not your turn");
            return;
        }

        try {
            game.makeMove(move);
        } catch (InvalidMoveException ex) {
            sendError(session, "Error: invalid move");
            return;
        }

        ChessGame.TeamColor nextPlayer = game.getTeamTurn();
        String checkedUsername = getUsernameForColor(nextPlayer, gameData);
        String extraNotification = null;

        if (game.isInCheckmate(nextPlayer)) {
            game.setGameOver(true);
            extraNotification = checkedUsername + " is in checkmate";
        } else if (game.isInStalemate(nextPlayer)) {
            game.setGameOver(true);
            extraNotification = checkedUsername + " is in stalemate";
        } else if (game.isInCheck(nextPlayer)) {
            extraNotification = checkedUsername + " is in check";
        }

        try {
            gameDAO.updateGame(gameData);
        } catch (DataAccessException ex) {
            sendError(session, "Error: unable to save move");
            return;
        }

        connections.broadcast(command.getGameID(), new LoadGameMessage(gameData));

        String moveMessage = username + " moved " + describeMove(move);
        connections.broadcastExcept(command.getGameID(), username, new NotificationMessage(moveMessage));

        if (extraNotification != null) {
            connections.broadcast(command.getGameID(), new NotificationMessage(extraNotification));
        }
    }

    private ChessGame.TeamColor getPlayerColor(String username, GameData gameData){
        if (username.equals(gameData.whiteUsername())) {
            return ChessGame.TeamColor.WHITE;
        }

        if (username.equals(gameData.blackUsername())) {
            return ChessGame.TeamColor.BLACK;
        }

        return null;
    }

    private String getUsernameForColor(ChessGame.TeamColor color, GameData gameData){
        if (color == ChessGame.TeamColor.WHITE) {
            return gameData.whiteUsername();
        }
        return  gameData.blackUsername();
    }

    private String describeMove(ChessMove move) {
        return "(" +
                move.getStartPosition().getRow() +
                "," +
                move.getStartPosition().getColumn() +
                ") to (" +
                move.getEndPosition().getRow() +
                "," +
                move.getEndPosition().getColumn() +
                ")";
    }

    private void leave(UserGameCommand command, Session session) throws IOException {
        WsGameContext context = loadGameContext(command, session, "leave game");
        if (context == null) {
            return;
        }

        GameData gameData = context.gameData();
        String username = context.authData().username();
        GameData updatedGameData = gameData;

        if (username.equals(gameData.whiteUsername())) {
            updatedGameData = new GameData(
                    gameData.gameID(),
                    null,
                    gameData.blackUsername(),
                    gameData.gameName(),
                    gameData.game()
            );
        } else if (username.equals(gameData.blackUsername())) {
            updatedGameData = new GameData(
                    gameData.gameID(),
                    gameData.whiteUsername(),
                    null,
                    gameData.gameName(),
                    gameData.game()
            );
        }

        if (updatedGameData != gameData) {
            try {
                gameDAO.updateGame(updatedGameData);
            } catch (DataAccessException ex) {
                sendError(session, "Error: unable to save leave");
                return;
            }
        }

        connections.remove(command.getGameID(), username);
        connections.broadcast(command.getGameID(), new NotificationMessage(username + " left the game"));


    }

    private void resign(UserGameCommand command, Session session) throws IOException {
        WsGameContext context = loadGameContext(command, session, "resign");
        if (context == null) {
            return;
        }

        AuthData authData = context.authData();
        GameData gameData = context.gameData();
        ChessGame game = gameData.game();

        if (game.isGameOver()) {
            sendError(session, "Error: game is over");
            return;
        }

        String username = authData.username();
        ChessGame.TeamColor playerColor = getPlayerColor(username, gameData);

        if (playerColor == null) {
            sendError(session, "Error: observers cannot resign");
            return;
        }

        game.setGameOver(true);

        try {
            gameDAO.updateGame(gameData);
        } catch (DataAccessException ex){
            sendError(session, "Error: unable to save resign");
            return;
        }

        connections.broadcast(command.getGameID(), new NotificationMessage(username + " resigned"));
    }

    private WsGameContext loadGameContext(UserGameCommand command, Session session,
                                          String action) throws IOException {
        if (command.getGameID() == null) {
            sendError(session, "Error: missing game ID");
            return null;
        }

        AuthData authData;
        GameData gameData;

        try {
            authData = authDAO.getAuth(command.getAuthToken());
            gameData = gameDAO.getGame(command.getGameID());
        } catch (DataAccessException ex) {
            sendError(session, "Error: unable to " + action);
            return null;
        }

        if (authData == null) {
            sendError(session, "Error: invalid auth token");
            return null;
        }

        if (gameData == null) {
            sendError(session, "Error: invalid game ID");
            return null;
        }

        return new WsGameContext(authData, gameData);
    }

    private record WsGameContext(AuthData authData, GameData gameData) {
    }

    private void sendError(Session session, String errorMessage) {
        try {
            connections.send(session, new ErrorMessage(errorMessage));
        } catch (IOException ex) {
            System.out.println("Unable to send websocket error message");
        }
    }
}
