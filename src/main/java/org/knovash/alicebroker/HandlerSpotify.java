package org.knovash.alicebroker;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.alicebroker.spotify.PageSpotify;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static org.knovash.alicebroker.HandlerWorkAuth.generateRandom;

@Log4j2
public class HandlerSpotify implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        log.info("HANDLER CALLBACK START >>>");
        Context context = Context.contextCreate(httpExchange);




        // Формируем ответ
        String json = " {\"access_token\":\"" + "token" + "\",\"token_type\":\"bearer\",\"expires_in\":4294967296}";


        json = PageSpotify.page();

        // Отправляем ответ клиенту
        log.info("Отправляем ответ клиенту");
        byte[] responseBytes = json.getBytes(StandardCharsets.UTF_8);
        httpExchange.sendResponseHeaders(200, responseBytes.length);
        OutputStream os = httpExchange.getResponseBody();
        os.write(responseBytes);


        log.info("HANDLER CALLBACK FINISH <<<");
    }

}