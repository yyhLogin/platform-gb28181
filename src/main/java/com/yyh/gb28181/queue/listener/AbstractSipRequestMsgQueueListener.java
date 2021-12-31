package com.yyh.gb28181.queue.listener;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.yyh.gb28181.constant.SipRequestConstant;
import com.yyh.gb28181.component.SipRequestProcessorMapping;
import com.yyh.gb28181.queue.SipRequestMsgQueue;
import com.yyh.gb28181.queue.SipRequestMsgQueueListener;
import com.yyh.gb28181.utils.XmlUtil;
import gov.nist.javax.sip.RequestEventExt;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;

import javax.sip.RequestEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.yyh.gb28181.utils.XmlUtil.getRootElement;

/**
 * @author: yyh
 * @date: 2021-11-25 16:46
 * @description: AbstractSipRequestMsgQueueListener
 **/
public abstract class AbstractSipRequestMsgQueueListener<T extends SipRequestMsgQueue> implements SipRequestMsgQueueListener {

    private final static Logger logger = LoggerFactory.getLogger(AbstractSipRequestMsgQueueListener.class);

    protected T queue;
    protected SipRequestProcessorMapping mapping;
    public AbstractSipRequestMsgQueueListener(T queue, SipRequestProcessorMapping mapping){
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
     * @param requestEvent requestEvent
     */
    @Override
    public void consumeMsg(RequestEvent requestEvent) {
        String method = requestEvent.getRequest().getMethod();
        try {
            if (SipRequestConstant.MESSAGE.equals(method)){
                RequestEventExt evt = (RequestEventExt)requestEvent;
                Element rootElement = getRootElement(evt);
                /// 根节点
                method += "/" +rootElement.getName();
                /// CmdType
                method += "/"+XmlUtil.getText(rootElement, "CmdType");
            }
        }catch (DocumentException e){
            logger.error("consumeMsg error -> {}",e.getMessage(),e);
        }
        logger.info("consumeMsg->{}",method);
        List<HandlerMethod> handlerMethods = mapping.getHandlerMethodsForMappingName(method.toUpperCase());
        if (handlerMethods==null){
            logger.info("consumeMsg -> {} no handle",requestEvent.getRequest().getMethod());
            return;
        }
        executor.execute(()->{
            try {
                HandlerMethod handlerMethod = handlerMethods.get(0);
                handlerMethod.getMethod().invoke(handlerMethod.getBean(),requestEvent);
            } catch (IllegalAccessException | InvocationTargetException e) {
                logger.error("consumeMsg error -> {}",e.getMessage(),e);
            }
        });
    }
}
