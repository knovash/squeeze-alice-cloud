package org.knovash.alicebroker;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.knovash.alicebroker.HandlerWorkAuth.generateRandom;

@Log4j2
public class HandlerWorkCallback implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        log.info("HANDLER CALLBACK START >>>");
        Context context = Context.contextCreate(httpExchange);

//        // Получаем код из запроса
//        String code = context.queryMap.get("code");
//        String clientId = "ваш_client_id"; // Замените на реальный client_id
//        String clientSecret = "ваш_client_secret"; // Замените на реальный client_secret
//
//        // 1. Обмен кода на токен
//        String tokenResponse = UtilsToken.exchangeCodeForToken(code, clientId, clientSecret);
//        JSONObject tokenJson = new JSONObject(tokenResponse);
//        String accessToken = tokenJson.getString("access_token");
//
//        // 2. Получение информации о пользователе
//        String userInfo = UtilsToken.getUserInfo(accessToken);
//        JSONObject userJson = new JSONObject(userInfo);
//        String userId = userJson.getString("id"); // Это и есть нужный user_id
//        log.info("User ID: " + userId);


        // Формируем ответ
        String token = generateRandom();
        String json = " {\"access_token\":\"" + token + "\",\"token_type\":\"bearer\",\"expires_in\":4294967296}";

        // Отправляем ответ клиенту
        log.info("Отправляем ответ клиенту");
        byte[] responseBytes = json.getBytes(StandardCharsets.UTF_8);
        httpExchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream os = httpExchange.getResponseBody();
        os.write(responseBytes);


        log.info("HANDLER CALLBACK FINISH <<<");
    }

}