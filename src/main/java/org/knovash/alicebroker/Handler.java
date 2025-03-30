package org.knovash.alicebroker;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.OutputStream;

@Log4j2
public class Handler implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        log.info("START ---------------------------------------------");
        Context context = Context.contextCreate(httpExchange);
        String path = context.path;
        log.info("SWITCH PATH: " + path);
        switch (path) {
            case ("/auth"): // сюда первый запрос от Яндекса для привязки акаунта
                context = YandexAuth.action(context);
                break;
            case ("/callback"): // сюда второй запрос от Яндекса для привязки акаунта
                context = YandexToken.action(context);
                break;
            default:
                log.info("PATH ERROR " + path);
                break;
        }

        String json = context.bodyResponse;
        int code = context.code;
        log.info("CODE: " + code);
        httpExchange.getResponseHeaders().putAll(context.headers);
//        log.info("HEADERS: " + httpExchange.getResponseHeaders().entrySet());
//        log.info("RESPONSE: " + json);
        httpExchange.sendResponseHeaders(code, json.getBytes().length);
        OutputStream outputStream = httpExchange.getResponseBody();
        outputStream.write(json.getBytes());
        outputStream.flush();
        outputStream.close();
        log.info("FINISH");
    }
}