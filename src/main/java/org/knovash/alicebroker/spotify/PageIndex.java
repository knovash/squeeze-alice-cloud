package org.knovash.alicebroker.spotify;

import lombok.extern.log4j.Log4j2;
import org.knovash.alicebroker.Context;

import java.util.HashMap;

@Log4j2
public class PageIndex {

    public static Context action(Context context) {
        context.bodyResponse = page();
        context.code = 200;
        return context;
    }

    public static void refresh(HashMap<String, String> parameters) {
        log.info("REFRESH");
    }

    public static String page() {
        String pageInner =
                "<p><a href=\\html\\manual " +
                        "target=\"_blank\" rel=\"noopener noreferrer\"" +
                        ">Инструкция</a></p>" +
                        "<p><a href=\\lms>Настройка LMS</a></p>" +
                        "<p><a href=\\players>Настройка плееров</a></p>" +
//                "<p><a href=\\yandex>Настройка Yandex</a></p>" +

                        "<p><a href=\\auth " +
                        "target=\"_blank\" rel=\"noopener noreferrer\"" +
                        ">Авторизация в Яндекс</a></p>" +

                        "<p><a href=\\spotify>Настройка Spotify</a></p>" +
                        "<p><b>" + "Комманды:</b></p>" +
                        "<p>" +
                        "Алиса, что играет<br>" +

                        "Алиса, скажи раз-два, выбери колонку Радиотехника<br>" +
                        "Алиса, где пульт<br>" +
                        "Алиса, включи пульт <br>";

        String page = pageOuter(pageInner, "Squeeze-Alice", "Squeeze-Alice");
        return page;
    }

    public static String pageOuter(String pageInner, String title, String header) {
        String page = "<!DOCTYPE html><html lang=\"ru\">" + // Изменили lang на "ru"
                "<head>" +
                "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">" +
                "<meta charset=\"UTF-8\">" +
                "<title>" + title + " local</title>" +
                "</head>" +

                "<body>" +
                "<p><a href=\"/\">Home</a></p>" +
                "  <h2>" + header + "</h2>" +
                pageInner +
                "<p><a href=\"/\">Home</a></p>" +
                "</body></html>";
        return page;
    }
}