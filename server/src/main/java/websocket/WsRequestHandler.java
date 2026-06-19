package websocket;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;

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
        sendError(session, "Error: CONNECT is not implemented yet");
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