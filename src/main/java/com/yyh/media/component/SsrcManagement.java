package com.yyh.media.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import static com.yyh.media.constants.MediaConstant.SSRC_SERVER;

/**
 * @author: yyh
 * @date: 2022-04-18 17:11
 * @description: SsrcManagement ssrc维护
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class SsrcManagement {

    private final RedisTemplate<String,String> redisTemplate;

    /**
     * 初始化媒体服务器的ssrc
     * @param serverId 媒体服务器id
     */
    public void init(String serverId){
        String key = SSRC_SERVER + serverId;
        redisTemplate.delete(key);
        int ssrcCount = 1000;
        for (int i = 0; i < ssrcCount; i++) {
            String format = String.format("%04d", i + 1);
            redisTemplate.opsForList().leftPush(key,format);
        }
        log.info("初始化ssrc | {}",serverId);
    }


    /**
     * 获取一个可用的ssrc
     * @param serverId 媒体服务器id
     * @return String ssrc
     */
    public synchronized String pop(String serverId){
        String key = SSRC_SERVER + serverId;
        return redisTemplate.opsForList().rightPop(key);
    }

    /**
     * 回收ssrc数据
     * @param serverId 媒体服务器id
     * @param value ssrc
     */
    public synchronized void push(String serverId,String value){
        String key = SSRC_SERVER + serverId;
        redisTemplate.opsForList().leftPush(key, value);
    }
}
