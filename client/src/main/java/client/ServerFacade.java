package client;

import model.AuthData;
import model.GameData;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;


import com.google.gson.Gson;


public class ServerFacade {

    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;
    private final Gson gson = new Gson();

    public ServerFacade(int port) {
        serverUrl = "http://localhost:" + port;
    }

    public void clear() throws ResponseException{
        var request = buildRequest("DELETE", "/db", null, null);
        var response = sendRequest(request);
        handleResponse(response, null);
    }

    public AuthData register(String username, String password, String email) throws ResponseException {
        var body = new RegisterRequest(username, password, email);
        var request = buildRequest("POST", "/user", body, null);
        var response = sendRequest(request);
        return handleResponse(response, AuthData.class);
    }

    public  AuthData login(String username, String password) throws ResponseException {
        var body = new LoginRequest(username, password);
        var request = buildRequest("POST", "/session", body, null);
        var response = sendRequest(request);
        return handleResponse(response, AuthData.class);
    }



    private HttpRequest buildRequest(String method, String path, Object body, String authToken){
        var request = HttpRequest.newBuilder().uri(URI.create(serverUrl + path)).method(method, makeRequestBody(body));

        if (body != null) {
            request.setHeader("Content-Type", "application/json");
        }

        if (authToken != null) {
            request.setHeader("authorization", authToken);
        }
        return request.build();
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws ResponseException{
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new ResponseException("Unable to reach server: " + ex.getMessage());
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws ResponseException {
        if (! isSuccessful(response.statusCode())) {
            var errorResponse = gson.fromJson(response.body(), ErrorResponse.class);
            throw new ResponseException(errorResponse.message());
        }

        if (responseClass == null) {
            return null;
        }

        return gson.fromJson(response.body(), responseClass);
    }

    private boolean isSuccessful(int statusCode) {
        return statusCode / 100 == 2;
    }

    private BodyPublisher makeRequestBody(Object request){
        if (request != null) {
            return BodyPublishers.ofString(gson.toJson(request));
        }
        return BodyPublishers.noBody();
    }











    private record RegisterRequest(
            String username,
            String password,
            String email
    ){}

    private record LoginRequest(
            String username,
            String password
    ){}

    private record CreateGameRequest(
            String gameName
    ){}

    private record JoinGameRequest(
            String playerColor,
            int gameID
    ){}

    private record CreateGameResult(
            int gameID
    ){}

    private record ListGamesResult(
            Collection<GameData> games
    ){}

    private record ErrorResponse(
            String message
    ){}
}
