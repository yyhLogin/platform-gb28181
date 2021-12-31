package com.yyh.media.listener;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

/**
 * @author: yyh
 * @date: 2021-12-22 17:40
 * @description: ZlmKeepliveTimeoutListener
 **/
@Slf4j
@Component
public class RedisKeyTimeoutListener extends KeyExpirationEventMessageListener {

    /**
     * Creates new {@link MessageListener} for {@code __keyevent@*__:expired} messages.
     *
     * @param listenerContainer must not be {@literal null}.
     */
    public RedisKeyTimeoutListener(RedisMessageListenerContainer listenerContainer) {
        super(listenerContainer);
    }

    @Override
    public void onMessage(@NotNull Message message, byte[] pattern) {
        String expiredKey = message.toString();
        log.info("expiredKey:{}",expiredKey);
        super.onMessage(message, pattern);
    }
}
