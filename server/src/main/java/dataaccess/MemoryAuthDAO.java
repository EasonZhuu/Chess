package dataaccess;

import model.AuthData;

import java.util.HashMap;
import java.util.Map;

public class MemoryAuthDAO implements AuthDAO{
    private Map<String, AuthData> authTokens = new HashMap<>();

    @Override
    public void createAuth(AuthData authData) throws DataAccessException{
        if (authData == null || authData.authToken() == null || authData.username() == null){
            throw new DataAccessException("Error: bad request");
        }
        authTokens.put(authData.authToken(), authData);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException{
        if (authToken == null){
            return null;
        }

        return authTokens.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException{
        if (authToken == null){
            throw new DataAccessException("Error: bad request");
        }

        authTokens.remove(authToken);
    }

    @Override
    public void clear() throws DataAccessException{
        authTokens.clear();
    }
}
