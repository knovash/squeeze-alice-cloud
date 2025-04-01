package org.knovash.alicebroker;

import com.sun.net.httpserver.HttpServer;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
@Log4j2
public class Server {

    public static void start() {
        System.out.println("SERVER STARTING...");
        HttpServer server;
        try {
            server = HttpServer.create(new InetSocketAddress(Main.config.port), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        иконка для браузера
        server.createContext("/favicon.ico", new HandlerFavicon());




//        server.createContext("/success", new HandlerSuccess());

//        server.createContext("/success", exchange -> {
//            String html = "<html><body>Authorization complete! You can close this window.</body></html>";
//            exchange.getResponseHeaders().set("Content-Type", "text/html");
//            exchange.sendResponseHeaders(200, html.getBytes().length);
//            try (OutputStream os = exchange.getResponseBody()) {
//                os.write(html.getBytes());
//            }
//        });

        server.createContext("/success", exchange -> {
            String html = "<html>"
                    + "<head><script>"
                    + "function parseHash() {"
                    + "  var hash = window.location.hash.substring(1);"
                    + "  var params = new URLSearchParams(hash);"
                    + "  window.opener.postMessage({"
                    + "    type: 'oauth',"
                    + "    access_token: params.get('access_token'),"
                    + "    expires_in: params.get('expires_in'),"
                    + "    token_type: params.get('token_type')"
                    + "  }, window.opener.location.origin);"
                    + "  window.close();"
                    + "}"
                    + "</script></head>"
                    + "<body onload='parseHash()'></body>"
                    + "</html>";

            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            byte[] response = html.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });

        //        авторизация в Яндекс
//        server.createContext("/auth", new AuthHandler());
//        server.createContext("/callback", new CallbackHandler());

        server.createContext("/auth", new HandlerWorkAuth());
        server.createContext("/callback", new HandlerWorkCallback());
        server.createContext("/callbackweb", new HandlerWorkCallbackWeb());

        server.createContext("/alice/", new HandlerAliceVoice());
        server.createContext("/yandex", new HandlerAliceUdy());
        server.createContext("/v1.0/", new HandlerAliceUdy());
        server.createContext("/html", new HandlerHtml());
        server.createContext("/", new HandlerWebAbstract());
        server.createContext("/index", new HandlerWebAbstract());

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
        System.out.println("SERVER STARTED OK");
    }
}