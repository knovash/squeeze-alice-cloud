package org.knovash.alicebroker;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class YandexApiClientOne {

    private static final String YANDEX_USER_INFO_URL = "https://login.yandex.ru/info";
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static String getYandexUserId(String accessToken) throws Exception {
        log.info("GET USER ID BY TOKEN: " + accessToken);
        // Создаем HTTP-запрос
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(YANDEX_USER_INFO_URL))
                .header("Authorization", "OAuth " + accessToken)
                .header("Accept", "application/json")
                .GET()
                .build();

        // Отправляем запрос и получаем ответ
        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
        log.info("RESPONSE: " + response);

        // Проверяем статус ответа
        if (response.statusCode() != 200) {
            log.info("ERROR REQUEST");
            throw new RuntimeException("Failed to get user info: " + response.body());
        }

        // Парсим JSON-ответ
        YandexUserInfo userInfo = objectMapper.readValue(response.body(), YandexUserInfo.class);
        log.info("USER INFO: " + userInfo);

        return userInfo.getId();
    }

    // DTO для парсинга ответа от Яндекса
    public static class YandexUserInfo {
        private String id;
        private String login;
        private String display_name;

        // Геттеры и сеттеры
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "YandexUserInfo{" +
                    "id='" + id + '\'' +
                    ", login='" + login + '\'' +
                    ", display_name='" + display_name + '\'' +
                    '}';
        }
// Остальные геттеры/сеттеры...
    }

//    public static void main(String[] args) {
//        try {
//            String accessToken = "your_yandex_oauth_token_here";
//            String yandexUserId = getYandexUserId(accessToken);
//            System.out.println("Yandex User ID: " + yandexUserId);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}