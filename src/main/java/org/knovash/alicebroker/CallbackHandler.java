// CallbackHandler.java
package org.knovash.alicebroker;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.knovash.alicebroker.Main.CLIENT_ID;
import static org.knovash.alicebroker.Main.CLIENT_SECRET;


@Log4j2
public class CallbackHandler implements HttpHandler {

    private static final String REDIRECT_URI = "https://alice-lms.zeabur.app/callback";
    private static final String TOKEN_URL = "https://oauth.yandex.ru/token";
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        log.info("CALLBACK START");
        Context.contextCreate(exchange);


        Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
        log.info("Callback params: {}", params); // Логирование параметров

        if (!params.containsKey("code") || !params.containsKey("state")) {
            sendError(exchange, 400, "Missing code or state");
            return;
        }

        try {
            String code = params.get("code");
            String state = params.get("state");

            if (!SessionStorage.validateState(state)) {
                throw new SecurityException("Invalid state");
            }
            // 3. Обмен кода на токен
            log.info("3. Обмен кода на токен");
            JSONObject tokens = exchangeCode(code, REDIRECT_URI);
            log.info("JSONObject tokens: " + tokens);
           String token =  tokens.getString("access_token");

//Чтобы авторизация завершалась корректно и приложение Яндекс переходило к поиску устройств,
// вам необходимо вернуть HTML-страницу с JavaScript для закрытия окна авторизации.
//             4. Отправка HTML для закрытия окна
            log.info("4. Отправка HTML для закрытия окна");
            // Используем рекомендованный Яндексом метод закрытия окна
//            String htmlResponse = "<!DOCTYPE html><html>"
//                    + "<head><script src='https://yastatic.net/s3/passport-sdk/autofill/v1/sdk-suggest-token-with-polyfills-latest.js'></script></head>"
//                    + "<body><script>"
//                    + "window.onload = function() {"
//                    + "  YaSendSuggestToken('https://alice-lms.zeabur.app', {access_token: '" + tokens.getString("access_token") + "'});"
//                    + "};"
//                    + "</script></body></html>";

            sendHtmlResponse(exchange,  token);

        } catch (Exception e) {
            JSONObject error = new JSONObject()
                    .put("error", "authorization_error")
                    .put("error_description", e.getMessage());
            sendJsonResponse(exchange, 400, String.valueOf(error));
        }
        log.info("CALLBACK FINISH");
    }

    public static JSONObject exchangeCode(String code, String redirectUri) throws IOException {
        log.info("START EXCHANGE CODE: " + code);
        String params = "grant_type=authorization_code" +
                "&code=" + code +
                "&client_id=" + CLIENT_ID +
                "&client_secret=" + CLIENT_SECRET +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8");
        HttpURLConnection conn = (HttpURLConnection) new URL(TOKEN_URL).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(params.getBytes(StandardCharsets.UTF_8));
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String responseBody = reader.lines().collect(Collectors.joining());
            log.info("Token response: {}", responseBody); // Добавьте логирование
            return new JSONObject(responseBody);
        }
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null) return result;
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            if (pair.length == 2) {
                result.put(pair[0], URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
            }
        }
        return result;
    }

    private void sendJsonResponse(HttpExchange exchange, int code, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(code, json.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(json.getBytes());
        }
    }

    private void sendError(HttpExchange exchange, int code, String message) throws IOException {
        JSONObject error = new JSONObject().put("error", message);
        sendJsonResponse(exchange, code, error.toString());
    }


//    Чтобы авторизация завершалась корректно и приложение Яндекс переходило к поиску устройств, вам необходимо вернуть HTML-страницу с JavaScript для закрытия окна авторизации.
//    private void sendHtmlResponse(HttpExchange exchange, int code, String html) throws IOException {
//        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
//        byte[] responseBytes = html.getBytes(StandardCharsets.UTF_8);
//        exchange.sendResponseHeaders(code, responseBytes.length);
//        try (OutputStream os = exchange.getResponseBody()) {
//            os.write(responseBytes);
//        }
//    }

    // CallbackHandler.java - Исправленная реализация закрытия окна
    private void sendHtmlResponse(HttpExchange exchange, String accessToken) throws IOException {
        String html = "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "  <script src='https://yastatic.net/s3/passport-sdk/autofill/v1/sdk-suggest-token-with-polyfills-latest.js'></script>"
                + "</head>"
                + "<body>"
                + "  <script>"
                + "    window.onload = function() {"
                + "      YaSendSuggestToken('https://alice-lms.zeabur.app', {"
                + "        access_token: '" + accessToken + "',"
                + "        expires_in: 3600,"
                + "        token_type: 'bearer'"
                + "      });"
                + "      setTimeout(function() { window.close(); }, 1000);" // Двойное закрытие
                + "    };"
                + "  </script>"
                + "</body>"
                + "</html>";

        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        byte[] response = html.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
    }
}