package handler;

import java.util.Map;

import io.javalin.http.Context;
import server.ErrorResponse;
import service.ServiceException;
import service.clear.ClearService;

public class ClearHandler {
    private final ClearService clearService;

    public ClearHandler(ClearService clearService){
        this.clearService = clearService;
    }

    public void clear(Context ctx){
        try{
            clearService.clear();
            ctx.status(200);
            ctx.json(Map.of());
        } catch (ServiceException ex){
            ctx.status(ex.statusCode());
            ctx.json(new ErrorResponse(ex.getMessage()));
        }
    }
}
