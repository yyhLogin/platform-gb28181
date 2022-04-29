package com.yyh.gb28181.queue;

import java.io.IOException;
import java.util.EventObject;

/**
 * @author: yyh
 * @date: 2022-01-04 14:44
 * @description: SipMessageQueueListener
 **/
public interface SipMessageQueueListener {
    /**
     * 消息处理
     * @param eventObject eventObject
     * @throws IOException IOException
     * @throws InterruptedException InterruptedException
     */
    void consumeMsg(EventObject eventObject) throws IOException, InterruptedException;
}
