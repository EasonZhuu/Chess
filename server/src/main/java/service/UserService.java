package service;

import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import dataaccess.AuthDAO;
import model.AuthData;
import model.UserData;
import java.util.UUID;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO){
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public RegisterResult register(RegisterRequest request) throws ServiceException{
        if (request == null || request.username() == null || request.email() == null || request.password()
            == null || request.username().isBlank() || request.email().isBlank() || request.password().isBlank()){
            throw new ServiceException(400, "Error: bad request");
        }

        try{
            if (userDAO.getUser(request.username()) != null){
                throw new ServiceException(403, "Error: already taken");
            }
            userDAO.createUser(new UserData(request.username(), request.password(), request.email()));

            String authToken = UUID.randomUUID().toString();
            authDAO.createAuth(new AuthData(authToken, request.username()));

            return new RegisterResult(request.username(), authToken);
        } catch (DataAccessException ex){
            throw new ServiceException(500, "Error: " + ex.getMessage());
        }
    }

    public LoginResult login(LoginRequest loginRequest) throws ServiceException{
        if (loginRequest == null || loginRequest.username() == null || loginRequest.password() == null || loginRequest.username().isBlank() || loginRequest.password().isBlank()){
            throw new ServiceException(400, "Error: bad request");
        }

        try{
            UserData userData = userDAO.getUser(loginRequest.username());
            if (userData == null || !userData.password().equals(loginRequest.password())){
                throw new ServiceException(401, "Error: unauthorized");
            }

            String authToken = UUID.randomUUID().toString();
            authDAO.createAuth(new AuthData(authToken, loginRequest.username()));
            
        }
    }
}
