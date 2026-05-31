package dataaccess;

import model.UserData;

import java.sql.SQLException;

public class MySqlUserDAO implements UserDAO{

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (user == null || user.username() == null || user.password() == null || user.email() == null || user.username().isBlank() || user.password().isBlank() || user.email().isBlank()){
            throw new DataAccessException("Error: bad request");
        }

        var statement = "INSERT INTO users (username, password_hash, email) VALUES (?, ?, ?)";
        var hashedPassword = org.mindrot.jbcrypt.BCrypt.hashpw(user.password(), org.mindrot.jbcrypt.BCrypt.gensalt());
        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(statement)){
            preparedStatement.setString(1, user.username());
            preparedStatement.setString(2, hashedPassword);
            preparedStatement.setString(3, user.email());
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            if ("23000".equals(ex.getSQLState())){
                throw new DataAccessException("Error: already taken");
            }
            throw new DataAccessException("failed to create user", ex);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        if (username == null || username.isBlank()) {
            return null;
        }

        var statement = "SELECT username, password_hash, email FROM users WHERE username = ?";
        try (var conn = DatabaseManager.getConnection();
            var preparedStatement = conn.prepareStatement(statement)){
            preparedStatement.setString(1, username);
            try (var rs = preparedStatement.executeQuery()) {
                if (rs.next()){
                    return new UserData(rs.getString("username"),
                                        rs.getString("password_hash"),
                                        rs.getString("email"));
                }
            }
            return null;
        } catch (SQLException ex){
            throw new DataAccessException("failed to get user", ex);
        }
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
