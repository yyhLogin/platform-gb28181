package com.yyh.gb28181.dispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sip.RequestEvent;
import java.util.EventObject;

/**
 * @author: yyh
 * @date: 2021-11-24 10:22
 * @description: AbstractSipRequestDispatcher
 **/
public abstract class AbstractSipRequestDispatcher implements SipRequestDispatcher{

    private final static Logger logger = LoggerFactory.getLogger(AbstractSipRequestDispatcher.class);
    /**
     * 分发sip request
     * @param requestEvent requestEvent
     * @throws Exception Exception
     */
    @Override
    public void doDispatcher(RequestEvent requestEvent) throws Exception {
//        if (SipEventTypeEnum.REQUEST == type){
//            RequestEvent requestEvent = (RequestEvent) eventObject;
//            String method = requestEvent.getRequest().getMethod();
//            logger.info("dispatcher->{}",method);
//            this.doBroadcast(requestEvent);
//        }
        String method = requestEvent.getRequest().getMethod();
        logger.info("dispatcher->{}",method);
        this.doBroadcast(requestEvent);

    }

    /**
     * 解析request
     * @param requestEvent requestEvent
     * @throws Exception Exception
     */
    public abstract void doBroadcast(RequestEvent requestEvent) throws Exception;
}
