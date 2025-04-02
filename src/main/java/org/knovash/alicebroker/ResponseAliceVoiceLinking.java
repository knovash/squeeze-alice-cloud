package org.knovash.alicebroker;

class ResponseAliceVoiceLinking {

    public Object version;
    public Object session;
    public Object start_account_linking;


    public ResponseAliceVoiceLinking(Object version, Object session, Object start_account_linking) {
        this.version = version;
        this.session = session;
        this.start_account_linking = start_account_linking;
    }
}
