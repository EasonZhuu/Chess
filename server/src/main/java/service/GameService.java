package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;

public class GameService {
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public GameService(AuthDAO authDAO, GameDAO gameDAO){
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public CreateGameResult createGame(CreateGameRequest request)throws ServiceException{
        if (request == null || request.authToken() == null || request.authToken().isBlank() || request.gameName() == null || request.gameName().isBlank()){
            throw new ServiceException(400, "Error: bad request");
        }

        try{
            if (authDAO.getAuth(request.authToken()) == null){
                throw new ServiceException(401, "Error: unauthorized");
            }

            int gameID = gameDAO.createGame(request.gameName());
            return new CreateGameResult(gameID);
        } catch (DataAccessException ex){
            throw new ServiceException(500, "Error: " + ex.getMessage());
        }
    }
}
