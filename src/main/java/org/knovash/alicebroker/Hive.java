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
import static org.knovash.alicebroker.Main.config;

@Log4j2
public class Hive {

    private static MqttClient mqttClient;
    private static final String hiveBroker = config.hiveBroker;
    private static final String hiveUsername = config.hiveUsername;
    private static final String hivePassword = config.hivePassword;
    private static final ResponseManager responseManager = new ResponseManager();

    //    public static String userYandexUid = "";
    public static String topicRecieveResponse = "from_lms_id";// подписаться, ответы на мои запросы в local
    public static String topicRecieveRequest = "from_local_request";// подписаться, запросы от local, вернуть ответ
    public static String topicUdyPublish = "to_lms_id";// отправить сюда запросы в local
    public static String callbackTopic = "";
    public static String state = "";

    public static void start() {
        log.info("MQTT STARTING...");
        try {
            mqttClient = new MqttClient(hiveBroker, MqttClient.generateClientId(), new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            options.setUserName(hiveUsername);
            options.setPassword(hivePassword.toCharArray());
            mqttClient.connect(options);

// Подписка на топик ответа
            log.info("SUBSCRIBE: " + topicRecieveResponse + " " + "handleResponseManagerResievedMessageWithIdAndContext");
            log.info("SUBSCRIBE: " + topicRecieveRequest + " " + "handleRequestPublishResponse");
            mqttClient.subscribe(topicRecieveResponse, (topic, message) -> handleResponseManagerResievedMessageWithIdAndContext(topic, message));
            mqttClient.subscribe(topicRecieveRequest, (topic, message) -> handleRequestPublishResponse(topic, message));
// mqttClient.subscribe("command_to_cloud", (topic, message) -> handleVoiceMqttRequestAndPublishAnswer(topic, message));
            log.info("MQTT STARTED OK");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    // ПРИНИМАЕТ ОТВЕТЫ ДЛЯ УДЯ И ГОЛОСОВЫХ КОМАНД
    private static void handleResponseManagerResievedMessageWithIdAndContext(String topic, MqttMessage message) {
        log.info("RECIEVED MESSAGE FROM TOPIC : " + topic);
        String payload = new String(message.getPayload());

//        Map<String, String> params = parseParams(payload);
        Map<String, String> params = Parser.run(payload);

        if (params.containsKey("correlationId")) {
// если есть ID то получить ID и Context из сообщения
            String correlationId = params.get("correlationId");
            String contextJson = params.getOrDefault("context", "");
            responseManager.completeResponse(correlationId, contextJson);
        }
    }

    private static Map<String, String> parseParams(String message) {
        Map<String, String> result = new HashMap<>();
        if (message == null || message.isEmpty()) return result;
        int ctxStart = message.indexOf("context=");
        if (ctxStart == -1) return result;
        String prefix = message.substring(0, ctxStart);
        String[] parts = prefix.split("&");
        for (String part : parts) {
            String[] kv = part.split("=", 2);
            if (kv.length != 2) continue;
            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String value = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            if (key.equals("correlationId")) {
                result.put(key, value);
            }
        }
        String contextValue = message.substring(ctxStart + "context=".length());
        result.put("context", URLDecoder.decode(contextValue, StandardCharsets.UTF_8));
        return result;
    }


    // *************************************
// ПРИНИМАЕТ ЗАПРОСЫ ОТ local И ВОЗВРАЩАЕТ ОТВЕТ
    private static void handleRequestPublishResponse(String topic, MqttMessage message) {
        log.info("RECIEVED MESSAGE FROM TOPIC : " + topic);
        log.info("RECIEVED MESSAGE: " + message);
        String payload = new String(message.getPayload());
        String correlationId = null;
        String contextJson = null;
//        String callbackTopic = null;
        String action = null;

// если в сообщении есть ключ то получить из сообщения: ключ, колбэктопик, контекст

//        Map<String, String> params = parseParams(payload);
        Map<String, String> params = Parser.run(payload);

        if (!params.containsKey("correlationId")) return;
        correlationId = params.getOrDefault("correlationId", "");
        callbackTopic = params.getOrDefault("callbackTopic", "");
        action = params.getOrDefault("action", "");
        contextJson = params.getOrDefault("context", "");
        log.info("correlationId: " + correlationId);
        log.info("callbackTopic: " + callbackTopic);


//        ОБРАБОТАТЬ КОНТЕКСТ **************
        switch (action) {
            case ("token"):
                log.info("ВЕРНУТЬ ТОКЕН В ТОПИК: " + callbackTopic);
                state = correlationId;
                log.info("ВЕРНУТЬ ТОКЕН ДЛЯ STATE: " + state);
                return;
//                break;
            case ("token_spotify"):
                log.info("ВЕРНУТЬ ТОКЕН В ТОПИК: " + callbackTopic);
                state = correlationId;
                log.info("ВЕРНУТЬ ТОКЕН ДЛЯ STATE: " + state);
                return;
//                break;
            case ("/v1.0/user/unlink"):
                break;
            default:
                log.info("PATH ERROR " + action);
                break;
        }


// отправка ответа в колбэк топик
        log.info("MQTT PUBLISH TO TOPIC: " + callbackTopic);
// String contextJson = context.toJson();

// подготовка ответа, вернуть ключ и контекст с ответом
        payload = "correlationId=" + correlationId + "&" +
                "context=" + contextJson;
// Отправка ответа
        try {
            mqttClient.publish(callbackTopic, new MqttMessage(payload.getBytes()));
        } catch (MqttException e) {
            log.info("MQTT PUBLISH ERROR: " + e);
// throw new RuntimeException(e);
        }
    }

    public static void publishCallbackToken(String token, String action) {
        // отправка ответа в колбэк топик
        log.info("MQTT PUBLISH TO TOPIC: " + callbackTopic);
// String contextJson = context.toJson();

// подготовка ответа, вернуть ключ и контекст с ответом
        String payload = "correlationId=" + state + "&" +
                "token=" + token + "&" +
                "action=" + action + "&" +
                "context=" + "---";
        log.info("PAYLOAD: " + payload);
// Отправка ответа
        try {
            log.info("TRY PUBLISH...");
            mqttClient.publish(callbackTopic, new MqttMessage(payload.getBytes()));
        } catch (MqttException e) {
            log.info("MQTT PUBLISH ERROR: " + e);
// throw new RuntimeException(e);
        }
    }


// ***************************************************


// ------------------------PUBLISH

    public static String publishContextCommandWaitForAnswer(String topic, Context context) {
        log.info("SIMPLE METHOD FOR VOICE ANSWER");
        String responseBody = publishContextWaitForContext(topic, context);
        String answer = extractBodyResponse(responseBody);
        return answer;
    }

    // ЭТО РАБОЧИЙ СЕЙЧАС МЕТОД ДЛЯ УДЯ КОМАНД
    public static String publishContextWaitForContext(String topic, Context context) {
        log.info("MQTT PUBLISH TO TOPIC: " + topic);
        String correlationId = UUID.randomUUID().toString();
        String responseBody = "";
        String contextJson = context.toJson();
        try {
// Отправка запроса в MQTT
//            String payload = String.format("correlationId=%s&context=%s", correlationId, contextJson);
            String payload = "correlationId=" + correlationId + "&" +
//                    "userTopicId=" + topicRecieveDevice + "&" +
                    "context=" + contextJson;
            mqttClient.publish(topic, new MqttMessage(payload.getBytes()));
// Ожидание ответа
            CompletableFuture<String> future = responseManager.waitForResponse(correlationId);
// Получение ответа
            try {
                log.info("MQTT WAIT FOR RESPONSE...");
// если таймаут больше 4 то навык ответит раньше что Навык не отвечает
// 4 - недождалась ответа, но иногда может быть Навык неотвечает
// для УДЯ было 10
                responseBody = future.get(4, TimeUnit.SECONDS);
                log.info("MQTT RESPONSE RECIEVED OK");
            } catch (TimeoutException e) {
                log.info("MQTT ERROR NO RESPONSE: " + e);
                responseBody = "---";
            }
        } catch (Exception e) {
        }
        return responseBody;
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

    public static void unsubscribe(String topic) {
        log.info("HIVE UNSUBSCRIBE TOPIC: " + topic);
        try {
            mqttClient.unsubscribe(topic);
        } catch (MqttException e) {
            log.info("MQTT UNSUBSCRIBE ERROR: " + e);
        }
    }

    public static void stop() {
        log.info("HIVE STOP");
        try {
            mqttClient.disconnect();
            mqttClient.close();
        } catch (MqttException e) {
            throw new RuntimeException(e);
        }
        mqttClient = null;
        log.info("MQTT CLIENT CLOSED");
    }
}