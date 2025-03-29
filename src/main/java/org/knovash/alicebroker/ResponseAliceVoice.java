package org.knovash.alicebroker;

import java.util.Map;

class ResponseAliceVoice {
    public Object version;
    public Object session;
    public Map<String, Object> response;


    public ResponseAliceVoice(Object version, Object session, Map<String, Object> response) {
        this.version = version;
        this.session = session;
        this.response = response;
    }
}
