package org.knovash.alicebroker;

import com.sun.net.httpserver.HttpServer;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
@Log4j2
public class Server {

    public static void start() {
        log.info("SERVER STARTING...");
        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress(Main.config.port), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        server.createContext("/favicon.ico", new HandlerFavicon());
        server.createContext("/auth", new HandlerWorkAuth());
        server.createContext("/callback", new HandlerWorkCallback());
        server.createContext("/callbackweb", new HandlerWorkCallbackWeb());
        server.createContext("/alice/", new HandlerAliceVoice());
        server.createContext("/yandex", new HandlerAliceUdy());
        server.createContext("/v1.0/", new HandlerAliceUdy());
        server.createContext("/html", new HandlerHtml());
        server.createContext("/", new HandlerWebAbstract());
//        server.createContext("/index", new HandlerWebAbstract());

        server.createContext("/static", exchange -> {
            String path = exchange.getRequestURI().getPath();
            try (InputStream is = Server.class.getResourceAsStream("/public" + path)) {
                if (is != null) {
                    byte[] data = is.readAllBytes();
                    exchange.sendResponseHeaders(200, data.length);
                    exchange.getResponseBody().write(data);
                } else {
                    exchange.sendResponseHeaders(404, -1);
                }
            }
        });

        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        log.info("SERVER STARTED OK");
    }
}