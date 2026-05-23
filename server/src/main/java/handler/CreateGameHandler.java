package handler;

import server.ErrorResponse;
import service.CreateGameRequest;
import service.CreateGameResult;
import service.GameService;
import io.javalin.http.Context;
import service.ServiceException;

public class CreateGameHandler {
    private final GameService gameService;

    public CreateGameHandler(GameService gameService){
        this.gameService = gameService;
    }

    private record Body(String gameName) {
    }

    public void createGame(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            Body body = ctx.bodyAsClass(Body.class);
            CreateGameRequest request = new CreateGameRequest(authToken, body.gameName());

            CreateGameResult result = gameService.createGame(request);
            ctx.status(200);
            ctx.json(result);
        } catch (ServiceException ex) {
            ctx.status(ex.statusCode());
            ctx.json(new ErrorResponse(ex.getMessage()));
        }
    }
}
