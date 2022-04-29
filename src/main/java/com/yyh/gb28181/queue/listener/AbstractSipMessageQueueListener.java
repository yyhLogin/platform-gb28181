package com.yyh.gb28181.queue.listener;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.yyh.gb28181.component.SipRequestProcessorMapping;
import com.yyh.gb28181.queue.SipMessageQueue;
import com.yyh.gb28181.queue.SipMessageQueueListener;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sip.IOExceptionEvent;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.TimeoutEvent;
import java.util.EventObject;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.yyh.gb28181.utils.XmlUtil.getRootElement;

/**
 * @author: yyh
 * @date: 2022-01-04 14:43
 * @description: AbstractSipMessageQueueListener
 **/
public abstract class AbstractSipMessageQueueListener<T extends SipMessageQueue> implements SipMessageQueueListener {

    private final static Logger logger = LoggerFactory.getLogger(AbstractSipMessageQueueListener.class);

    protected T queue;
    protected SipRequestProcessorMapping mapping;
    public AbstractSipMessageQueueListener(T queue, SipRequestProcessorMapping mapping){
        this.queue = queue;
        this.mapping = mapping;
        executor = new ThreadPoolExecutor(
                5,
                10,
                60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                new ThreadFactoryBuilder()
                        .setNameFormat("consume-td-%d")
                        .setDaemon(true)
                        .build(),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }

    protected ThreadPoolExecutor executor;
    /**
     * 消息处理
     *
     * @param eventObject eventObject
     */
    @Override
    public void consumeMsg(EventObject eventObject) {
        if (eventObject instanceof RequestEvent){
            handleRequest((RequestEvent) eventObject);
        }else if (eventObject instanceof ResponseEvent){
            handleResponse((ResponseEvent) eventObject);
        }else if (eventObject instanceof TimeoutEvent){
            TimeoutEvent timeoutEvent = (TimeoutEvent) eventObject;
            logger.warn("TimeoutEvent:{}",timeoutEvent.getTimeout());
        }else {
            logger.warn("no support:{}",eventObject.toString());
        }

    }

    /**
     * 处理请求 request
     * @param requestEvent requestEvent
     */
    protected abstract void handleRequest(RequestEvent requestEvent);

    /**
     * 处理response
     * @param responseEvent responseEvent
     */
    protected abstract void handleResponse(ResponseEvent responseEvent);
}
