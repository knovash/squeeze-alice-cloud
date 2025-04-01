package org.knovash.alicebroker;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class PageIndex {

    public static String page() {
        log.info("PAGE INDEX");

        String page = "<!doctype html><html lang=\"ru\">\n" +
                "<head>\n" +
                "<meta charSet=\"utf-8\" />\n" +
                "<title>Squeeze-Alice cloud</title>" +
                "</head>\n" +
                "<body> \n" +

                "<p><strong>Привет! это навык Алисы для управления плеерами Logitech Media Server</strong></p> \n" +
                "<a href=\"/html/manual\" target=\"_blank\" rel=\"noopener noreferrer\">Инструкция</a>" +
                "<br>" +

                "<p><strong>Команды</strong></p> \n" +

                "<p>Алиса, скажи раз два, что играет</p> \n" +
                "<p>а) Ответ: сейчас на Homepod1 играет Chillout</p> \n" +
                "<p>б) Ответ: сейчас на Homepod1 не играет Chillout</p> \n" +
                "<p>в) Ответ: скажите навыку, это комната и название комнаты, например Гостиная</p> \n" +

                "<p>Алиса, скажи раз два, это комната гостиная</p> \n" +
                "<p>а) Ответ: это комната Гостиная. колонка в комнате еще не выбрана</p> \n" +
                "<p>б) Ответ: это комната Гостиная. с колонкой Homepod1</p> \n" +

                "<p>Алиса, скажи раз два, выбери колонку homepod</p> \n" +
                "<p>Ответ: выбрана колонка Homepod1 в комнате Гостиная</p> \n" +

                "<p>Алиса, скажи раз два, включи канал минимал</p> \n" +
                "<p>Алиса, скажи раз два, включи избранное минимал</p> \n" +
                "<p>а) сейчас, мой господин, включаю канал 9, Minimal</p> \n" +
                "<p>б) повторите</p> \n" +

                "</body>\n" +
                "</html>";
        return page;
    }
}