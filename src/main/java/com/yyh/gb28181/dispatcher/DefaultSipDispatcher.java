package com.yyh.gb28181.dispatcher;

import com.yyh.gb28181.queue.SipMessageQueue;

import java.util.EventObject;

/**
 * @author: yyh
 * @date: 2022-01-04 15:18
 * @description: DefaultSipDispatcher
 **/
public class DefaultSipDispatcher extends AbstractSipDispatcher{

    private final SipMessageQueue eventBus;

    public DefaultSipDispatcher(SipMessageQueue eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * 推送消息
     *
     * @param eventObject eventObject
     * @throws Exception Exception
     */
    @Override
    protected void doBroadcast(EventObject eventObject) throws Exception {
        this.eventBus.postMsg(eventObject);
    }
}
