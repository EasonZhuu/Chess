package client;

import org.junit.jupiter.api.*;
import server.Server;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @BeforeEach
    public void clearDatabase() throws ResponseException{
        facade.clear();
    }
    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    public void serverFacadeStarts() {
        Assertions.assertNotNull(facade);
    }

    @Test
    public void registerSuccess() throws ResponseException {
        var authData = facade.register("player1", "password", "player1@gmail.com");
        Assertions.assertEquals("player1", authData.username());
        Assertions.assertNotNull(authData.authToken());
        Assertions.assertFalse(authData.authToken().isBlank());
    }

    @Test
    public void registerFailed() throws ResponseException {
        facade.register("player1", "password", "player1@gmail.com");
        Assertions.assertThrows(ResponseException.class, () -> facade.register("player1", "differentPassword", "other@gamil.com"));
    }

    @Test
    public void loginSuccess() throws ResponseException {
        facade.register("player1", "password", "player1@gmail.com");

        var authData = facade.login("player1", "password");

        Assertions.assertEquals("player1", authData.username());
        Assertions.assertNotNull(authData.authToken());
        Assertions.assertFalse(authData.authToken().isBlank());
    }

    @Test
    public void loginFailedWrongPassword() throws ResponseException {
        facade.register("player1", "password", "player1@gmail.com");

        Assertions.assertThrows(ResponseException.class, () -> facade.login("player1", "wrongPassword"));
    }

    @Test
    public void logoutSuccess() throws ResponseException {
        var authData = facade.register("player1", "password", "player1@gmail.com");

        facade.logout(authData.authToken());

        Assertions.assertThrows(ResponseException.class, () -> facade.listGames(authData.authToken()));
    }

    @Test
    public void logoutFailedBadToken() {
        Assertions.assertThrows(ResponseException.class, () -> facade.logout("bad-token"));
    }

    @Test
    public void createGameSuccess() throws ResponseException {
        var authData = facade.register("player1", "password", "player1@gmail.com");

        var gameID = facade.createGame(authData.authToken(), "My Game");

        Assertions.assertTrue(gameID > 0);
    }

    @Test
    public void createGameFailedBadToken() {
        Assertions.assertThrows(ResponseException.class, () -> facade.createGame("bad-token", "My Game"));
    }

    @Test
    public void listGamesSuccess() throws ResponseException {
        var authData = facade.register("player1", "password", "player1@gmail.com");

        facade.createGame(authData.authToken(), "My Game");

        var games = facade.listGames(authData.authToken());

        Assertions.assertEquals(1, games.size());
        Assertions.assertEquals("My Game", games.iterator().next().gameName());
    }

    @Test
    public void listGamesFailedBadToken() {
        Assertions.assertThrows(ResponseException.class, () -> facade.listGames("bad-token"));
    }

    @Test
    public void joinGameSuccess() throws ResponseException {
        var authData = facade.register("player1", "password", "player1@gmail.com");
        var gameID = facade.createGame(authData.authToken(), "My Game");

        facade.joinGame(authData.authToken(), "WHITE", gameID);

        var games = facade.listGames(authData.authToken());
        var game = games.iterator().next();

        Assertions.assertEquals("player1", game.whiteUsername());
    }

    @Test
    public void joinGameFailedColorTaken() throws ResponseException {
        var firstPlayer = facade.register("player1", "password", "player1@gmail.com");
        var gameID = facade.createGame(firstPlayer.authToken(), "My Game");

        facade.joinGame(firstPlayer.authToken(), "WHITE", gameID);

        var secondPlayer = facade.register("player2", "password", "player2@gmail.com");

        Assertions.assertThrows(ResponseException.class, () -> facade.joinGame(secondPlayer.authToken(), "WHITE", gameID));
    }

    @Test
    public void clearSuccess() throws ResponseException {
        facade.register("player1", "password", "player1@gmail.com");

        facade.clear();

        var authData = facade.register("player1", "password", "player1@gmail.com");

        Assertions.assertEquals("player1", authData.username());
    }

    @Test
    public void clearFailedServerUnavailable() {
        var badFacade = new ServerFacade(1);

        Assertions.assertThrows(ResponseException.class, badFacade::clear);
    }

}
