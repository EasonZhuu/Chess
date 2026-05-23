package handler;

import io.javalin.http.Context;
import server.ErrorResponse;
import service.GameService;
import service.ListGamesRequest;
import service.ListGamesResult;
import service.ServiceException;

public class ListGamesHandler {
    private final GameService gameService;

    public ListGamesHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public void listGames(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            ListGamesRequest request = new ListGamesRequest(authToken);

            ListGamesResult result = gameService.listGames(request);
            ctx.status(200);
            ctx.json(result);
        } catch (ServiceException ex) {
            ctx.status(ex.statusCode());
            ctx.json(new ErrorResponse(ex.getMessage()));
        }
    }
}