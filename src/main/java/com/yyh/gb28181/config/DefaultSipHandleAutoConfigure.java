package com.yyh.gb28181.config;

import org.springframework.stereotype.Component;

/**
 * @author: yyh
 * @date: 2021-11-24 16:08
 * @description: DefaultSipHandleAutoConfigure
 **/
@Component
public class DefaultSipHandleAutoConfigure extends SipHandleConfigurationSupport{

    public DefaultSipHandleAutoConfigure(SipMsgProcessorThreadPoolProps poolProps) {
        super(poolProps);
    }
}
