package org.knovash.alicebroker;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AuthServiceClient {

    private static final String AUTH_SERVICE_URL = "https://your-auth-service.com/api/validate";

    public String validateTokenAndGetUserId(String token) throws AuthException {
        HttpURLConnection connection = null;
        try {
            // 1. Создаем подключение
            URL url = new URL(AUTH_SERVICE_URL);
            connection = (HttpURLConnection) url.openConnection();

            // 2. Настраиваем запрос
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000); // 5 секунд таймаут
            connection.setReadTimeout(5000);

            // 3. Отправляем тело запроса
            try (OutputStream os = connection.getOutputStream()) {
                String requestBody = new JSONObject().put("token", token).toString();
                os.write(requestBody.getBytes(StandardCharsets.UTF_8));
            }

            // 4. Проверяем статус ответа
            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                throw new AuthException("Auth service error: " + status);
            }

            // 5. Читаем ответ
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }

                JSONObject jsonResponse = new JSONObject(response.toString());
                if (!jsonResponse.optBoolean("valid", false)) {
                    throw new AuthException("Invalid token");
                }
                return jsonResponse.getString("userId");
            }
        } catch (IOException e) {
            throw new AuthException("Auth service communication error", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static class AuthException extends Exception {
        public AuthException(String message) { super(message); }
        public AuthException(String message, Throwable cause) { super(message, cause); }
    }
}