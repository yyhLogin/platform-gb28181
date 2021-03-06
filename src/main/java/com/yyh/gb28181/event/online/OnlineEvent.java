package com.yyh.gb28181.event.online;

import com.yyh.web.entity.GbDevice;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.io.Serializable;

/**
 * @author: yyh
 * @date: 2021-12-06 13:58
 * @description: OnlineEvent
 **/
@Getter
@Setter
public class OnlineEvent extends ApplicationEvent implements Serializable {


    private static final long serialVersionUID = 1L;

    private String id;

    private GbDevice device;

    private String from;

    public OnlineEvent(Object source) {
        super(source);
    }
}
