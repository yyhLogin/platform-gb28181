package com.yyh.gb28181.queue.listener;

import com.google.common.eventbus.Subscribe;
import com.yyh.gb28181.component.SipRequestProcessorMapping;
import com.yyh.gb28181.queue.DefaultSipRequestEventBus;

import javax.annotation.PostConstruct;
import javax.sip.RequestEvent;
import java.io.IOException;

/**
 * @author: yyh
 * @date: 2021-11-25 16:50
 * @description: LocalSipRequestMsgQueueListener
 **/
public class DefaultSipRequestMsgQueueListener extends AbstractSipRequestMsgQueueListener<DefaultSipRequestEventBus> {

    public DefaultSipRequestMsgQueueListener(DefaultSipRequestEventBus queue, SipRequestProcessorMapping mapping) {
        super(queue,mapping);
    }
    @PostConstruct
    public void init() {
        queue.register(this);
    }

    @Subscribe
    public void listen(RequestEvent requestEvent) throws IOException, InterruptedException {
        consumeMsg(requestEvent);
    }
}


