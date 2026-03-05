package org.knovash.alicebroker;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

@Log4j2
public class HandlerWorkAuth implements HttpHandler {

    private static final SecureRandom secureRandom = new SecureRandom();

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        log.info("HANDLER WORK AUTH START >>>");
        Context context = Context.contextCreate(httpExchange);

//        мое приложеие https://oauth.yandex.ru

//        Когда пользователь нажимает кнопку Привязать к Яндексу, Диалоги перенаправляют его на страницу авторизации вашего сервиса
//        URL этой страницы вы указываете, когда создаете связку аккаунтов в консоли разработчика.
//        https://dialogs.yandex.ru/developer/skills/53f7314b-b845-4ec3-9a09-49aaff2e5198/authorization
//        URL авторизации https://alice-lms.zeabur.app/auth

// https://yandex.ru/dev/dialogs/smart-home/doc/ru/auth/how-it-works#authorization
//        Как происходит авторизация в навыке
//        В URL авторизации Диалоги передают параметры:
//        state,redirect_uri,response_type,client_id,scope
        log.info("В URL авторизации Диалоги передают параметры:");
        log.info("state: " + context.queryMap.get("state")); // состояние авторизации. Формируется Диалогами, чтобы отслеживать процесс авторизации. Сервер авторизации должен вернуть Диалогам этот параметр с тем же значением.
        log.info("redirect_uri: " + context.queryMap.get("redirect_uri")); // страница, куда перенаправляется авторизованный пользователь (redirect endpoint).
        // В качестве redirect_uri указывайте URI Диалогов: https://social.yandex.net/broker/redirect.
        log.info("response_type: " + context.queryMap.get("response_type")); // тип авторизации. Принимает значение code.
        log.info("client_id: " + context.queryMap.get("client_id")); // идентификатор OAuth-приложения.
        log.info("scope: " + context.queryMap.get("scope")); // список разрешений Если параметр scope не передан, то токен будет выдан с правами, указанными при регистрации приложения.

//        String redirect = context.queryMap.get("redirect_uri");
        String redirect = "https://social.yandex.net/broker/redirect";

        String randomCode = generateRandom();
        log.info("RANDOM CODE: " + randomCode);
        String location = redirect + "?" +
                "state=" + context.queryMap.get("state") +
                "&client_id=" + context.queryMap.get("client_id") +
                "&scope=" + context.queryMap.get("scope") +
                "&code=" + randomCode;
        log.info("REDIRECTURI: " + location);
        // 4. Отправка редиректа
        log.info("Отправка редиректа 302 " + context.queryMap.get("redirect_uri"));
        httpExchange.getResponseHeaders().add("Location", location);
        httpExchange.sendResponseHeaders(302, -1);
//      Для редиректов 302 отправляем только заголовки без тела (-1)
//        OutputStream outputStream = httpExchange.getResponseBody();
//        outputStream.write("REDIRECT".getBytes());
        log.info("HANDLER WORK AUTH FINISH <<<");
    }

    public static String generateRandom() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

}