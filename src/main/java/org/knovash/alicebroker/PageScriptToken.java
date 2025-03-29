package org.knovash.alicebroker;

import lombok.extern.log4j.Log4j2;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Log4j2
public class PageScriptToken {

    public static String page() {
        log.info("PAGE INDEX");

        String clientId = "9aa97fffe29849bb945db5b82b3ee015";
        String redirectUri = "http://alice-lms.zeabur.app/callback";
        String authUrl = "https://oauth.yandex.ru/authorize" +
                "?response_type=code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8) +
                "&force_confirm=true" +

                "&scope=yandex:passport:access" + // Добавьте необходимый scope
//                "&scope=yandex:passport:access+yandex:cloud:access" // <-- Добавлен scope
//                "&scope=yandex:passport:access+yandex:cloud:access"
               "" ; // <-- Добавлен scope


//        String authUrl = "https://oauth.yandex.ru/authorize"
//                + "?response_type=code"
//                + "&client_id=9aa97fffe29849bb945db5b82b3ee015"
//                + "&redirect_uri=http://alice-lms.zeabur.app/callback"
//                + "&force_confirm=true"; // <-- Этот параметр

        String page =

//                "<!doctype html><html lang=\"ru\">\n" +
//                "<head>\n" +
//                "<meta charSet=\"utf-8\" />\n" +
//                "<title>Squeeze-Alice cloud</title>" +
//                "</head>\n" +
//                "<body> \n" +

                "<!doctype html>\n" +
                "<html lang=\"ru\">\n" +
                "\n" +
                "<head>\n" +
                "   <meta charSet=\"utf-8\" />\n" +
                "   <meta name='viewport' content='width=device-width, initial-scale=1, maximum-scale=1, minimum-scale=1, shrink-to-fit=no, viewport-fit=cover'>\n" +
                "   <meta http-equiv='X-UA-Compatible' content='ie=edge'>\n" +
                "   <style>\n" +
                "      html,\n" +
                "      body {\n" +
                "         background: #eee;\n" +
                "      }\n" +
                "   </style>\n" +
                "   <script src=\"https://yastatic.net/s3/passport-sdk/autofill/v1/sdk-suggest-token-with-polyfills-latest.js\"></script>\n" +
                "</head>\n" +
                "\n" +
                "<body>\n" +
                "   <script>\n" +
                "      window.onload = function() {\n" +
                "         window.YaSendSuggestToken(\"https://examplesite.com\", {\n" +
                "            \"kek\": true\n" +
                "         });\n" +
                "      };\n" +
                "   </script>\n" +


//                "</body>\n" +
//                "\n" +
//                "</html>"+

                "</body>\n" +
                "</html>";
        return page;
    }
}