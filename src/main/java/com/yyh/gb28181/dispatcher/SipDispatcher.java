package com.yyh.gb28181.dispatcher;


import java.util.EventObject;

/**
 * @author: yyh
 * @date: 2022-01-04 15:15
 * @description: SipDispatcher
 **/
public interface SipDispatcher {

    /**
     * 分发sip request response 等
     * @param eventObject eventObject
     * @throws Exception Exception
     */
    void doDispatcher(EventObject eventObject) throws Exception;
}
