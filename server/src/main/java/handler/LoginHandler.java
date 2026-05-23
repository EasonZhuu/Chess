package handler;

import io.javalin.http.Context;
import server.ErrorResponse;
import service.LoginRequest;
import service.LoginResult;
import service.ServiceException;
import service.UserService;

public class LoginHandler {
    private final UserService userService;

    public LoginHandler(UserService userService) {
        this.userService = userService;
    }

    public void login(Context ctx) {
        try {
            LoginRequest request = ctx.bodyAsClass(LoginRequest.class);
            LoginResult result = userService.login(request);
            ctx.status(200);
            ctx.json(result);
        } catch (ServiceException ex) {
            ctx.status(ex.statusCode());
            ctx.json(new ErrorResponse(ex.getMessage()));
        }
    }
}
