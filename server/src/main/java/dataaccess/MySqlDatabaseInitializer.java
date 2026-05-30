package dataaccess;

import java.sql.Connection;
import java.sql.SQLException;

public class MySqlDatabaseInitializer {
    public static void configureDatabase() throws DataAccessException{
        DatabaseManager.createDatabase();

        var createUserTable = """
                CREATE TABLE IF NOT EXISTS users (
                    username VARCHAR(255) NOT NULL,
                    password_hash VARCHAR(255) NOT NULL,
                    email VARCHAR(255) NOT NULL,
                    PRIMARY KEY (username)
                    )
                """;

        var createAuthTokenTable = """
                CREATE TABLE IF NOT EXISTS auth_tokens (
                    auth_token VARCHAR(255) NOT NULL,
                    username VARCHAR(255) NOT NULL,
                    PRIMARY KEY (auth_token),
                    INDEX idx_auth_username (username),
                    CONSTRAINT fk_auth_username
                        FOREIGN KEY (username)
                        REFERENCES users(username)
                        ON DELETE CASCADE
                        )
                """;

        var createGamesTable = """
                CREATE TABLE IF NOT EXISTS games (
                    game_id INT NOT NULL AUTO_INCREMENT,
                    white_username VARCHAR(255) NULL,
                    black_username VARCHAR(255) NULL,
                    game_name VARCHAR(255) NOT NULL,
                    game_json TEXT NOT NULL,
                    PRIMARY KEY (game_id),
                    INDEX idx_white_username (white_username),
                    INDEX idx_black_username (black_username),
                    CONSTRAINT fk_games_white
                        FOREIGN KEY (white_username)
                        REFERENCES users(username)
                        ON DELETE SET NULL,
                    CONSTRAINT fk_games_black
                        FOREIGN KEY (black_username)
                        REFERENCES users(username)
                        ON DELETE SET NULL
                    )
                """;

        try (var conn = DatabaseManager.getConnection()){
            executeStatement(conn, createUserTable);
            executeStatement(conn, createAuthTokenTable);
            executeStatement(conn, createGamesTable);
        } catch (SQLException ex){
            throw new DataAccessException("failed to configure database tables", ex);
        }
    }

    private static void executeStatement(Connection conn, String sql) throws SQLException{
        try (var statement = conn.prepareStatement(sql)){
            statement.executeUpdate();
        }
    }
}
