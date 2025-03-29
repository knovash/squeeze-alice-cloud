package org.knovash.alicebroker;

public class HandlerWebAbstract extends HandlerAbstract {

    @Override
    protected Context processContext(Context context) {

        System.out.println("HANDLER WEB");
//        генерация html страницы
        context.bodyResponse = PageIndex.page();
        context.code = 200;
//        context.headers.add("Content-Type", "text/html; charset=UTF-8");
        System.out.println("HANDLER WEB FINISH");
        return context;
    }
}