package org.knovash.alicebroker;

import lombok.extern.log4j.Log4j2;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

import static org.knovash.alicebroker.HandlerAliceVoice.extractBodyResponse;
import static org.knovash.alicebroker.Main.config;

@Log4j2
public class Hive implements MqttCallbackExtended {

    private static MqttClient mqttClient;
    private static final String hiveBroker = config.hiveBroker;
    private static final String hiveUsername = config.hiveUsername;
    private static final String hivePassword = config.hivePassword;
    private static final ResponseManager responseManager = new ResponseManager();

    public static String topicRecieveResponse = "from_lms_id"; // подписка на ответы от local
    public static String topicRecieveRequest = "from_local_request"; // подписка на запросы от local
    public static String topicUdyPublish = "to_lms_id"; // отправка запросов в local
    public static String callbackTopic = "";
//    public static String state = "";

    // Хранилище активных подписок для восстановления после переподключения
    private static final Set<String> activeSubscriptions = ConcurrentHashMap.newKeySet();

    public static void start() {
        log.info("MQTT STARTING...");
        try {
            mqttClient = new MqttClient(hiveBroker, MqttClient.generateClientId(), new MemoryPersistence());
            // Устанавливаем callback на сам экземпляр Hive (класс реализует интерфейс)
            Hive hiveInstance = new Hive();
            mqttClient.setCallback(hiveInstance);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            options.setUserName(hiveUsername);
            options.setPassword(hivePassword.toCharArray());
            mqttClient.connect(options);

            // Первоначальные подписки (сохраняем в activeSubscriptions)
            subscribe(topicRecieveResponse);
            subscribe(topicRecieveRequest);

            log.info("MQTT STARTED OK");
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    // Метод подписки с сохранением топика
    private static void subscribe(String topic) {
        activeSubscriptions.add(topic);
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.subscribe(topic);
                log.info("SUBSCRIBED TO: {}", topic);
            } catch (MqttException e) {
                log.error("SUBSCRIBE ERROR: {}", e.getMessage());
            }
        } else {
            log.warn("MQTT not connected, subscription will be restored later");
        }
    }

    // Метод отписки с удалением из хранилища
    public static void unsubscribe(String topic) {
        activeSubscriptions.remove(topic);
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.unsubscribe(topic);
                log.info("UNSUBSCRIBED FROM: {}", topic);
            } catch (MqttException e) {
                log.error("UNSUBSCRIBE ERROR: {}", e.getMessage());
            }
        }
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        log.info("MQTT CONNECTION ESTABLISHED. Reconnect: {}", reconnect);
        // Восстанавливаем все сохранённые подписки
        if (!activeSubscriptions.isEmpty()) {
            log.info("Restoring {} subscriptions...", activeSubscriptions.size());
            for (String topic : activeSubscriptions) {
                try {
                    mqttClient.subscribe(topic);
                    log.info("Subscription restored: {}", topic);
                } catch (MqttException e) {
                    log.error("Failed to restore subscription: {}", topic, e);
                }
            }
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.warn("MQTT CONNECTION LOST: {}", cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) {
        // Единый обработчик входящих сообщений
        log.info("RECEIVED MESSAGE FROM TOPIC: {}", topic);
        if (topic.equals(topicRecieveResponse)) {
            handleResponseManagerResievedMessageWithIdAndContext(topic, message);
        } else if (topic.equals(topicRecieveRequest)) {
            handleRequestPublishResponse(topic, message);
        } else if (topic.startsWith("callback")) {
            // callback-топики тоже обрабатываем как ответы
            handleResponseManagerResievedMessageWithIdAndContext(topic, message);
        } else {
            log.warn("Unknown topic: {}", topic);
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        // не используется
    }

    // --- Обработчики сообщений ---

    private static void handleResponseManagerResievedMessageWithIdAndContext(String topic, MqttMessage message) {
        String payload = new String(message.getPayload());
        log.info("PAYLOAD: {}", payload);

        Map<String, String> params = Parser.run(payload);
        if (params.containsKey("correlationId")) {
            String correlationId = params.get("correlationId");
            String contextJson = params.getOrDefault("context", "");
            responseManager.completeResponse(correlationId, contextJson);
        }
    }

    private static void handleRequestPublishResponse(String topic, MqttMessage message) {
        String payload = new String(message.getPayload());
        log.info("RECEIVED MESSAGE: {}", payload);

        Map<String, String> params = Parser.run(payload);
        log.info("PARAMS: {}", params);

        if (!params.containsKey("correlationId")) return;
        String correlationId = params.getOrDefault("correlationId", "");
        callbackTopic = params.getOrDefault("callbackTopic", "");
        String action = params.getOrDefault("action", "");
        String contextJson = params.getOrDefault("context", "");

        log.info("correlationId: {}", correlationId);
        log.info("callbackTopic: {}", callbackTopic);

//        state = correlationId; // возможно используется где-то ещё

        // Подготовка ответа по умолчанию (возвращаем тот же контекст)
        String responsePayload = "correlationId=" + correlationId + "&" +
                "context=" + contextJson;

        log.info("ACTION: {}", action);
        switch (action) {
            case "token":
                log.info("ВЕРНУТЬ ТОКЕН В ТОПИК: " + callbackTopic);
//                log.info("ВЕРНУТЬ ТОКЕН ДЛЯ STATE: " + state);
                return; // ничего не публикуем, ответ будет позже через publishCallbackToken
            case "token_spotify":
                log.info("ВЕРНУТЬ СПОТИФАЙ ТОКЕН В ТОПИК: " + callbackTopic);
//                log.info("ВЕРНУТЬ ТОКЕН ДЛЯ STATE: " + state);
                return;
            case "token_spotify_refresh":
                log.info("ВЕРНУТЬ РЕФРЕШ ТОКЕН В ТОПИК: " + callbackTopic);
                String refreshToken = params.getOrDefault("text", "");
                log.info("REFRESH TOKEN: " + refreshToken);
                String refreshTokenResponse = CloudServerSpoty.refreshTokenResponseBody(refreshToken);
                log.info("REFRESH TOKEN NEW: " + refreshTokenResponse);
                responsePayload = "correlationId=" + correlationId + "&" +
                        "action=" + "spotify_callback_refresh_token" + "&" +
                        "refreshTokenResponse=" + refreshTokenResponse + "&" +
                        "context=" + contextJson;
                break;
            case "/v1.0/user/unlink":
                break;
            default:
                log.info("PATH ERROR " + action);
                break;
        }

        // Публикация ответа
        try {
            mqttClient.publish(callbackTopic, new MqttMessage(responsePayload.getBytes()));
            log.info("PUBLISHED TO {}: {}", callbackTopic, responsePayload);
        } catch (MqttException e) {
            log.error("MQTT PUBLISH ERROR", e);
        }
    }

    // --- Публикация токенов (вызывается из CloudServer) ---
    public static void publishCallbackToken(String token, String refreshToken, String expiresAt, String action, String correlationId) {
        String topic = "callback" + correlationId;
        String payload = "correlationId=" + correlationId + "&" +
                "token=" + token + "&" +
                "expiresAt=" + expiresAt + "&" +
                "refreshToken=" + refreshToken + "&" +
                "action=" + action + "&" +
                "context=---";
        try {
            log.info("PUBLISH TO TOPIC: {}", topic);
            mqttClient.publish(topic, new MqttMessage(payload.getBytes()));
        } catch (MqttException e) {
            log.error("MQTT PUBLISH ERROR", e);
        }
    }

    public static void publishCallbackToken(String token, String action, String correlationId) {
        String topic = "callback" + correlationId;
        String payload = "correlationId=" + correlationId + "&" +
                "token=" + token + "&" +
                "action=" + action + "&" +
                "context=---";
        try {
            log.info("PUBLISH TO TOPIC: {}", topic);
            mqttClient.publish(topic, new MqttMessage(payload.getBytes()));
        } catch (MqttException e) {
            log.error("MQTT PUBLISH ERROR", e);
        }
    }

    // --- Публикация команд и ожидание ответа (для голосовых запросов) ---
    public static String publishContextCommandWaitForAnswer(String topic, Context context) {
        log.info("SIMPLE METHOD FOR VOICE ANSWER");
        String responseBody = publishContextWaitForContext(topic, context);
        return extractBodyResponse(responseBody);
    }

    public static String publishContextWaitForContext(String topic, Context context) {
        log.info("MQTT PUBLISH TO TOPIC: " + topic);
        String correlationId = UUID.randomUUID().toString();
        String contextJson = context.toJson();
        String payload = "correlationId=" + correlationId + "&" +
                "context=" + contextJson;
        try {
            mqttClient.publish(topic, new MqttMessage(payload.getBytes()));

            CompletableFuture<String> future = responseManager.waitForResponse(correlationId);
            log.info("MQTT WAIT FOR RESPONSE...");
            // Увеличиваем таймаут до 10 секунд (было 4)
            String responseBody = future.get(10, TimeUnit.SECONDS);
            log.info("MQTT RESPONSE RECEIVED OK");
            return responseBody;
        } catch (TimeoutException e) {
            log.warn("MQTT RESPONSE TIMEOUT after 10s: {}", e.getMessage());
            return "---";
        } catch (Exception e) {
            log.error("MQTT RESPONSE ERROR", e);
            return "---";
        }
    }

    // --- Внутренний класс ResponseManager (без изменений) ---
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
            } else {
                log.warn("No future found for correlationId: {}", correlationId);
            }
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