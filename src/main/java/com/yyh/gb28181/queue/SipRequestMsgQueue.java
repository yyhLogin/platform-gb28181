package com.yyh.gb28181.queue;

import javax.sip.RequestEvent;

/**
 * @author: yyh
 * @date: 2021-11-25 16:39
 * @description: SipRequestMsgQueue
 **/
public interface SipRequestMsgQueue {

    /**
     * 向队列里添加数据
     * @param requestEvent requestEvent
     * @throws InterruptedException InterruptedException
     */
    void postMsg(RequestEvent requestEvent) throws InterruptedException;

    /**
     * takeMsg
     * @return RequestEvent
     * @throws InterruptedException InterruptedException
     */
    default RequestEvent takeMsg() throws InterruptedException {
        return null;
    }
}
