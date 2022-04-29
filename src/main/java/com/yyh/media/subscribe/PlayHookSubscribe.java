package com.yyh.media.subscribe;

import cn.hutool.core.map.MapUtil;
import com.yyh.web.entity.MediaServer;
import com.yyh.web.service.IMediaServerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author: yyh
 * @date: 2022-01-07 14:24
 * @description: PlayHookSubscribe 播放hook订阅回调
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class PlayHookSubscribe extends AbstractHookSubscribe<String>{

    private final IMediaServerService mediaServerService;

    /**
     * 执行订阅
     *
     * @param key key
     * @param map map
     */
    @Override
    public void invokeSubscribe(String key, Map<String, Object> map) {
        HookEvent hook = HOOK_MAP.get(key);
        if (hook!=null){
            String serverId = MapUtil.getStr(map,"mediaServerId");
            MediaServer mediaServer = mediaServerService.queryMediaServerByServerId(serverId);
            if (mediaServer==null){
                log.warn("获取媒体服务信息为空:{}",serverId);
                return;
            }
            hook.response(mediaServer,map);
        }
        HOOK_MAP.remove(key);
    }
}
