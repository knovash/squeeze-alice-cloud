package org.knovash.alicebroker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class CloudServerSpoty {

    private static final String CLIENT_ID = "f45a18e2bcfe456dbd9e7b73e74514af";
    private static final String CLIENT_SECRET = "5c3321b4ae7e43ab93a2ce4ec1b4cf48";
    private static final String REDIRECT_URI = "https://alice-lms.zeabur.app/spoti_callback";
    private static final String SPOTIFY_TOKEN_URI = "https://accounts.spotify.com/api/token";
    private static final String SPOTIFY_AUTH_URI = "https://accounts.spotify.com/authorize";


    public static String scope = // spoty
            "app-remote-control " +
                    "user-read-private " +
                    "user-read-email " +
                    "user-read-playback-state " +
                    "user-read-currently-playing " +
                    "user-read-private " +
                    "user-modify-playback-state";

    private static final ConcurrentHashMap<String, String> tokenStore = new ConcurrentHashMap<>();


    //   для запроса от local и возврата токена в callbackTopic
    static class AuthorizeHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            log.info("");
            log.info("AUTHORIZE START >>>>>>");
            try {
                URI requestUri = exchange.getRequestURI();
                String query = requestUri.getQuery();
                String state = getParam(query, "state");
                String authUrl = SPOTIFY_AUTH_URI + "?" +
                        "response_type=" + "code" + "&" +    // Required
                        "client_id=" + CLIENT_ID + "&" +            // Required
                        "scope=" + URLEncoder.encode(scope, StandardCharsets.UTF_8) + "&" +  // Optional
                        "redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8) + "&" +      // Required
//                        "&force_confirm=true" +
                        "state=" + state + "&" +                    // Optional
                        "";

                log.info("AUTH URL: " + authUrl);

                exchange.getResponseHeaders().add("Location", authUrl);
                exchange.sendResponseHeaders(302, -1);
            } catch (Exception e) {
                sendError(exchange, 400, "Invalid request");
            }

            log.info("AUTHORIZE FINISH >>>>>>");
            log.info("");
        }
    }

    //   для запроса от local и возврата токена в callbackTopic
    static class CallbackHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            log.info("CALBACK START >>>>>>");
            try (OutputStream os = exchange.getResponseBody()) {
                URI requestUri = exchange.getRequestURI();
                String query = requestUri.getQuery();

                String code = getParam(query, "code");
                String state = getParam(query, "state");
                log.info("CALBACK code: " + code);
                log.info("CALBACK state: " + state);


                // Обмен кода на токен
                String tokenResponse = exchangeCodeForToken(code);
                log.info("CALBACK TOKEN RESPONSE: " + tokenResponse);

                // Сохраняем токен в хранилище
                String accessToken = extractToken(tokenResponse);
                log.info("CALBACK STORE accessToken: " + accessToken);


                log.info("PUBLISH CALLBACK TOKEN" + Hive.callbackTopic);
                log.info("PUBLISH CALLBACK TOKEN" + Hive.state);
                Hive.publishCallbackToken(accessToken, "spotify_callback_token");


                tokenStore.put(state, accessToken);

                log.info("CALBACK SEND RESPONSE 200 Authorization successful! You can close this page.");
                sendResponse(exchange, 200, "Authorization successful! You can close this page.");

            } catch (Exception e) {
                log.info("CALBACK SEND RESPONSE ERROR 500 Token exchange failed");
                sendError(exchange, 500, "Token exchange failed");
            }
            log.info("CALBACK FINISH >>>>>>");
        }
    }

    //   для запроса от local и возврата токена в callbackTopic
    static class TokenHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            log.info("START");
            try {
                URI requestUri = exchange.getRequestURI();
                String query = requestUri.getQuery();
                String state = getParam(query, "state");

//                String token = tokenStore.getOrDefault(state, "");
//                log.info("token: " + token);
                String tokens = tokenStore.getOrDefault(state, "");
                String[] parts = tokens.split(":");
                String accessToken = parts.length > 0 ? parts[0] : "";

                String jsonResponse = "{\"access_token\": \"" + accessToken + "\"}";
//                String jsonResponse = "{\"token\": \"" + token + "\"}";
                log.info("jsonResponse: " + jsonResponse);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jsonResponse.getBytes().length);

                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(jsonResponse.getBytes());
                }

                // Очистка токена после получения
                tokenStore.remove(state);
            } catch (Exception e) {
                sendError(exchange, 404, "Token not found");
            }
        }
    }


    private static String exchangeCodeForToken(String code) throws IOException, InterruptedException {
        log.info("START EXCHANGE CODE: " + code);
        HttpClient client = HttpClient.newHttpClient();

        String authHeader = "Basic " + Base64.getEncoder().encodeToString(
                (CLIENT_ID + ":" + CLIENT_SECRET).getBytes());

        String requestBody = "grant_type=authorization_code" +
                "&code=" + code +
                "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SPOTIFY_TOKEN_URI))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", authHeader)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Spotify token error: " + response.body());
        }

        return response.body();
    }

    private static String extractToken(String jsonResponse) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonResponse);
        return rootNode.path("access_token").asText();
    }

    private static String extractRefreshToken(String jsonResponse) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonResponse);
        return rootNode.path("refresh_token").asText();
    }

    private static String getParam(String query, String paramName) {
        log.info("START GET PARAM: " + paramName + " QUERY: " + query);
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue[0].equals(paramName)) {
                return keyValue[1];
            }
        }
        throw new IllegalArgumentException("Parameter not found: " + paramName);
    }

    private static void sendResponse(HttpExchange exchange, int code, String message) throws IOException {
        log.info("START SEND RESPONSE");
        log.info("CODE: " + code);
        log.info("MESSAGE: " + message);
        exchange.sendResponseHeaders(code, message.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(message.getBytes());
            log.info("WRITE OK");
        }
    }

    private static void sendError(HttpExchange exchange, int code, String message) throws IOException {
        log.info("ERROR");
        exchange.sendResponseHeaders(code, message.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(message.getBytes());
        }
    }
}