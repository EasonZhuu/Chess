package dataaccess;

import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MySqlUserDAOTests {
    private MySqlUserDAO userDAO;

    @BeforeEach
    void setUp() throws DataAccessException {
        MySqlDatabaseInitializer.configureDatabase();
        userDAO = new MySqlUserDAO();
        userDAO.clear();
    }

    @Test
    void createUserPositive() throws DataAccessException {
        userDAO.createUser(new UserData("u1", "p1", "e1@gmail.com"));
        Assertions.assertNotNull(userDAO.getUser("u1"));
    }

    @Test
    void createUserNegative() throws DataAccessException {
        userDAO.createUser(new UserData("u1", "p1", "e1@gmail.com"));
        Assertions.assertThrows(DataAccessException.class, () -> userDAO.createUser(new UserData("u1", "p2", "e2@gmail.com")));
    }

    @Test
    void getUserPositive() throws DataAccessException{
        userDAO.createUser(new UserData("u2", "p2", "e2@gmail.com"));
        Assertions.assertEquals("u2", userDAO.getUser("u2").username());
    }

    @Test
    void getUserNegative() throws DataAccessException {
        userDAO.createUser(new UserData("u2", "p2", "e2@gmail.com"));
        Assertions.assertNull(userDAO.getUser("u3"));
    }

    @Test
    void clearPositive() throws DataAccessException {
        userDAO.createUser(new UserData("u2", "p2", "e2@gmail.com"));
        userDAO.clear();
        Assertions.assertNull(userDAO.getUser("u2"));
    }
}