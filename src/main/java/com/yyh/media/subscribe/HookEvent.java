package com.yyh.media.subscribe;

import com.yyh.web.entity.MediaServer;

import java.util.Map;

/**
 * @author: yyh
 * @date: 2021-12-22 10:01
 * @description: Event
 **/
public interface HookEvent {
    /**
     * 处理回调
     * @param mediaServer mediaServer
     * @param response response
     */
    void response(MediaServer mediaServer, Map<String,Object> response);
}
