package server;

import handler.*;
import io.javalin.*;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryClearDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.UserDAO;
import io.javalin.Javalin;
import io.javalin.json.JavalinGson;
import service.ClearService;
import service.GameService;
import service.UserService;




public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> {
            config.staticFiles.add("web");
            config.jsonMapper(new JavalinGson());
        });

        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();

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
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
