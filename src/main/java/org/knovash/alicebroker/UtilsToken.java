package org.knovash.alicebroker;

import java.util.Arrays;

public class UtilsToken {

    public static String maskToken(String token) {
        if (token == null) {
            return null;
        }

        int length = token.length();

        // Для токенов короче 20 символов возвращаем как есть
        if (length <= 20) {
            return token;
        }

        // Рассчитываем длину маскируемой части
        int keep = 10;
        String head = token.substring(0, keep);
        String tail = token.substring(length - keep);
        char[] stars = new char[length - 2 * keep];
        Arrays.fill(stars, '*');

        return head + new String(stars) + tail;
    }
}