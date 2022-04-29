package com.yyh.gb28181.callback;


import java.util.EventObject;

/**
 * @author: yyh
 * @date: 2021-12-08 15:46
 * @description: SipCallback 回调函数
 **/
public interface SipCallback {
    /**
     * sip 回调
     * @param eventResult eventResult
     */
    void response(EventResult<EventObject> eventResult);
}

