package org.knovash.alicebroker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.alicebroker.utils.JsonUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.knovash.alicebroker.Main.config;
import static org.knovash.alicebroker.Main.context;

@Log4j2
public class HandlerAliceVoice implements HttpHandler {

    private final ObjectMapper mapper = new ObjectMapper();
    private static String uid;

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        log.info("HANDLER ALICE VOICE START >>>>>>>>>>>>>>>");
//        создание контекста запроса
        context = Context.contextCreate(exchange);

//        из контекста body получить version и session для использование в ответе
        log.info("CREATE OBJECT REQUEST FROM CONTEXT BODY");
        RequestAliceVoice requestAliceVoice = JsonUtils.jsonToPojo(context.body, RequestAliceVoice.class);
        log.info("CREATE OBJECT RESPONSE NEW");
        Map<String, Object> response = new HashMap<>();
        Object version = requestAliceVoice.version;
        Object session = requestAliceVoice.session;
        log.info("VERSION: " + version);
        log.info("SESSION: " + session);

//        проверка авторизации в запросе, если нет то ответить карточкой авторизации
//        https://yandex.ru/dev/dialogs/alice/doc/ru/auth/make-skill#check-account-linking-available
        String bearerToken = null;
        try {
            bearerToken = context.headers.getFirst("Authorization");
            log.info("BEARER TOKEN: " + bearerToken);
        } catch (Exception e) {
            log.info("ERROR BEARER TOKEN");
        }

        if (bearerToken == null) {
            Object linking = new HashMap<>();
            ResponseAliceVoiceLinking responseAliceVoiceLinking = new ResponseAliceVoiceLinking(version, session, linking);
//          Преобразование ответа в JSON
            String jsonResponse2 = mapper.writeValueAsString(responseAliceVoiceLinking);
            log.info("RESPONSE: " + jsonResponse2);
            log.info("LINKING MAPPER OK");
//          Отправка ответа
            sendResponse(exchange, jsonResponse2);
            return;
        } else {
            log.info("TOKEN: " + bearerToken);
            String jwtToken = "";
            List<String> headerAuthorization = context.headers.getOrDefault("Authorization", null);
            if (headerAuthorization != null && headerAuthorization.size() != 0) {
                bearerToken = headerAuthorization.get(0);
                log.info("BEARER TOKEN: " + UtilsToken.maskToken(bearerToken));
                String token = bearerToken.replace("Bearer ", "");
                uid = YandexJwtUtils.getValueByTokenAndKey(token, "uid");
                log.info("UID: " + uid);
//                try {
//                    jwtToken = YandexJwtUtils.getJwtByOauth(token);
//                } catch (Exception e) {
//                    log.info("JWT ERROR");
////                    throw new RuntimeException(e);
//                }
//                String email = YandexJwtUtils.parseYandexJwtForKey(jwtToken, "email");

//                Hive.topicUdyPublish = "to_lms_id" + uid;
//                log.info("SET PUBLISH TO TOPIC: <" + Hive.topicUdyPublish + ">");
            }
        }

//---------------------------------

        String text = "привет";
        try {
            log.info("TRY PUBLISH COMMAND WAIT FOR ANSWER TO: " + Hive.topicUdyPublish + uid);
            text = Hive.publishContextCommandWaitForAnswer(Hive.topicUdyPublish + uid, context);

        } catch (Exception e) {
            log.info("ERROR WAIT FOR ANSWER");
        }

//        https://yandex.ru/dev/dialogs/alice/doc/ru/auth/make-skill#check-account-linking-available
//        {
//  "start_account_linking": {},
//  "version": 1.0
//}
//        Когда навык отвечает карточкой авторизации, поле response должно отсутствовать.
//        Если одновременно указаны поля start_account_linking и response — это некорректный ответ.
//        Алиса сообщит пользователю, что навык не отвечает.


        response.put("text", text);
        response.put("end_session", true);
//        log.info(" OK");
        ResponseAliceVoice responseAliceVoice = new ResponseAliceVoice(version, session, response);


        // Преобразование ответа в JSON
        String jsonResponse = mapper.writeValueAsString(responseAliceVoice);
        log.info("RESPONSE: " + jsonResponse);

//        Отправка ответа
        sendResponse(exchange, jsonResponse);

        System.out.println("FINISH <<<<<<<<<<<<<<<");
    }

    public static String extractBodyResponse(String rawJson) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(rawJson);
            JsonNode bodyResponseNode = rootNode.get("bodyResponse");
            if (bodyResponseNode != null && bodyResponseNode.isTextual()) {
                return bodyResponseNode.asText();
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
            return null;
        }
    }

    public static void sendResponse(HttpExchange exchange, String jsonResponse) {
        // Настройка заголовков ответа
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        try {
            exchange.sendResponseHeaders(200, jsonResponse.getBytes(StandardCharsets.UTF_8).length);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Отправка ответа
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}