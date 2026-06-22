package client;

import chess.ChessMove;
import com.google.gson.Gson;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.net.URI;

public class WebSocketFacade extends Endpoint {
    private final Gson gson = new Gson();
    private final ServerMessageObserver observer;
    private Session session;

    public WebSocketFacade(int port, ServerMessageObserver observer) throws ResponseException {
        this.observer = observer;

        try {
            URI uri = new URI("ws://localhost:" + port + "/ws");
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            session = container.connectToServer(this, uri);

            session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    receive(message);
                }
            });
        } catch (Exception ex) {
            throw new ResponseException("Unable to open websocket: " + ex.getMessage());
        }
    }

    public void connect(String authToken, int gameID) throws ResponseException {
        UserGameCommand command = new UserGameCommand(
                UserGameCommand.CommandType.CONNECT,
                authToken,
                gameID
        );
        send(command);
    }

    public void makeMove(String authToken, int gameID, ChessMove move) throws ResponseException {
        MakeMoveCommand command = new MakeMoveCommand(authToken, gameID, move);
        send(command);
    }

    public void leave(String authToken, int gameID) throws ResponseException {
        UserGameCommand command = new UserGameCommand(
                UserGameCommand.CommandType.LEAVE,
                authToken,
                gameID
        );
        send(command);
    }

    public void resign(String authToken, int gameID) throws ResponseException {
        UserGameCommand command = new UserGameCommand(
                UserGameCommand.CommandType.RESIGN,
                authToken,
                gameID
        );
        send(command);
    }

    public void close() {
        try {
            if (session != null && session.isOpen()) {
                session.close();
            }
        } catch (IOException ex) {
            observer.showError("Error: unable to close websocket");
        }
    }

    private void send(Object command) throws ResponseException {
        try {
            String json = gson.toJson(command);
            session.getBasicRemote().sendText(json);
        } catch (Exception ex) {
            throw new ResponseException("Unable to send websocket message: " + ex.getMessage());
        }
    }

    private void receive(String message) {
        try {
            ServerMessage serverMessage = gson.fromJson(message, ServerMessage.class);

            if (serverMessage == null || serverMessage.getServerMessageType() == null) {
                observer.showError("Error: invalid server message");
                return;
            }

            switch (serverMessage.getServerMessageType()) {
                case LOAD_GAME -> {
                    LoadGameMessage loadGameMessage = gson.fromJson(message, LoadGameMessage.class);
                    observer.loadGame(loadGameMessage.getGame());
                }
                case NOTIFICATION -> {
                    NotificationMessage notificationMessage = gson.fromJson(message, NotificationMessage.class);
                    observer.showNotification(notificationMessage.getMessage());
                }
                case ERROR -> {
                    ErrorMessage errorMessage = gson.fromJson(message, ErrorMessage.class);
                    observer.showError(errorMessage.getErrorMessage());
                }
            }
        } catch (Exception ex) {
            observer.showError("Error: unable to read server message");
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }
}