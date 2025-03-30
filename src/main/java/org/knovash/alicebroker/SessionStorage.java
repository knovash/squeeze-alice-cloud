// SessionStorage.java
package org.knovash.alicebroker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionStorage {

    private static final Map<String, String> states = new ConcurrentHashMap<>();
    private static final Map<String, TokenInfo> tokens = new ConcurrentHashMap<>();

    public static void saveState(String state) {
        states.put(state, "valid");
    }

    public static boolean validateState(String state) {
        return states.remove(state) != null;
    }

    public static void saveToken(String userId, TokenInfo token) {
        tokens.put(userId, token);
    }

    public static TokenInfo getToken(String userId) {
        return tokens.get(userId);
    }

    public static class TokenInfo {
        private final String accessToken;
        private final String refreshToken;
        private final long expiresIn;

        public TokenInfo(String accessToken, String refreshToken, long expiresIn) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.expiresIn = expiresIn;
        }

        // Getters
    }
}