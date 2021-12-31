package com.yyh.gb28181.handle.request;

import javax.sip.RequestEvent;

/**
 * @author: yyh
 * @date: 2021-11-24 16:23
 * @description: SipRequestProcessor
 **/
public interface SipRequestProcessor{

    /**
     * 处理消息
     * @param requestEvent requestEvent
     */
    void dispatch(RequestEvent requestEvent);
}


