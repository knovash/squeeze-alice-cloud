package org.knovash.alicebroker;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import static org.knovash.alicebroker.Main.CLIENT_ID;

@Log4j2
public class AuthHandler implements HttpHandler {

    private static final String REDIRECT_URI = "https://alice-lms.zeabur.app/callback";
    private static final String SCOPE = "home:lights";
    private static final SecureRandom secureRandom = new SecureRandom();

    @Override
    public void handle(HttpExchange exchange) {
        log.info("AUTH HANDLER START >>>");
        try {
            // 1. Генерация и сохранение state
            String state = generateState();
            SessionStorage.saveState(state);
            log.info("Generated state: {}", state);

            // 2. Подготовка параметров
            String encodedRedirectUri = URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8);
            String encodedScope = URLEncoder.encode(SCOPE, StandardCharsets.UTF_8);
            String encodedState = URLEncoder.encode(state, StandardCharsets.UTF_8);

            // 3. Формирование URL
            String authUrl = "https://oauth.yandex.ru/authorize?" +
                    "response_type=code" +
                    "&client_id=" + CLIENT_ID +
                    "&redirect_uri=" + encodedRedirectUri +
//                    "&scope=" + encodedScope +
//                    "&scope=" + "home:lights" + // invalid scope
                    "&scope=" + "iot:control" +
                    "&state=" + encodedState +
                    "&force_confirm=true";
            log.info("AUTH URL: " + authUrl);

            // 4. Отправка редиректа
            exchange.getResponseHeaders().add("Location", authUrl);
            exchange.sendResponseHeaders(302, -1);
        } catch (Exception e) {
            log.error("Auth error: {}", e.getMessage());
        }
        log.info("AUTH HANDLER FINISH <<<");
    }

    private String generateState() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}