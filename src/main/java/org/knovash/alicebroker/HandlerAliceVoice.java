package org.knovash.alicebroker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;
import org.knovash.alicebroker.utils.JsonUtilsNew;
//import org.knovash.alicebroker.utils.JsonUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.knovash.alicebroker.Main.context;

@Log4j2
public class HandlerAliceVoice implements HttpHandler {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        log.info("HANDLER ALICE VOICE START >>>>>>>>>>>>>>>");
        context = Context.contextCreate(exchange);
        RequestAliceVoice requestAliceVoice = JsonUtilsNew.jsonToPojo(context.body, RequestAliceVoice.class);
        // Обработка запроса
        Map<String, Object> response = new HashMap<>();
        Object version = requestAliceVoice.version;
        Object session = requestAliceVoice.session;
//        String responseJson = Hive.publishCommandWaitForAnswer(Hive.topicUdyPublish, context);
////        log.info("RECIEVED MQTT: " + responseJson);
//        String rrr = extractBodyResponse(responseJson);
//        log.info("RESPONCE VOICE SET: " + rrr);
////        String text = "привет";
//        String text = rrr;
        String text = Hive.publishContextCommandWaitForAnswer(Hive.topicUdyPublish, context);

        response.put("text", text);
        response.put("end_session", true);
//        log.info(" OK");
        ResponseAliceVoice responseAliceVoice = new ResponseAliceVoice(version, session, response);
        // Преобразование ответа в JSON
        String jsonResponse = mapper.writeValueAsString(responseAliceVoice);
//        log.info("JSON OK");
        // Настройка заголовков ответа
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(200, jsonResponse.getBytes(StandardCharsets.UTF_8).length);
//        log.info("HEADERS OK");

//        byte[] responseBytes = context.bodyResponse.getBytes(StandardCharsets.UTF_8);
//        exchange.getResponseHeaders().putAll(context.headers);
//        exchange.sendResponseHeaders(context.code, responseBytes.length);
//        try (OutputStream os = exchange.getResponseBody()) {
//            os.write(responseBytes);
//        }

        // Отправка ответа
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(jsonResponse.getBytes(StandardCharsets.UTF_8));
        }
        System.out.println("FINISH <<<<<<<<<<<<<<<");
    }

    public static String extractBodyResponse(String rawJson) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(rawJson);
            // Получаем значение bodyResponse напрямую из корневого узла
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
}