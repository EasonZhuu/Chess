package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UserServiceTests {
    private UserDAO userDAO;
    private AuthDAO authDAO;
    private UserService userService;

    @BeforeEach
    void setup() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        userService = new UserService(userDAO, authDAO);
    }

    @Test
    void registerPositive() throws ServiceException, DataAccessException {
        var request = new RegisterRequest("u1", "p1", "u1@test.com");

        var result = userService.register(request);

        Assertions.assertEquals("u1", result.username());
        Assertions.assertNotNull(result.authToken());
        Assertions.assertEquals("u1", userDAO.getUser("u1").username());
        Assertions.assertEquals("u1", authDAO.getAuth(result.authToken()).username());
    }

    @Test
    void registerNegativeAlreadyTaken() throws DataAccessException {
        userDAO.createUser(new UserData("u1", "p1", "u1@test.com"));
        var request = new RegisterRequest("u1", "newPass", "new@test.com");

        var ex = Assertions.assertThrows(ServiceException.class, () -> userService.register(request));

        Assertions.assertEquals(403, ex.statusCode());
        Assertions.assertEquals("Error: already taken", ex.getMessage());
    }

    @Test
    void loginPositive() throws DataAccessException, ServiceException {
        userDAO.createUser(new UserData("u1", "p1", "u1@test.com"));

        var result = userService.login(new LoginRequest("u1", "p1"));

        Assertions.assertEquals("u1", result.username());
        Assertions.assertNotNull(result.authToken());
        Assertions.assertEquals("u1", authDAO.getAuth(result.authToken()).username());
    }

    @Test
    void loginNegativeUnauthorized() throws DataAccessException {
        userDAO.createUser(new UserData("u1", "p1", "u1@test.com"));

        var ex = Assertions.assertThrows(ServiceException.class,
                () -> userService.login(new LoginRequest("u1", "wrongPassword")));

        Assertions.assertEquals(401, ex.statusCode());
        Assertions.assertEquals("Error: unauthorized", ex.getMessage());
    }

    @Test
    void logoutPositive() throws DataAccessException, ServiceException {
        authDAO.createAuth(new AuthData("token-1", "u1"));

        var result = userService.logout(new LogoutRequest("token-1"));

        Assertions.assertNotNull(result);
        Assertions.assertNull(authDAO.getAuth("token-1"));
    }

    @Test
    void logoutNegativeUnauthorized() {
        var ex = Assertions.assertThrows(ServiceException.class,
                () -> userService.logout(new LogoutRequest("missing-token")));

        Assertions.assertEquals(401, ex.statusCode());
        Assertions.assertEquals("Error: unauthorized", ex.getMessage());
    }
}
