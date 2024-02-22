package dataAccess;

import model.UserData;
import model.AuthData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MemoryAuthDAO implements AuthDAO {
    private static Map<String, AuthData> AUTH;
    public MemoryAuthDAO() {
        if (AUTH == null) {
            AUTH = new HashMap<>();
        }
    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        AuthData found = AUTH.get(authToken);
        if (found == null) {
            throw new DataAccessException("No such AuthToken");
        }
        return found;
    }

    public AuthData createAuth(UserData user) {
        String authToken = UUID.randomUUID().toString();
        AuthData data = new AuthData(authToken, user.username());
        AUTH.put(authToken, data);
        return data;
    }

    public boolean deleteAuth(String authToken) throws DataAccessException{
        if (AUTH.get(authToken) == null) {
             throw new DataAccessException("No such AuthToken");
        }
        AUTH.remove(authToken);
        return true;
    }

    public boolean clear() {
        AUTH.clear();
        return true;
    }
}
