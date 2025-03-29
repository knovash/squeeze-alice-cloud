package org.knovash.alicebroker;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class HandlerAliceUdy extends HandlerAbstract {

    @Override
    protected Context processContext(Context context) {
        log.info("HANDLER ALICE UDY START >>>");
        String contextJson = Hive.publishContextWaitForContext(Hive.topicUdyPublish, context);

//        Hive.sendToTopicText(users.users.get(0).getUserId(), "TEST MESSAGE");

        context = Context.fromJson(contextJson);
        log.info("HANDLER ALICE UDY FINISH <<<<<");
        return context;
    }
}