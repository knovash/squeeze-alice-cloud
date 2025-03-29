package org.knovash.alicebroker;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.knovash.alicebroker.Main.usersFix;

@Log4j2
public class HandlerCallback implements HttpHandler {
//
//
    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        log.info("CALLBACK HANDLER START >>>>>>>>>>>>");
        Context context = Context.contextCreate(httpExchange);

        try {
            // Получаем токен от Яндекс
//            context = YandexToken.action(context);
            log.info("Token response code: {}", context.code);

            // Если токен успешно получен
            if (context.code == 200) {
                JSONObject tokenResponse = new JSONObject(context.bodyResponse);
                String accessToken = tokenResponse.getString("access_token");
                String refreshToken = tokenResponse.getString("refresh_token");

                try {
                    String userId ="";
//                    String userId = YandexToken.getUserId(accessToken);
                    Main.userId = userId;
                    log.info("User ID received: {}", userId);

                    // Проверяем существование пользователя перед сохранением
                    if (!usersFix.exists(userId)) {
                        usersFix.saveUser(userId, accessToken, refreshToken);
                        usersFix.write();
                    } else {
                        log.warn("User already exists: {}", userId);
                    }
                    usersFix.printAll();

                } catch (JSONException e) {
                    throw new IOException("Failed to parse user info", e);
                }

                // Редирект
//                context.headers.set("Location", "/success.html");  // Используем set вместо add
//                context.code = 302;

                context.bodyResponse = "<html><head><meta http-equiv=\"refresh\" content=\"0; url=/\"></head></html>";

                // Например, на страницу /success
                context.bodyResponse = "<html><head><meta http-equiv=\"refresh\" content=\"0; url=/success\"></head></html>";

                context.code = 200;
            }

        } catch (Exception e) {
            log.error("Error processing callback: {}", e.getMessage());
            context.code = 500;
            context.bodyResponse = "{\"error\":\"Authorization failed\"}";
        } finally {
            // Отправляем ответ клиенту
            byte[] responseBytes = context.bodyResponse.getBytes(StandardCharsets.UTF_8);

//            httpExchange.getResponseHeaders().putAll(context.headers);
            context.headers.forEach((k, v) ->
                    httpExchange.getResponseHeaders().add(k, v.get(0)));


            if (context.code == 302) {
                // Для редиректов отправляем только заголовки без тела
                httpExchange.sendResponseHeaders(context.code, -1);
            } else {
                httpExchange.sendResponseHeaders(context.code, responseBytes.length);
                try (OutputStream os = httpExchange.getResponseBody()) {
                    os.write(responseBytes);
                }
            }

            log.info("CALLBACK HANDLER FINISH <<<<<<<<<<<");
        }
    }
}