package handler;

import io.javalin.http.Context;
import service.RegisterRequest;
import service.RegisterResult;
import service.ServiceException;
import service.UserService;
import server.ErrorResponse;

public class RegisterHandler {
    private UserService userService;

    public RegisterHandler(UserService userService){
        this.userService = userService;
    }

    public void register(Context ctx){
        try{
            RegisterRequest request = ctx.bodyAsClass(RegisterRequest.class);
            RegisterResult result = userService.register(request);
            ctx.status(200);
            ctx.json(result);
        } catch (ServiceException ex){
            ctx.status(ex.statusCode());
            ctx.json(new ErrorResponse(ex.getMessage()));
        }
    }
}
