package dataaccess;

import model.AuthData;

import java.sql.SQLException;

public class MySqlAuthDAO implements AuthDAO{
    @Override
    public void createAuth(AuthData authData) throws DataAccessException {
        if (authData == null || authData.authToken() == null || authData.username() == null || authData.authToken().isBlank() || authData.username().isBlank()){
            throw new DataAccessException("Error: bad request");
        }

        var statement = "INSERT INTO auth_tokens (auth_token, username) VALUES (?, ?)";
        try (var conn = DatabaseManager.getConnection();
            var preparedStatement = conn.prepareStatement(statement)){
            preparedStatement.setString(1, authData.authToken());
            preparedStatement.setString(2, authData.username());
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to create auth token", ex);
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        if (authToken == null || authToken.isBlank()) {
            return null;
        }

        var statement = "SELECT auth_token, username FROM auth_tokens WHERE auth_token = ?";
        try (var conn = DatabaseManager.getConnection();
            var preparedStatement = conn.prepareStatement(statement)){
            preparedStatement.setString(1, authToken);
            try (var rs = preparedStatement.executeQuery()) {
                if (rs.next()){
                    return new AuthData(rs.getString("auth_token"), rs.getString("username"));
                }
            }
            return null;
        }catch (SQLException ex){
            throw new DataAccessException("failed to get auth token", ex);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        if (authToken == null || authToken.isBlank()) {
            throw new DataAccessException("Error: bad request");
        }

        var statement = "DELETE FROM auth_tokens WHERE auth_token = ?";
        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(statement)){
            preparedStatement.setString(1, authToken);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to delete auth token", ex);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        var statement = "DELETE FROM auth_tokens";
        try (var conn = DatabaseManager.getConnection();
            var preparedStatement = conn.prepareStatement(statement)){
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to clear auth tokens database", ex);
        }
    }
}
