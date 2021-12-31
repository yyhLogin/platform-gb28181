package com.yyh.gb28181.queue;

import javax.sip.RequestEvent;
import java.util.EventObject;

/**
 * @author: yyh
 * @date: 2021-12-31 17:44
 * @description: SipMsgQueue
 * sip服务器处理消息队列
 **/
public interface SipMsgQueue {

    /**
     * 向队列里添加数据
     * @param eventObject eventObject
     * @throws InterruptedException InterruptedException
     */
    void postMsg(EventObject eventObject) throws InterruptedException;

    /**
     * takeMsg
     * @return RequestEvent
     * @throws InterruptedException InterruptedException
     */
    default RequestEvent takeMsg() throws InterruptedException {
        return null;
    }
}
