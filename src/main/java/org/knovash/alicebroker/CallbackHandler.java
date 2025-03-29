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
            JSONObject tokens = exchangeCode(code, REDIRECT_URI);

//            // 4. Ответ для Яндекса
//            JSONObject response = new JSONObject()
//                    .put("access_token", tokens.getString("access_token"))
//                    .put("token_type", "bearer")
//                    .put("expires_in", tokens.getInt("expires_in"));
//            sendJsonResponse(exchange, 200, String.valueOf(response));

//Чтобы авторизация завершалась корректно и приложение Яндекс переходило к поиску устройств, вам необходимо вернуть HTML-страницу с JavaScript для закрытия окна авторизации.
            // 4. Отправка HTML для закрытия окна
//            String htmlResponse = "<html><script>"
//                    + "window.opener.postMessage({type:'oauth', token:'" + tokens.getString("access_token") + "'}, '*');"
//                    + "window.close();"
//                    + "</script></html>";

            // Формируем URL с параметрами в fragment
            String redirectUrl = "/success#"
                    + "access_token=" + URLEncoder.encode(tokens.getString("access_token"), StandardCharsets.UTF_8)
                    + "&token_type=bearer"
                    + "&expires_in=" + tokens.getInt("expires_in");

            // Перенаправляем на страницу success с fragment
            exchange.getResponseHeaders().add("Location", redirectUrl);
            exchange.sendResponseHeaders(302, -1);


//            sendHtmlResponse(exchange, 200, htmlResponse);

        } catch (Exception e) {
            JSONObject error = new JSONObject()
                    .put("error", "authorization_error")
                    .put("error_description", e.getMessage());
            sendJsonResponse(exchange, 400, String.valueOf(error));
        }
    }

    public static JSONObject exchangeCode(String code, String redirectUri) throws IOException {
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
    private void sendHtmlResponse(HttpExchange exchange, int code, String html) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        byte[] responseBytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}