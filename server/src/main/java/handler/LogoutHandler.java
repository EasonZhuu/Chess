package handler;

import server.ErrorResponse;
import service.LogoutRequest;
import service.LogoutResult;
import service.ServiceException;
import service.UserService;
import io.javalin.http.Context;

public class LogoutHandler {
    private final UserService userService;

    public LogoutHandler(UserService userService){
        this.userService = userService;
    }

    public void logout(Context ctx){
        try{
            String authToken = ctx.header("authorization");
            LogoutRequest request = new LogoutRequest(authToken);
            LogoutResult result = userService.logout(request);
            ctx.status(200);
            ctx.json(result);
        } catch (ServiceException ex){
            ctx.status(ex.statusCode());
            ctx.json(new ErrorResponse(ex.getMessage()));
        }
    }
}

