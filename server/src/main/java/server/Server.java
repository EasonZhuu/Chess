package server;

import dataaccess.*;
import handler.*;
import io.javalin.*;
import io.javalin.Javalin;
import io.javalin.json.JavalinGson;
import service.GameService;
import service.UserService;
import service.clear.ClearService;
import dataaccess.MySqlDatabaseInitializer;
import dataaccess.DataAccessException;
import websocket.WsRequestHandler;



public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> {
            config.staticFiles.add("web");
            config.jsonMapper(new JavalinGson());
        });

        try {
            MySqlDatabaseInitializer.configureDatabase();
        } catch (DataAccessException ex) {
            throw new RuntimeException("Unable to configure database", ex);
        }

        UserDAO userDAO = new MySqlUserDAO();
        AuthDAO authDAO = new MySqlAuthDAO();
        GameDAO gameDAO = new MySqlGameDAO();

        WsRequestHandler wsHandler = new WsRequestHandler(authDAO, gameDAO);
        javalin.ws("/ws", ws -> {
            ws.onConnect(wsHandler);
            ws.onMessage(wsHandler);
            ws.onClose(wsHandler);
        });

        MemoryClearDAO clearDAO = new MemoryClearDAO(userDAO, authDAO, gameDAO);
        ClearService clearService = new ClearService(clearDAO);
        ClearHandler clearHandler = new ClearHandler(clearService);
        javalin.delete("/db", ctx -> clearHandler.clear(ctx));

        UserService userService = new UserService(userDAO, authDAO);
        RegisterHandler registerHandler = new RegisterHandler(userService);
        javalin.post("/user", ctx -> registerHandler.register(ctx));

        LoginHandler loginHandler = new LoginHandler(userService);
        javalin.post("/session", ctx -> loginHandler.login(ctx));

        LogoutHandler logoutHandler = new LogoutHandler(userService);
        javalin.delete("/session", ctx -> logoutHandler.logout(ctx));

        GameService gameService = new GameService(authDAO, gameDAO);
        CreateGameHandler createGameHandler = new CreateGameHandler(gameService);
        javalin.post("/game", ctx -> createGameHandler.createGame(ctx));

        ListGamesHandler listGamesHandler = new ListGamesHandler(gameService);
        javalin.get("/game", listGamesHandler::listGames);

        JoinGameHandler joinGameHandler = new JoinGameHandler(gameService);
        javalin.put("/game", joinGameHandler::joinGame);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
