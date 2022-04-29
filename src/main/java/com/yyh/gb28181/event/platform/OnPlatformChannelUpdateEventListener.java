package com.yyh.gb28181.event.platform;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author: yyh
 * @date: 2022-04-25 15:15
 * @description: OnPlatformChannelUpdateEventListener
 **/
@Slf4j
@Component
@AllArgsConstructor
public class OnPlatformChannelUpdateEventListener implements ApplicationListener<OnPlatformChannelUpdateEvent> {


    @Override
    public void onApplicationEvent(@NotNull OnPlatformChannelUpdateEvent event) {
        log.info("平台共享设备更新了:{}",event.getPlatformId());
    }
}
