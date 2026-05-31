package dataaccess;

import model.UserData;

import java.sql.SQLException;

public class MySqlUserDAO implements UserDAO{

    @Override
    public void createUser(UserData user) throws DataAccessException {

    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return null;
    }

    @Override
    public void clear() throws DataAccessException {
        var statement = "TRUNCATE TABLE users";
        try (var conn = DatabaseManager.getConnection();
            var preparedStatement = conn.prepareStatement(statement)){
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to clear users database", ex);
        }
    }
}
