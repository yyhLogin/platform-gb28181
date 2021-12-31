package com.yyh.gb28181.queue;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.SubscriberExceptionHandler;

import javax.sip.RequestEvent;
import java.util.concurrent.Executor;

/**
 * @author: yyh
 * @date: 2021-11-25 16:41
 * @description: DefaultSipRequestEventBus
 **/
public class DefaultSipRequestEventBus extends AsyncEventBus implements SipRequestMsgQueue {

    public DefaultSipRequestEventBus(String identifier, Executor executor) {
        super(identifier, executor);
    }

    public DefaultSipRequestEventBus(Executor executor, SubscriberExceptionHandler subscriberExceptionHandler) {
        super(executor, subscriberExceptionHandler);
    }

    public DefaultSipRequestEventBus(Executor executor) {
        super(executor);
    }

    /**
     * 向队列里添加数据
     *
     * @param requestEvent requestEvent
     * @throws InterruptedException InterruptedException
     */
    @Override
    public void postMsg(RequestEvent requestEvent) throws InterruptedException {
        super.post(requestEvent);
    }
}
