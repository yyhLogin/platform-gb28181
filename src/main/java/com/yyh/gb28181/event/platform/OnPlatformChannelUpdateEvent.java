package com.yyh.gb28181.event.platform;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * @author: yyh
 * @date: 2022-04-25 15:11
 * @description: OnPlatformChannelUpdateEvent
 **/
@Getter
@Setter
public class OnPlatformChannelUpdateEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    /**
     * 平台id
     */
    private String platformId;

    public OnPlatformChannelUpdateEvent(Object source, String platformId) {
        super(source);
        this.platformId = platformId;
    }
}
