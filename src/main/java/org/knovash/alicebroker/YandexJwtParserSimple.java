package org.knovash.alicebroker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.log4j.Log4j2;

import java.util.Base64;
import java.util.Map;

@Log4j2
public class YandexJwtParserSimple {

    public static String parseYandexJwt(String jwtToken, String key) {
        log.info("START PARSE JWT FOR KEY: " + key);
        // Разбиваем JWT на части
        String[] parts = jwtToken.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT token");
        }
        // Декодируем payload (вторую часть)
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
        // Парсим JSON
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> claims = null;
        try {
            claims = mapper.readValue(payload, Map.class);
        } catch (JsonProcessingException e) {
            log.info("ERROR: " + e);
            return null;
//            throw new RuntimeException(e);
        }

//        for (Map.Entry<String, Object> entry : claims.entrySet()) {
//            System.out.printf("%-15s: %s%n", entry.getKey(), entry.getValue());
//        }

        Map.Entry<String, Object> set = claims.entrySet().stream()
//                .peek(c -> log.info(c))
                .filter(c -> c.getKey().equals(key))
                .findFirst()
                .orElse(null);
        if (set == null) return null;
        String result = String.valueOf(set.getValue());
        log.info("KEY: "+key+ " VALUE:"+result);
        return result;
    }
}