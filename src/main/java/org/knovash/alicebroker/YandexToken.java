// YandexToken.java
package org.knovash.alicebroker;

import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import static org.knovash.alicebroker.Main.CLIENT_ID;
import static org.knovash.alicebroker.Main.CLIENT_SECRET;

@Log4j2
public class YandexToken {

    private static final String TOKEN_URL = "https://oauth.yandex.ru/token";

//    public static JSONObject exchangeCode(String code, String redirectUri) throws IOException {
//        String params = "grant_type=authorization_code" +
//                "&code=" + code +
//                "&client_id=" + CLIENT_ID +
//                "&client_secret=" + CLIENT_SECRET +
//                "&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8");
//        HttpURLConnection conn = (HttpURLConnection) new URL(TOKEN_URL).openConnection();
//        conn.setRequestMethod("POST");
//        conn.setDoOutput(true);
//
//        try (OutputStream os = conn.getOutputStream()) {
//            os.write(params.getBytes(StandardCharsets.UTF_8));
//        }
//
//        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
//            JSONObject response = new JSONObject(reader.lines().collect(Collectors.joining()));
//            return new JSONObject(response);
//        }
//    }
}