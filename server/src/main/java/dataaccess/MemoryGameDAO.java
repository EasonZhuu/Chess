package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MemoryGameDAO implements GameDAO{
    private Map<Integer, GameData> games = new HashMap<>();
    private int nextGameID = 1;

    @Override
    public int createGame(String gameName) throws DataAccessException{
        if (gameName == null || gameName.isBlank()){
            throw new DataAccessException("Error: bad request");
        }

        int gameID = nextGameID++;
        GameData gameData = new GameData(gameID, null, null, gameName, new ChessGame());
        games.put(gameID, gameData);
        return gameID;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException{
        return games.get(gameID);
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException{
        return new ArrayList<>(games.values());
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException{
        if (game == null || game.gameID() < 1){
            throw new DataAccessException("Error: bad request");
        }
        if (!games.containsKey(game.gameID())){
            throw new DataAccessException("Error: bad request");
        }

        games.put(game.gameID(), game);
    }

    @Override
    public void clear() throws DataAccessException{
        games.clear();
        nextGameID = 1;
    }
}
