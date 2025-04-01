package org.knovash.alicebroker;

import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
@Log4j2
public class YandexApiClient {

    public static String getYandexUserInfoJwt(String oauthToken) throws Exception {
        log.info("START GET JWT");
        String url = "https://login.yandex.ru/info?format=jwt";

        HttpURLConnection connection = null;
        try {
            // Создаем соединение
            URL apiUrl = new URL(url);
            connection = (HttpURLConnection) apiUrl.openConnection();

            // Устанавливаем метод и заголовки
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "OAuth " + oauthToken);
            connection.setRequestProperty("Accept", "application/jwt");

            // Получаем ответ
            log.info("ПОЛУЧАЕМ ОТВЕТ");
            int responseCode = connection.getResponseCode();
            log.info("RESPONSE CODE: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Читаем ответ
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                log.info("OK: " + response.toString());
                return response.toString();
            } else {
                log.info("ERROR");
                return "ERROR";
//                throw new Exception("HTTP error code: " + responseCode);
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

}