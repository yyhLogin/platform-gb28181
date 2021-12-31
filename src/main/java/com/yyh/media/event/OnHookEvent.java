package com.yyh.media.event;

import com.yyh.media.subscribe.HookType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

/**
 * @author: yyh
 * @date: 2021-12-22 15:23
 * @description: OnHookEvent
 **/
@Getter
@Setter
public class OnHookEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private Map<String,Object> map;

    private HookType hookType;

    public OnHookEvent(Object source, HookType hookType, Map<String,Object> map) {
        super(source);
        this.hookType = hookType;
        this.map = map;
    }
}
