package org.knovash.alicebroker;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.knovash.alicebroker.Main.*;

@Log4j2
public abstract class HandlerAbstract implements HttpHandler {

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        log.info("");
        log.info("HANDLER ABSTARCT START >>>>>>>>>>>>>>>");
        context = Context.contextCreate(httpExchange);
        String bearerToken = "";

        if (context.path.contains("v1.0")) {
            List<String> headerAuthorization = context.headers.getOrDefault("Authorization", null);
//            log.info("headerAuthorization: " + headerAuthorization);
            if (headerAuthorization != null && headerAuthorization.size() != 0) {
                bearerToken = headerAuthorization.get(0);
                log.info("BEARER TOKEN: " + UtilsToken.maskToken(bearerToken));
//                String bearerHash = String.valueOf(bearerToken.hashCode());
//                log.info("BEARER HASH: " + bearerHash);
            }

//            String token = bearerToken.replace("Bearer ", "");
//            log.info("TOKEN: " + UtilsToken.maskToken(token));

//            Users.User user = users.getUserByAccessToken(token);
//            if(user != null) userId = user.id;


//            String userId = YandexToken.getUserId(token);
            log.info("USER ID: " + userId);

        }

//        override
        context = processContext(context);

        log.info("SEND RESPONSE");
        sendResponse(httpExchange, context);
        log.info("HANDLER ABSTARCT FINISH <<<<<<<<<<<<<<<");
        log.info("");
    }

    private void sendResponse(HttpExchange exchange, Context context) throws IOException {
        log.info("CODE: " + context.code);
        log.info("BODY: " + context.bodyResponse);
        byte[] responseBytes = context.bodyResponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().putAll(context.headers);
        exchange.sendResponseHeaders(context.code, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    protected abstract Context processContext(Context context);
}