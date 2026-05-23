package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;

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

    public ListGamesResult listGames(ListGamesRequest request) throws ServiceException {
        if (request == null || request.authToken() == null || request.authToken().isBlank()) {
            throw new ServiceException(401, "Error: unauthorized");
        }

        try {
            if (authDAO.getAuth(request.authToken()) == null) {
                throw new ServiceException(401, "Error: unauthorized");
            }

            return new ListGamesResult(gameDAO.listGames());
        } catch (DataAccessException ex) {
            throw new ServiceException(500, "Error: " + ex.getMessage());
        }
    }

    public JoinGameResult joinGame(JoinGameRequest request) throws ServiceException{
        if (request == null || request.authToken() == null || request.authToken().isBlank() || request.playerColor() == null || request.playerColor().isBlank() || request.gameID() == null) {
            throw new ServiceException(400, "Error: bad request");
        }

        String color = request.playerColor().toUpperCase();
        if (!color.equals("WHITE") && !color.equals("BLACK")){
            throw new ServiceException(400, "Error: bad request");
        }

        try{
            if (authDAO.getAuth(request.authToken()) == null){
                throw new ServiceException(401, "Error: unauthorized");
            }

            GameData game = gameDAO.getGame(request.gameID());
            if (game == null){
                throw new ServiceException(400, "Error: bad request");
            }

            if (color.equals("WHITE")){
                if (game.whiteUsername() != null){
                    throw new ServiceException(403, "Error: already taken");
                }
                gameDAO.updateGame(new GameData(game.gameID(),authDAO.getAuth(request.authToken()).username(), game.blackUsername(),game.gameName(),game.game()));
            } else {
                if (color.equals("BLACK")) {
                    if (game.blackUsername() != null) {
                        throw new ServiceException(403, "Error: already taken");
                    }
                    gameDAO.updateGame(new GameData(game.gameID(), game.whiteUsername(), authDAO.getAuth(request.authToken()).username(), game.gameName(), game.game()));
                }
            }
            return new JoinGameResult();
        } catch (DataAccessException ex){
                throw new ServiceException(500, "Error: " + ex.getMessage());
            }


    }
}
