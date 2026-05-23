package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import model.AuthData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GameServiceTests {
    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private GameService gameService;

    @BeforeEach
    void setup() {
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        gameService = new GameService(authDAO, gameDAO);
    }

    @Test
    void createGamePositive() throws DataAccessException, ServiceException {
        authDAO.createAuth(new AuthData("token-1", "u1"));

        var result = gameService.createGame(new CreateGameRequest("token-1", "g1"));

        Assertions.assertTrue(result.gameID() > 0);
        Assertions.assertNotNull(gameDAO.getGame(result.gameID()));
        Assertions.assertEquals("g1", gameDAO.getGame(result.gameID()).gameName());
    }

    @Test
    void createGameNegativeUnauthorized() {
        var ex = Assertions.assertThrows(ServiceException.class,
                () -> gameService.createGame(new CreateGameRequest("missing-token", "g1")));

        Assertions.assertEquals(401, ex.statusCode());
        Assertions.assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    void listGamesPositive() throws DataAccessException, ServiceException {
        authDAO.createAuth(new AuthData("token-1", "u1"));
        gameDAO.createGame("g1");
        gameDAO.createGame("g2");

        var result = gameService.listGames(new ListGamesRequest("token-1"));

        Assertions.assertNotNull(result.games());
        Assertions.assertEquals(2, result.games().size());
    }

    @Test
    void listGamesNegativeUnauthorized() {
        var ex = Assertions.assertThrows(ServiceException.class,
                () -> gameService.listGames(new ListGamesRequest("bad-token")));

        Assertions.assertEquals(401, ex.statusCode());
        Assertions.assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    void joinGamePositive() throws DataAccessException, ServiceException {
        authDAO.createAuth(new AuthData("token-1", "u1"));
        int gameID = gameDAO.createGame("g1");

        var result = gameService.joinGame(new JoinGameRequest("token-1", "WHITE", gameID));

        Assertions.assertNotNull(result);
        Assertions.assertEquals("u1", gameDAO.getGame(gameID).whiteUsername());
    }

    @Test
    void joinGameNegativeAlreadyTaken() throws DataAccessException, ServiceException {
        authDAO.createAuth(new AuthData("token-1", "u1"));
        authDAO.createAuth(new AuthData("token-2", "u2"));
        int gameID = gameDAO.createGame("g1");
        gameService.joinGame(new JoinGameRequest("token-1", "BLACK", gameID));

        var ex = Assertions.assertThrows(ServiceException.class,
                () -> gameService.joinGame(new JoinGameRequest("token-2", "BLACK", gameID)));

        Assertions.assertEquals(403, ex.statusCode());
        Assertions.assertEquals("Error: already taken", ex.getMessage());
    }
}
