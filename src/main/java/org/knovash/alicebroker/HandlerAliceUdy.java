package org.knovash.alicebroker;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class HandlerAliceUdy extends HandlerAbstract {

    @Override
    protected Context processContext(Context context) {
        log.info("HANDLER ALICE UDY START >>>");
        String contextJson = Hive.publishContextWaitForContext(Hive.topicUdyPublish + uid, context);
        context = Context.fromJson(contextJson);
        log.info("HANDLER ALICE UDY FINISH <<<");
        return context;
    }
}