package com.yyh.gb28181.dispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EventObject;

/**
 * @author: yyh
 * @date: 2022-01-04 15:16
 * @description: AbstractSipDispatcher
 **/
public abstract class AbstractSipDispatcher implements SipDispatcher{

    private final static Logger logger = LoggerFactory.getLogger(AbstractSipDispatcher.class);


    /**
     * 分发sip request response等
     *
     * @param eventObject eventObject
     * @throws Exception Exception
     */
    @Override
    public void doDispatcher(EventObject eventObject) throws Exception {
        this.doBroadcast(eventObject);
    }

    /**
     * 推送消息
     * @param eventObject eventObject
     * @throws Exception Exception
     */
    protected abstract void doBroadcast(EventObject eventObject) throws Exception;
}
