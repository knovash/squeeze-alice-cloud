package org.knovash.alicebroker;

import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
@Log4j2
public class UtilsToken {

    public static String maskToken(String token) {
        if (token == null) {
            return null;
        }

        int length = token.length();

        // Для токенов короче 20 символов возвращаем как есть
        if (length <= 20) {
            return token;
        }

        // Рассчитываем длину маскируемой части
        int keep = 10;
        String head = token.substring(0, keep);
        String tail = token.substring(length - keep);
        char[] stars = new char[length - 2 * keep];
        Arrays.fill(stars, '*');

        return head + new String(stars) + tail;
    }

    public static String exchangeCodeForToken(String code, String clientId, String clientSecret) throws IOException {
        log.info("EXCHANGE CODE FOR TOKEN code: " + code);
        String params = String.format(
                "grant_type=authorization_code&code=%s&client_id=%s&client_secret=%s",
                URLEncoder.encode(code, StandardCharsets.UTF_8),
                URLEncoder.encode(clientId, StandardCharsets.UTF_8),
                URLEncoder.encode(clientSecret, StandardCharsets.UTF_8)
        );

        URL url = new URL("https://oauth.yandex.ru/token");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = params.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            log.info("RESPONSE CODE: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    return response.toString();
                }
            } else {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String errorLine;
                    while ((errorLine = br.readLine()) != null) {
                        errorResponse.append(errorLine.trim());
                    }
                    throw new IOException("Failed to get token. Response code: " + responseCode
                            + ", Error: " + errorResponse.toString());
                }
            }
        } finally {
            connection.disconnect();
        }
    }

    public static String getUserInfo(String accessToken) throws IOException {
        log.info("GET USER INFO BY ACCESS TOKEN: " + accessToken);
        URL url = new URL("https://login.yandex.ru/info?format=json");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "OAuth " + accessToken);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    return response.toString();
                }
            } else {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String errorLine;
                    while ((errorLine = br.readLine()) != null) {
                        errorResponse.append(errorLine.trim());
                    }
                    throw new IOException("Failed to get user info. Response code: " + responseCode
                            + ", Error: " + errorResponse.toString());
                }
            }
        } finally {
            connection.disconnect();
        }
    }

}