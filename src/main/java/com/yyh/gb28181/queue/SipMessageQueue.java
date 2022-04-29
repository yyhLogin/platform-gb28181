package com.yyh.gb28181.queue;


import java.util.EventObject;

/**
 * @author: yyh
 * @date: 2021-12-31 17:44
 * @description: SipMsgQueue
 * sip服务器处理消息队列
 **/
public interface SipMessageQueue {

    /**
     * 向队列里添加数据
     * @param eventObject 消息
     * @throws InterruptedException InterruptedException
     */
    void postMsg(EventObject eventObject) throws InterruptedException;

}
