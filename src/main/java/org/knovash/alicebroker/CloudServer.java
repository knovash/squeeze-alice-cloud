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
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
public class CloudServer {

    private static final String CLIENT_ID = "9aa97fffe29849bb945db5b82b3ee015";
    private static final String CLIENT_SECRET = "37cf34e9fdbd48d389e293fc96d5e794";
    private static final String REDIRECT_URI = "https://alice-lms.zeabur.app/callback";

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

                String authUrl = "https://oauth.yandex.ru/authorize" +
                        "?response_type=code" +
                        "&client_id=" + CLIENT_ID +
                        "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8) +
                        "&force_confirm=true" +
                        "&state=" + state;

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
                Hive.publishCallbackToken(accessToken, "yandex_callback_token");


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

                String token = tokenStore.getOrDefault(state, "");
                log.info("token: " + token);


                String jsonResponse = "{\"token\": \"" + token + "\"}";
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

        String requestBody = "grant_type=authorization_code" +
                "&code=" + code +
                "&client_id=" + CLIENT_ID +
                "&client_secret=" + CLIENT_SECRET;
        log.info("REQUEST BODY: " + requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://oauth.yandex.ru/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
        log.info("TRY GET RESPONSE...");
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        log.info("RESPONSE: " + response);
        return response.body();
    }

    private static String extractToken(String jsonResponse) throws IOException {
        log.info("START EXTRACT TOKEN");
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonResponse);
        String token = rootNode.path("access_token").asText();
        log.info("RESULT: " + token);
        return token;
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