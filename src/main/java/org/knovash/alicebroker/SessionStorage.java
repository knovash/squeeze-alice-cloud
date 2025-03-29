package org.knovash.alicebroker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionStorage {
    private static final Map<String, String> states = new ConcurrentHashMap<>();

    public static void saveState(String state) {
        states.put(state, "valid");
    }

    public static boolean validateState(String state) {
        return states.remove(state) != null;
    }
}