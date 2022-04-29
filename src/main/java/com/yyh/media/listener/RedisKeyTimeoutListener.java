package com.yyh.media.listener;

import com.yyh.gb28181.constant.VideoManagerConstant;
import com.yyh.media.constants.MediaConstant;
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
 * @description: Zlm Keepalive TimeoutListener
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
        if (expiredKey.startsWith(MediaConstant.MEDIA_ONLINE_SERVER)){
            log.info("expiredKey:{}",expiredKey);
            //TODO 媒体服务器掉线
        }
        super.onMessage(message, pattern);
    }
}
