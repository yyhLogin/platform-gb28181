package com.yyh.gb28181.queue;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.SubscriberExceptionHandler;

import java.util.EventObject;
import java.util.concurrent.Executor;

/**
 * @author: yyh
 * @date: 2022-01-04 14:06
 * @description: DefaultSipEventBus
 **/
public class DefaultSipEventBus extends AsyncEventBus implements SipMessageQueue {
    /**
     * Creates a new AsyncEventBus that will use {@code executor} to dispatch events.
     *
     * @param executor Executor to use to dispatch events. It is the caller's responsibility to shut
     *                 down the executor after the last event has been posted to this event bus.
     */
    public DefaultSipEventBus(Executor executor) {
        super(executor);
    }

    /**
     * Creates a new AsyncEventBus that will use {@code executor} to dispatch events. Assigns {@code
     * identifier} as the bus's name for logging purposes.
     *
     * @param identifier short name for the bus, for logging purposes.
     * @param executor   Executor to use to dispatch events. It is the caller's responsibility to shut
     */
    public DefaultSipEventBus(String identifier, Executor executor) {
        super(identifier, executor);
    }

    /**
     * Creates a new AsyncEventBus that will use {@code executor} to dispatch events.
     *
     * @param executor                   Executor to use to dispatch events. It is the caller's responsibility to shut
     *                                   down the executor after the last event has been posted to this event bus.
     * @param subscriberExceptionHandler Handler used to handle exceptions thrown from subscribers.
     *                                   See {@link SubscriberExceptionHandler} for more information.
     * @since 16.0
     */
    public DefaultSipEventBus(Executor executor, SubscriberExceptionHandler subscriberExceptionHandler) {
        super(executor, subscriberExceptionHandler);
    }

    /**
     * 向队列里添加数据
     *
     * @param eventObject 消息
     * @throws InterruptedException InterruptedException
     */
    @Override
    public void postMsg(EventObject eventObject) throws InterruptedException {
        super.post(eventObject);
    }
}
