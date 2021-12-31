package com.yyh.gb28181.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.yyh.gb28181.dispatcher.DefaultSipRequestDispatcher;
import com.yyh.gb28181.dispatcher.SipRequestDispatcher;
import com.yyh.gb28181.component.SipRequestProcessorMapping;
import com.yyh.gb28181.queue.DefaultSipRequestEventBus;
import com.yyh.gb28181.queue.SipRequestMsgQueue;
import com.yyh.gb28181.queue.SipRequestMsgQueueListener;
import com.yyh.gb28181.queue.listener.DefaultSipRequestMsgQueueListener;
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


    @Bean
    public SipRequestProcessorMapping sipRequestProcessorMapping(){
        return new SipRequestProcessorMapping();
    }

    @Bean
    public SipRequestMsgQueue requestMsgQueue() {
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
        return new DefaultSipRequestEventBus(executor);
    }

    @Bean
    public SipRequestMsgQueueListener msgQueueListener(SipRequestMsgQueue queue,SipRequestProcessorMapping mapping) {
        return new DefaultSipRequestMsgQueueListener((DefaultSipRequestEventBus) queue,mapping);
    }

    @Bean
    public SipRequestDispatcher requestMsgDispatcher(SipRequestMsgQueue sipRequestMsgQueue) {
        return new DefaultSipRequestDispatcher(sipRequestMsgQueue);
    }

}
