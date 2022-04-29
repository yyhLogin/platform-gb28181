package com.yyh.gb28181.event.platform;

import com.yyh.web.entity.ParentPlatform;
import jdk.jfr.DataAmount;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.time.Clock;

/**
 * @author: yyh
 * @date: 2022-04-20 14:21
 * @description: OnPlatformEvent 平台操作事件
 **/
@Getter
@Setter
public class OnPlatformEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    /**
     * 操作/register/keepalive/logout
     */
    private OnPlatformEnum type;

    private ParentPlatform platform;

    public OnPlatformEvent(Object source,OnPlatformEnum type, ParentPlatform platform) {
        super(source);
        this.type = type;
        this.platform = platform;
    }

    public OnPlatformEvent(Object source, Clock clock) {
        super(source, clock);
    }


}
