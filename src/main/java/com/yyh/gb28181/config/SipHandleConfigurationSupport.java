package com.yyh.gb28181.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.yyh.gb28181.callback.SipSubscribe;
import com.yyh.gb28181.dispatcher.DefaultSipDispatcher;
import com.yyh.gb28181.dispatcher.SipDispatcher;
import com.yyh.gb28181.component.SipRequestProcessorMapping;
import com.yyh.gb28181.queue.*;
import com.yyh.gb28181.queue.listener.DefaultSipMessageQueueListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: yyh
 * @date: 2021-11-24 15:34
 * @description: SipHandleConfigurationSupport
 **/
public abstract class SipHandleConfigurationSupport {

    private final static Logger logger = LoggerFactory.getLogger(SipHandleConfigurationSupport.class);

    private final SipMsgProcessorThreadPoolProps poolProps;

    public SipHandleConfigurationSupport(SipMsgProcessorThreadPoolProps poolProps){
        this.poolProps = poolProps;
        logger.info("<<< SipServerConfigurationSupport init ... [{}] >>>", this.getClass());
    }


    /**
     * 注册消息处理
     * @return SipRequestProcessorMapping
     */
    @Bean
    public SipRequestProcessorMapping sipRequestProcessorMapping(){
        return new SipRequestProcessorMapping();
    }

    @Bean
    public SipMessageQueue sipMessageQueue(){
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat(poolProps.getThreadNameFormat())
                .setDaemon(true)
                .build();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                poolProps.getCorePoolSize(),
                poolProps.getMaximumPoolSize(),
                poolProps.getKeepAliveTime().getSeconds(),
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(poolProps.getBlockingQueueSize()),
                threadFactory,
                new ThreadPoolExecutor.AbortPolicy()
        );
        return new DefaultSipEventBus(executor);
    }

    @Bean
    public SipMessageQueueListener sipMessageQueueListener(SipMessageQueue queue,
                                                           SipRequestProcessorMapping mapping,
                                                           SipSubscribe sipSubscribe) {
        return new DefaultSipMessageQueueListener((DefaultSipEventBus) queue,mapping,sipSubscribe);
    }

    @Bean
    public SipDispatcher sipDispatcher(SipMessageQueue sipMessageQueue) {
        return new DefaultSipDispatcher(sipMessageQueue);
    }
}
