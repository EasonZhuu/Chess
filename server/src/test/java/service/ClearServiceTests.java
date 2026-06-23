package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryClearDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.clear.ClearService;

public class ClearServiceTests {
    private UserDAO userDAO;
    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private ClearService clearService;

    @BeforeEach
    void setup() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        var clearDAO = new MemoryClearDAO(userDAO, authDAO, gameDAO);
        clearService = new ClearService(clearDAO);
    }

    @Test
    void clearPositive() throws DataAccessException, ServiceException {
        userDAO.createUser(new UserData("u1", "p1", "u1@test.com"));
        authDAO.createAuth(new AuthData("t1", "u1"));
        gameDAO.createGame("g1");

        clearService.clear();

        Assertions.assertNull(userDAO.getUser("u1"));
        Assertions.assertNull(authDAO.getAuth("t1"));
        Assertions.assertEquals(0, gameDAO.listGames().size());
    }
}
