package websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    private final ConcurrentHashMap<Integer, ConcurrentHashMap<String, Session>> connections = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    public void add(Integer gameID, String username, Session session) {
        if (!connections.containsKey(gameID)) {
            connections.put(gameID, new ConcurrentHashMap<>());
        }

        ConcurrentHashMap<String, Session> gameConnections = connections.get(gameID);
        gameConnections.put(username, session);
    }

    public void remove(Integer gameID, String username) {
        ConcurrentHashMap<String, Session> gameConnections = connections.get(gameID);
        if (gameConnections == null) {
            return;
        }

        gameConnections.remove(username);

        if (gameConnections.isEmpty()) {
            connections.remove(gameID);
        }

    }

    public void remove(Session session) {
        for (Integer gameID : connections.keySet()) {
            ConcurrentHashMap<String, Session> gameConnections = connections.get(gameID);

            String usernameToRemove = null;

            for (String username : gameConnections.keySet()) {
                Session savedSession = gameConnections.get(username);

                if (savedSession.equals(session)) {
                    usernameToRemove = username;
                    break;
                }
            }

            if (usernameToRemove != null) {
                gameConnections.remove(usernameToRemove);
            }

            if (gameConnections.isEmpty()) {
                connections.remove(gameID);
            }
        }
    }

    public void send(Session session, Object message) throws IOException {
        if (session.isOpen()) {
            String json = gson.toJson(message);
            session.getRemote().sendString(json);
        }
    }

    public void broadcast(Integer gameID, Object message) throws IOException {
        ConcurrentHashMap<String, Session> gameConnections = connections.get(gameID);
        if (gameConnections == null) {
            return;
        }

        for (String username : gameConnections.keySet()) {
            Session session = gameConnections.get(username);
            send(session, message);
        }
    }

    public void broadcastExcept(Integer gameID, String excludedUsername, Object message) throws IOException {
        ConcurrentHashMap<String, Session> gameConnections = connections.get(gameID);
        if (gameConnections == null) {
            return;
        }

        for (String username : gameConnections.keySet()) {
            if (!username.equals(excludedUsername)) {
                Session session = gameConnections.get(username);
                send(session, message);
            }
        }
    }
}
