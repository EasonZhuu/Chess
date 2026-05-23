package server;

import io.javalin.*;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryClearDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.UserDAO;
import handler.ClearHandler;
import io.javalin.Javalin;
import io.javalin.json.JavalinGson;
import service.ClearService;

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
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
