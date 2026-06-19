package websocket;

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
                case MAKE_MOVE -> makeMove(command, ctx.session);
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
        if (command.getGameID() == null) {
            sendError(session, "Error: missing game ID");
            return;
        }

        AuthData authData;
        GameData gameData;

        try {
            authData = authDAO.getAuth(command.getAuthToken());
            gameData = gameDAO.getGame(command.getGameID());
        } catch (DataAccessException ex) {
            sendError(session, "Error: unable to connect to game");
            return;
        }

        if (authData == null) {
            sendError(session, "Error: invalid auth token");
            return;
        }

        if (gameData == null) {
            sendError(session, "Error: invalid game ID");
            return;
        }

        String username = authData.username();
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

    private void makeMove(UserGameCommand command, Session session) throws IOException {
        sendError(session, "Error: MAKE_MOVE is not implemented yet");
    }

    private void leave(UserGameCommand command, Session session) throws IOException {
        sendError(session, "Error: LEAVE is not implemented yet");
    }

    private void resign(UserGameCommand command, Session session) throws IOException {
        sendError(session, "Error: RESIGN is not implemented yet");
    }

    private void sendError(Session session, String errorMessage) {
        try {
            connections.send(session, new ErrorMessage(errorMessage));
        } catch (IOException ex) {
            System.out.println("Unable to send websocket error message");
        }
    }
}