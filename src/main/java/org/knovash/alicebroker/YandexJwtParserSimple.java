package org.knovash.alicebroker;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import java.util.Map;

public class YandexJwtParserSimple {

    public static void parseYandexJwt(String jwtToken) throws Exception {
        // Разбиваем JWT на части
        String[] parts = jwtToken.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT token");
        }

        // Декодируем payload (вторую часть)
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));

        // Парсим JSON
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> claims = mapper.readValue(payload, Map.class);

        System.out.println("User Info from JWT:");
        System.out.println("-------------------");

        for (Map.Entry<String, Object> entry : claims.entrySet()) {
            System.out.printf("%-15s: %s%n", entry.getKey(), entry.getValue());
        }
    }
}