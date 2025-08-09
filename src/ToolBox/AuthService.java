package ToolBox;

import java.util.HashMap;
import java.util.Map;

public class AuthService {

    private static final Map<String, String> USERS = new HashMap<>();

    static {
        USERS.put("user1", "12345");
        USERS.put("user2", "54321");
    }

    public static boolean authenticate(String username, String phoneNumber) {
        String stored = USERS.get(username);
        return stored != null && stored.equals(phoneNumber);
    }
}
