package org.knovash.alicebroker;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.alicebroker.utils.JsonUtils; // вернули импорт

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@Log4j2
public class HandlerInfo implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        log.info("HANDLER INFO START >>>");

        // Проверяем метод и путь
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        if (!"GET".equalsIgnoreCase(method) || !"/info".equals(path)) {
            log.warn("Invalid request: method={}, path={}", method, path);
            exchange.sendResponseHeaders(404, -1);
            exchange.close();
            return;
        }

        Info info = new Info();

        // Сериализуем объект в JSON
        String jsonResponse = JsonUtils.pojoToJson(info); // предполагаем, что этот метод существует
        byte[] responseBytes = jsonResponse.getBytes(StandardCharsets.UTF_8);

        // Заголовки
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, responseBytes.length);

        // Отправляем тело
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }

        log.info("HANDLER INFO FINISH <<<");
    }
}