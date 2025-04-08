package org.knovash.alicebroker.spotify;

import lombok.extern.log4j.Log4j2;
import org.knovash.alicebroker.Context;


@Log4j2
public class PageSpotiCallback {

    public static Context action(Context context) {
        context.bodyResponse = page();
        context.code = 200;
        return context;
    }

    public static String page() {
        String page = "<!doctype html><html lang=\"ru\">\n" +
                "<head>\n" +
                "<meta charSet=\"utf-8\" />\n" +
                "<title>Spotify callback</title>" +
                "</head>\n" +
                "<body> \n" +
                "<p><a href=\"/\">Home</a></p>" +
                "<p><strong>Spotify callback</strong></p> \n" +

                "</body>\n" +
                "</html>";
        log.info("bearerToken: " + SpotifyAuth.bearer_token);
        return page;
    }
}