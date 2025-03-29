package org.knovash.alicebroker;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class HandlerSuccess implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String response =   PageSuccess.page();
//        String response = "<h1>Авторизация успешна!</h1>" +
//                "<p><strong>Команды</strong></p> \n" +
//                Main.userId+
//                "<p>Алиса, скажи раз два, что играет</p> \n" +
//                "<a href='/'>На главную</a>";
        exchange.sendResponseHeaders(200, response.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}