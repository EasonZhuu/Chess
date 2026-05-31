package dataaccess;

import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MySqlGameDAOTests {
    private MySqlGameDAO gameDAO;

    @BeforeEach
    void setUp() throws DataAccessException {
        MySqlDatabaseInitializer.configureDatabase();
        gameDAO = new MySqlGameDAO();

        var authDAO = new MySqlAuthDAO();
        var userDAO = new MySqlUserDAO();
        gameDAO.clear();
        authDAO.clear();
        userDAO.clear();
    }

    @Test
    void createGamePositive() throws DataAccessException {
        int id = gameDAO.createGame("g1");
        Assertions.assertTrue(id > 0);
    }

    @Test
    void createGameNegative() {
        Assertions.assertThrows(DataAccessException.class,
                () -> gameDAO.createGame(" "));
    }

    @Test
    void getGamePositive() throws DataAccessException {
        int id = gameDAO.createGame("g2");
        Assertions.assertEquals("g2", gameDAO.getGame(id).gameName());
    }

    @Test
    void getGameNegative() throws DataAccessException {
        Assertions.assertNull(gameDAO.getGame(999999));
    }

    @Test
    void listGamesPositive() throws DataAccessException {
        gameDAO.createGame("g3");
        gameDAO.createGame("g4");
        Assertions.assertEquals(2, gameDAO.listGames().size());
    }

    @Test
    void listGamesNegative() throws DataAccessException {
        Assertions.assertTrue(gameDAO.listGames().isEmpty());
    }

    @Test
    void updateGamePositive() throws DataAccessException {
        int id = gameDAO.createGame("g5");
        var oldGame = gameDAO.getGame(id);
        var updated = new GameData(id, oldGame.whiteUsername(), oldGame.blackUsername(), "g5-updated", oldGame.game());

        gameDAO.updateGame(updated);

        Assertions.assertEquals("g5-updated", gameDAO.getGame(id).gameName());
    }

    @Test
    void updateGameNegative() {
        Assertions.assertThrows(DataAccessException.class,
                () -> gameDAO.updateGame(new GameData(9999, null, null, "bad", new ChessGame())));
    }

    @Test
    void clearPositive() throws DataAccessException {
        gameDAO.createGame("g6");
        gameDAO.clear();
        Assertions.assertTrue(gameDAO.listGames().isEmpty());
    }
}