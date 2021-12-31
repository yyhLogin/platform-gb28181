package com.yyh.gb28181.dispatcher;

import com.yyh.gb28181.queue.SipRequestMsgQueue;

import javax.sip.RequestEvent;

/**
 * @author: yyh
 * @date: 2021-11-25 18:32
 * @description: DefaultSipRequestDispatcher
 **/
public class DefaultSipRequestDispatcher extends AbstractSipRequestDispatcher {

    private final SipRequestMsgQueue eventBus;

    public DefaultSipRequestDispatcher(SipRequestMsgQueue eventBus) {
        this.eventBus = eventBus;
    }

    /**
     * 解析request
     *
     * @param requestEvent requestEvent
     * @throws Exception Exception
     */
    @Override
    public void doBroadcast(RequestEvent requestEvent) throws Exception {
        eventBus.postMsg(requestEvent);
    }
}
