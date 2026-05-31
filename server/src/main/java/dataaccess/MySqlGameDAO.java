package dataaccess;

import model.GameData;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public class MySqlGameDAO implements GameDAO{
    @Override
    public int createGame(String gameName) throws DataAccessException {
        return 0;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return null;
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return List.of();
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {

    }

    @Override
    public void clear() throws DataAccessException {
        var statement = "TRUNCATE TABLE games";
        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(statement)){
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to clear games database", ex);
        }
    }
}
