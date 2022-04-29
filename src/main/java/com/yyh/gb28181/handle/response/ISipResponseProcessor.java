package com.yyh.gb28181.handle.response;

import javax.sip.ResponseEvent;

/**
 * @author: yyh
 * @date: 2022-01-04 16:35
 * @description: ISipResponseProcessor
 **/
public interface ISipResponseProcessor {
    /**
     * 处理请求 process
     * @param evt evt
     */
    void process(ResponseEvent evt);
}
