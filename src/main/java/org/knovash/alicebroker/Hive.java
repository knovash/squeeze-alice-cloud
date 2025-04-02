package org.knovash.alicebroker;

import lombok.extern.log4j.Log4j2;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import static org.knovash.alicebroker.HandlerAliceVoice.extractBodyResponse;

@Log4j2
public class Hive {

    private static final String HIVE_BROKER = "ssl://811c56b338f24aeea3215cd680851784.s1.eu.hivemq.cloud:8883";
    private static final String HIVE_USERNAME = "novashki";
    private static final String HIVE_PASSWORD = "Darthvader0";
    private static MqttClient mqttClient;
    public static String userYandexEmail = "";
    private static final ResponseManager responseManager = new ResponseManager();
    public static String topicRecieve = "from_lms_id"; // подписаться
    public static String topicUdyPublish = "to_lms_id" + userYandexEmail; // отправить сюда


    public static void start() {
        log.info("MQTT STARTING...");
        try {
            mqttClient = new MqttClient(HIVE_BROKER, MqttClient.generateClientId(), new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
//            переподключение
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            options.setUserName(HIVE_USERNAME);
            options.setPassword(HIVE_PASSWORD.toCharArray());
            mqttClient.connect(options);
            // Подписка на топик ответа
            mqttClient.subscribe(topicRecieve, (topic, message) -> handleMqttMessageId(topic, message));
            mqttClient.subscribe("command_to_cloud", (topic, message) -> handleVoiceMqttRequestAndPublishAnswer(topic, message));
            log.info("MQTT STARTED OK");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public static void publishToTopicText(String topic, String payload) {
        try {
            mqttClient.publish(topic, new MqttMessage(payload.getBytes()));
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
    }

//    ЭТО РАБОЧИЙ СЕЙЧАС МЕТОД ОТВЕТА НА ВОПРОС ЧТО ИГРАЕТ
    public static String publishContextCommandWaitForAnswer(String topic, Context context) {
        log.info("MQTT PUBLISH TO TOPIC: " + topic);
        String correlationId = UUID.randomUUID().toString();
        String responseBody = "";
        String contextJson = context.toJson();
        try {
            // Отправка запроса в MQTT
            String payload = String.format("correlationId=%s&context=%s",
                    correlationId, contextJson);
            mqttClient.publish(topic, new MqttMessage(payload.getBytes()));
            // Ожидание ответа
            CompletableFuture<String> future = responseManager.waitForResponse(correlationId);
            try {
//                если таймаут больше 4 то навык ответит раньше что Навык не отвечает
//                4 - недождалась ответа, но иногда может быть Навык неотвечает
                responseBody = future.get(4, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                log.info("MQTT ERROR NO RESPONSE answer = недождалась ответа");
//                responseBody = "---";
                return "недождалась ответа";
            }
        } catch (Exception e) {
            log.info("MQTT ERROR NO RESPONSE 2");
//                responseBody = "---";
            return "недождалась ответа";
        }
        String answer = extractBodyResponse(responseBody);
        return answer;
    }

    public static String publishContextWaitForContext(String topic, Context context) {
        log.info("MQTT PUBLISH TO TOPIC: " + topic);
        String correlationId = UUID.randomUUID().toString();
        String responseBody = "";
        String contextJson = context.toJson();
        try {
            // Отправка запроса в MQTT
            String payload = String.format("correlationId=%s&context=%s",
                    correlationId, contextJson);
            mqttClient.publish(topic, new MqttMessage(payload.getBytes()));
            // Ожидание ответа
            CompletableFuture<String> future = responseManager.waitForResponse(correlationId);
            try {
                log.info("MQTT WAIT FOR RESPONSE...");
                responseBody = future.get(15, TimeUnit.SECONDS);
                log.info("MQTT RESPONSE RECIEVED OK");
            } catch (TimeoutException e) {
                log.info("MQTT ERROR NO RESPONSE: " + e);
                responseBody = "---";
            }
        } catch (Exception e) {
        }
        return responseBody;
    }

    private static void handleMqttMessageId(String topic, MqttMessage message) {
        log.info("RECIEVED MESSAGE FROM TOPIC : " + topic);
        log.info("MESSAGE : " + message);
        String payload = new String(message.getPayload());
        Map<String, String> params = parseParams(payload);
        if (params.containsKey("correlationId")) {
            String correlationId = params.get("correlationId");
//            log.info("ID : " + correlationId);
            String contextJson = params.getOrDefault("context", "");
//            log.info("CONTEXT JSON : " + contextJson);
            responseManager.completeResponse(correlationId, contextJson);
        }
    }

    private static void handleVoiceMqttRequestAndPublishAnswer(String topicRecieved, MqttMessage request) {
        log.info("RECIEVED MESSAGE FROM TOPIC: " + topicRecieved);
        log.debug("MESSAGE : " + request);
        String payload = new String(request.getPayload());
        Map<String, String> params = parseParams(payload);
        String command = "";
        String correlationId = "";
        if (params.containsKey("correlationId")) {
            correlationId = params.get("correlationId");
            command = params.getOrDefault("command", "");

//            Context context = Context.fromJson(contextJson);
//            AliceRequest aliceRequest = AliceRequest.fromJson(context.body);
//            String applicationId = aliceRequest.session.application.applicationId;
//            String command = aliceRequest.request.command;
//            log.debug("applicationId : " + applicationId);
//            log.info("command : " + command);
//            aliceId = applicationId;

//          выполнить голосовую команду c ID колонки(комнаты) и получить ответ
            String answer = "ff";
//            String answer = SwitchVoiceCommand.switchVoice(applicationId, command);
            log.info("ANSWER : " + answer);

//         положить в пэйлоад сообщения ид и ответ
            payload = "correlationId=" + correlationId + "&" +
                    "context=" + answer;
            try {
                MqttMessage responseMessage = new MqttMessage(payload.getBytes());
                mqttClient.publish(correlationId, responseMessage);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    private static Map<String, String> parseParams(String message) {
        Map<String, String> result = new HashMap<>();
        if (message == null || message.isEmpty()) return result;
        // Ищем параметры по ключам с учетом их позиции
        int ctxStart = message.indexOf("context=");
        if (ctxStart == -1) return result;
        // Выделяем correlationId и requestId до начала context
        String prefix = message.substring(0, ctxStart);
        String[] parts = prefix.split("&");
        for (String part : parts) {
            String[] kv = part.split("=", 2);
            if (kv.length != 2) continue;
            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String value = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            if (key.equals("correlationId")) {
                result.put(key, value);
//                log.info("RESULT ADD correlationId: " + result.entrySet());
            }
        }
        // Извлекаем context как всю оставшуюся часть строки
        String contextValue = message.substring(ctxStart + "context=".length());
        result.put("context", URLDecoder.decode(contextValue, StandardCharsets.UTF_8));
//        log.info("RESULT ADD context: " + result.entrySet());
        return result;
    }

    private static class ResponseManager {

        private final ConcurrentMap<String, CompletableFuture<String>> responses = new ConcurrentHashMap<>();

        public CompletableFuture<String> waitForResponse(String correlationId) {
            CompletableFuture<String> future = new CompletableFuture<>();
            responses.put(correlationId, future);
            return future;
        }

        public void completeResponse(String correlationId, String contextJson) {
            CompletableFuture<String> future = responses.remove(correlationId);
            if (future != null) {
                future.complete(contextJson);
            }
        }
    }
}




//    private static void handleCommandAndPublishAnswer(String topicRecieved, MqttMessage request) {
////        получить текстовую команду (запуск авторизации) и отправить текстовый ответ (User ID)
//        log.info("RECIEVED MESSAGE FROM TOPIC: " + topicRecieved);
//        log.debug("MESSAGE : " + request);
//        String payload = new String(request.getPayload());
//        Map<String, String> params = parseParams(payload);
//        String command = "";
//        String correlationId = "";
//        if (params.containsKey("correlationId")) {
//            correlationId = params.get("correlationId");
//            command = params.getOrDefault("command", "");
//
////            Context context = Context.fromJson(contextJson);
////            AliceRequest aliceRequest = AliceRequest.fromJson(context.body);
////            String applicationId = aliceRequest.session.application.applicationId;
////            String command = aliceRequest.request.command;
////            log.debug("applicationId : " + applicationId);
////            log.info("command : " + command);
////            aliceId = applicationId;
////          выполнить голосовую команду c ID колонки(комнаты) и получить ответ
//            String answer = "ff";
////            String answer = SwitchVoiceCommand.switchVoice(applicationId, command);
//            log.info("ANSWER : " + answer);
//
////         положить в пэйлоад сообщения ид и ответ
//            payload = "correlationId=" + correlationId + "&" +
//                    "context=" + answer;
//            try {
//                MqttMessage responseMessage = new MqttMessage(payload.getBytes());
//                mqttClient.publish(correlationId, responseMessage);
//            } catch (MqttException e) {
//                e.printStackTrace();
//            }
//        }
//    }