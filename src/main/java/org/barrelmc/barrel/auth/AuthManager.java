package org.barrelmc.barrel.auth;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

public class AuthManager {

    private static AuthManager instance;
    @Getter
    private final Map<String, String> accessTokens = new HashMap<>();
    @Getter
    private final Map<String, Boolean> loginPlayers = new ConcurrentHashMap<>();
    @Getter
    private final Map<String, Timer> timers = new ConcurrentHashMap<>();
    @Getter
    private final Live xboxLive;

    public AuthManager() {
        xboxLive = new Live();
    }

    public static AuthManager getInstance() {
        if (instance == null) {
            instance = new AuthManager();
        }
        return instance;
    }
}
