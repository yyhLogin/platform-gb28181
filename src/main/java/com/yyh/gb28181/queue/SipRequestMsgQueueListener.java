package com.yyh.gb28181.queue;

import javax.sip.RequestEvent;
import java.io.IOException;

/**
 * @author: yyh
 * @date: 2021-11-25 16:44
 * @description: SipRequestMsgQueueListener
 **/
public interface SipRequestMsgQueueListener {
    /**
     * 消息处理
     * @param requestEvent requestEvent
     * @throws IOException IOException
     * @throws InterruptedException InterruptedException
     */
    void consumeMsg(RequestEvent requestEvent) throws IOException, InterruptedException;
}
