package dataaccess;

import model.GameData;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public class MySqlGameDAO implements GameDAO{
    @Override
    public int createGame(String gameName) throws DataAccessException {
        if (gameName == null || gameName.isBlank()){
            throw new DataAccessException("Error: bad request");
        }

        var statement = "INSERT INTO games (white_username, black_username, game_name, game_json) VALUES (?, ?, ?, ?)";
        var json = new com.google.gson.Gson().toJson(new chess.ChessGame());

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(statement, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, null);
            preparedStatement.setString(2, null);
            preparedStatement.setString(3, gameName);
            preparedStatement.setString(4, json);
            preparedStatement.executeUpdate();

            try (var rs = preparedStatement.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            throw new DataAccessException("failed to create game: missing generated id");
        } catch (SQLException ex) {
            throw new DataAccessException("failed to create game", ex);
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        if (gameID < 1) {
            return null;
        }

        var statement = "SELECT game_id, white_username, black_username, game_name, game_json FROM games WHERE game_id = ?";
        try (var conn = DatabaseManager.getConnection();
            var preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.setInt(1, gameID);
            try (var rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    var json = rs.getString("game_json");
                    var game = new com.google.gson.Gson().fromJson(json, chess.ChessGame.class);

                    return new GameData(
                            rs.getInt("game_id"),
                            rs.getString("white_username"),
                            rs.getString("black_username"),
                            rs.getString("game_name"),
                            game
                    );
                }
            }
            return null;
        } catch (SQLException ex) {
            throw new DataAccessException("failed to get game", ex);
        }
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        var statement = "SELECT game_id, white_username, black_username, game_name, game_json FROM games";
        var games = new java.util.ArrayList<GameData>();

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(statement);
             var rs = preparedStatement.executeQuery()) {
            while (rs.next()) {
                var json = rs.getString("game_json");
                var game = new com.google.gson.Gson().fromJson(json, chess.ChessGame.class);
                games.add(new GameData(
                        rs.getInt("game_id"),
                        rs.getString("white_username"),
                        rs.getString("black_username"),
                        rs.getString("game_name"),
                        game));
            }
            return games;
        } catch (SQLException ex) {
            throw new DataAccessException("failed to list games", ex);
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (game == null || game.gameID() < 1) {
            throw new DataAccessException("Error: bad request");
        }

        var statement = """
                UPDATE games
                SET white_username = ?, black_username = ?, game_name = ?, game_json = ?
                WHERE game_id = ?
                """;

        var json = new com.google.gson.Gson().toJson(game.game());

        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.setString(1, game.whiteUsername());
            preparedStatement.setString(2, game.blackUsername());
            preparedStatement.setString(3, game.gameName());
            preparedStatement.setString(4, json);
            preparedStatement.setInt(5, game.gameID());

            var rows = preparedStatement.executeUpdate();
            if (rows == 0) {
                throw new DataAccessException("Error: bad request");
            }
        } catch (SQLException ex){
            throw new DataAccessException("failed to update game", ex);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        var statement = "DELETE FROM games";
        try (var conn = DatabaseManager.getConnection();
             var preparedStatement = conn.prepareStatement(statement)){
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to clear games database", ex);
        }
    }
}
