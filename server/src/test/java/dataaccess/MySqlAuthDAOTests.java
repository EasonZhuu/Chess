package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MySqlAuthDAOTests {
    private MySqlAuthDAO authDAO;
    private MySqlUserDAO userDAO;

    @BeforeEach
    void setUp() throws DataAccessException {
        MySqlDatabaseInitializer.configureDatabase();
        authDAO = new MySqlAuthDAO();
        userDAO = new MySqlUserDAO();
        authDAO.clear();
        userDAO.clear();
        userDAO.createUser(new UserData("u1", "p1", "u1@test.com"));
    }

    @Test
    void createAuthPositive() throws DataAccessException {
        authDAO.createAuth(new AuthData("t1", "u1"));
        Assertions.assertNotNull(authDAO.getAuth("t1"));
    }

    @Test
    void createAuthNegative() {
        Assertions.assertThrows(DataAccessException.class,
                () -> authDAO.createAuth(new AuthData("t2", "missing-user")));
    }

    @Test
    void getAuthPositive() throws DataAccessException {
        authDAO.createAuth(new AuthData("t3", "u1"));
        Assertions.assertEquals("u1", authDAO.getAuth("t3").username());
    }

    @Test
    void getAuthNegative() throws DataAccessException {
        Assertions.assertNull(authDAO.getAuth("missing-token"));
    }

    @Test
    void deleteAuthPositive() throws DataAccessException {
        authDAO.createAuth(new AuthData("t4", "u1"));
        authDAO.deleteAuth("t4");
        Assertions.assertNull(authDAO.getAuth("t4"));
    }

    @Test
    void deleteAuthNegative() {
        Assertions.assertThrows(DataAccessException.class,
                () -> authDAO.deleteAuth(" "));
    }

    @Test
    void clearPositive() throws DataAccessException {
        authDAO.createAuth(new AuthData("t5", "u1"));
        authDAO.clear();
        Assertions.assertNull(authDAO.getAuth("t5"));
    }
}