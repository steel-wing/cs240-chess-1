package dataAccess;
import model.UserData;

import java.util.HashMap;
import java.util.Map;

public class MemoryUserDAO implements UserDAO {
    private static Map<String, UserData> USER;

    public MemoryUserDAO() {
        if (USER == null) {
            USER = new HashMap<>();
        }
    }

    public UserData getUser(String username) throws DataAccessException {
        if (!USER.containsKey(username)) {
            throw new DataAccessException("No such User");
        }
        return USER.get(username);
    }

    public UserData createUser(String username, UserData data) throws DataAccessException{
        if (USER.get(username) == null) {
            USER.put(username, data);
            return data;
        }
        throw new DataAccessException("User already exists");
    }

    public boolean clear() {
        USER.clear();
        return true;
    }
}

