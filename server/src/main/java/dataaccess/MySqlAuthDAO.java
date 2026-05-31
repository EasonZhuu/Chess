package dataaccess;

import model.AuthData;

import java.sql.SQLException;

public class MySqlAuthDAO implements AuthDAO{
    @Override
    public void createAuth(AuthData authData) throws DataAccessException {

    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {

    }

    @Override
    public void clear() throws DataAccessException {
        var statement = "TRUNCATE TABLE auth_tokens";
        try (var conn = DatabaseManager.getConnection();
            var preparedStatement = conn.prepareStatement(statement)){
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to clear auth tokens database", ex);
        }
    }
}
