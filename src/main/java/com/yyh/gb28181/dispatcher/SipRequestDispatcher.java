package com.yyh.gb28181.dispatcher;


import javax.sip.RequestEvent;
import java.util.EventObject;

/**
 * @author: yyh
 * @date: 2021-11-24 10:00
 * @description: SipRequestDispatcher
 **/
public interface SipRequestDispatcher {

    /**
     * 分发sip request response
     * @param type 消息类型
     * @param eventObject eventObject
     * @throws Exception Exception
     */
    //void doDispatcher(SipEventTypeEnum type,EventObject eventObject) throws Exception;

    /**
     * 分发sip request
     * @param requestEvent requestEvent
     * @throws Exception Exception
     */
    void doDispatcher(RequestEvent requestEvent) throws Exception;
}
