package handler;

import io.javalin.http.Context;
import server.ErrorResponse;
import service.GameService;
import service.JoinGameRequest;
import service.JoinGameResult;
import service.ServiceException;

public class JoinGameHandler {
    private final GameService gameService;

    public JoinGameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    private record Body(String playerColor, Integer gameID) {
    }

    public void joinGame(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            Body body = ctx.bodyAsClass(Body.class);

            JoinGameRequest request = new JoinGameRequest(
                    authToken,
                    body.playerColor(),
                    body.gameID()
            );

            JoinGameResult result = gameService.joinGame(request);
            ctx.status(200);
            ctx.json(result);
        } catch (ServiceException ex) {
            ctx.status(ex.statusCode());
            ctx.json(new ErrorResponse(ex.getMessage()));
        }
    }
}