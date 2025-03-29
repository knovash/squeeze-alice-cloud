package org.knovash.alicebroker;

import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;

@Log4j2
public class UsersFix {

    private final List<User> userList = new ArrayList<>();

    public void saveUser(String id, String accessToken, String refreshToken) {
        userList.removeIf(u -> u.id.equals(id));
        userList.add(new User(id, accessToken, refreshToken));
    }

    public boolean exists(String userId) {
        return userList.stream().anyMatch(u -> u.id.equals(userId));
    }

    public void write() {
        // Реализация записи в файл/БД
        try {
            log.info("Users data saved");
        } catch (Exception e) {
            log.error("Save error: {}", e.getMessage());
        }
    }

    public void printAll() {
        StringBuilder sb = new StringBuilder("\nCurrent users (" + userList.size() + "):\n");
        sb.append("----------------------------------------\n");
        for (User user : userList) {
            sb.append(String.format("| ID: %s | Access: %s | Refresh: %s |\n",
                    user.id,
                    maskToken(user.accessToken),
                    maskToken(user.refreshToken)));
        }
        sb.append("----------------------------------------");
        log.info(sb.toString());
    }

    private String maskToken(String token) {
        if (token == null || token.length() < 8) return "****";
        return token.substring(0, 4) + "****" + token.substring(token.length() - 4);
    }

    public User getUserByAccessToken(String token) {
        User user = userList.stream()
                .peek(u -> log.info("USER TOKEN: " + u.accessToken + " ID: " + u.id))
                .filter(u -> u.accessToken.equals(token))
                .findFirst()
                .orElseGet(null);
        return user;
    }

    public static class User {

        String id;
        String accessToken;
        String refreshToken;

        public User(String id, String accessToken, String refreshToken) {
            this.id = id;
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }
}