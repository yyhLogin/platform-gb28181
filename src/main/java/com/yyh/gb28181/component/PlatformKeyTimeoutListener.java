package com.yyh.gb28181.component;

import com.yyh.gb28181.callback.EventResult;
import com.yyh.gb28181.callback.SipCallback;
import com.yyh.gb28181.callback.SipSubscribe;
import com.yyh.gb28181.constant.VideoManagerConstant;
import com.yyh.gb28181.event.platform.OnPlatformEnum;
import com.yyh.gb28181.event.platform.OnPlatformEvent;
import com.yyh.web.entity.ParentPlatform;
import com.yyh.web.service.IPlatformService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;

import java.util.EventObject;

/**
 * @author: yyh
 * @date: 2022-04-26 14:32
 * @description: PlatformKeyTimeoutListener
 **/
@Slf4j
@Component
public class PlatformKeyTimeoutListener extends KeyExpirationEventMessageListener {

    private final ApplicationEventPublisher publisher;
    private final IPlatformService platformService;

    private final SipSubscribe sipSubscribe;
    /**
     * Creates new {@link MessageListener} for {@code __keyevent@*__:expired} messages.
     *
     * @param listenerContainer must not be {@literal null}.
     * @param publisher
     * @param platformService
     * @param sipSubscribe
     */
    public PlatformKeyTimeoutListener(RedisMessageListenerContainer listenerContainer,
                                      ApplicationEventPublisher publisher,
                                      IPlatformService platformService, SipSubscribe sipSubscribe) {
        super(listenerContainer);
        this.publisher = publisher;
        this.platformService = platformService;
        this.sipSubscribe = sipSubscribe;
    }

    @Override
    public void onMessage(@NotNull Message message, byte[] pattern) {
        String expiredKey = message.toString();
        if (expiredKey.startsWith(VideoManagerConstant.PLATFORM_KEEPALIVE)){
            // 平台心跳
            log.info("expiredKey:{}",expiredKey);
            String id = expiredKey.substring(expiredKey.lastIndexOf(":") + 1);
            ParentPlatform platform = platformService.queryParentPlatByServerGbId(id);
            publisher.publishEvent(new OnPlatformEvent("keepalive", OnPlatformEnum.KEEPALIVE,platform));
        } else if (expiredKey.startsWith(VideoManagerConstant.PLATFORM_REGISTER)) {
            // 重新注册
            log.info("expiredKey:{}",expiredKey);
            String id = expiredKey.substring(expiredKey.lastIndexOf(":") + 1);
            ParentPlatform platform = platformService.queryParentPlatByServerGbId(id);
            publisher.publishEvent(new OnPlatformEvent("register", OnPlatformEnum.REGISTER,platform));
        } else if (expiredKey.startsWith(VideoManagerConstant.PLATFORM_REGISTER_INFO)){
            // 注册失败
            String callId = expiredKey.substring(VideoManagerConstant.PLATFORM_REGISTER_INFO.length());
            SipCallback errorSubscribe = sipSubscribe.getErrorSubscribe(callId);
            if (errorSubscribe!=null){
                EventResult<EventObject> eventResult = new EventResult<>();
                eventResult.callId = callId;
                eventResult.msg = "注册超时";
                eventResult.type = "register timeout";
                errorSubscribe.response(eventResult);
            }
        }
        super.onMessage(message, pattern);
    }
}
