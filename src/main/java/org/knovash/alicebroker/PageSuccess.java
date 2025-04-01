package org.knovash.alicebroker;

import lombok.extern.log4j.Log4j2;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Log4j2
public class PageSuccess {

    public static String page() {
        log.info("PAGE SUCCESS");
        String page = "<!doctype html><html lang=\"ru\">\n" +
                "<head>\n" +
                "<meta charSet=\"utf-8\" />\n" +
                "<title>Squeeze-Alice</title>" +
                "</head>\n" +
                "<body> \n" +

                "<h1>Авторизация успешна!</h1>" +
//                Main.userId +

                "</body>\n" +
                "</html>";
        return page;
    }
}