package org.knovash.alicebroker;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class Main {

    public static Config config = new Config();
    public static Context context = new Context();
    public static UsersFix usersFix = new UsersFix();
    public static final String CLIENT_ID = "9aa97fffe29849bb945db5b82b3ee015";
    public static final String CLIENT_SECRET = "37cf34e9fdbd48d389e293fc96d5e794";

    public static void main(String[] args) {
        log.info("START");
        config.readProperties();
        Server.start();
        Hive.start();
    }
}