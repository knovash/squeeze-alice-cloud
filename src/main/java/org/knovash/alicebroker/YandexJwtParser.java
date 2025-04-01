package org.knovash.alicebroker;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.log4j.Log4j2;

import java.util.Map;
@Log4j2
public class YandexJwtParser {

    public static void parseYandexJwt(String jwtToken) {
        log.info("PARSE JWT START: " + jwtToken);
        // JWT от Yandex не подписан, поэтому пропускаем проверку подписи
        Claims claims = Jwts.parserBuilder()
                .setAllowedClockSkewSeconds(60 * 60 * 24 * 365) // игнорируем expiration
                .build()
                .parseClaimsJws(jwtToken)
                .getBody();

        // Выводим всю информацию из токена
        log.info("User Info from JWT:");
        log.info("-------------------");

        for (Map.Entry<String, Object> entry : claims.entrySet()) {
            log.info("%-15s: %s%n", entry.getKey(), entry.getValue());
        }

        // Пример доступа к конкретным полям
        log.info("\nОсновные данные:");
        log.info("Email: " + claims.get("email", String.class));
        log.info("Login: " + claims.get("login", String.class));
        log.info("Name: " + claims.get("name", String.class));
        log.info("UID: " + claims.get("uid", Integer.class));
    }
}