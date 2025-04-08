package org.knovash.alicebroker;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class Main {

    public static Config config = new Config();
    public static Context context = new Context();

    public static void main(String[] args) {
        log.info("+----------------+");
        log.info("|      START     |");
        log.info("+----------------+");
        config.readConfigProperties();
        Hive.start();
        Server.start();
    }
}
// Подключение мгновенной авторизации https://yandex.ru/dev/id/doc/ru/suggest-connection
// Настроить авторизационный сервер https://yandex.ru/dev/dialogs/smart-home/doc/ru/auth/create-server