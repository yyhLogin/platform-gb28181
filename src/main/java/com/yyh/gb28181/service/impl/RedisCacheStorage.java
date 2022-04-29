package com.yyh.gb28181.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yyh.gb28181.constant.VideoManagerConstant;
import com.yyh.gb28181.entity.ParentPlatformCache;
import com.yyh.gb28181.service.IRedisCacheStorage;
import com.yyh.web.entity.ParentPlatform;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * @author: yyh
 * @date: 2022-04-20 16:05
 * @description: PlatformCatchStorage
 **/
@Slf4j
@Service
public class RedisCacheStorage implements IRedisCacheStorage {

    private final RedisTemplate<String,String> redisTemplate;
    private final ObjectMapper mapper;

    public RedisCacheStorage(RedisTemplate<String, String> redisTemplate, ObjectMapper mapper) {
        this.redisTemplate = redisTemplate;
        this.mapper = mapper;
    }


    /**
     * 查找上级平台注册缓存信息
     *
     * @param callId callId
     * @return String
     */
    @Override
    public String queryPlatformRegisterInfo(String callId) {
        return redisTemplate.opsForValue().get(VideoManagerConstant.PLATFORM_REGISTER_INFO+callId);
    }

    /**
     * 查找上级平台注册信息
     *
     * @param platformGbId platformGbId
     * @return ParentPlatformCatch
     */
    @Override
    public ParentPlatformCache queryPlatformCacheInfo(String platformGbId) {
        ParentPlatformCache parentPlatformCatch = null;
        String s = redisTemplate.opsForValue().get(VideoManagerConstant.PLATFORM_CACHE_INFO + platformGbId);
        if (StringUtils.hasLength(s)){
            try {
                parentPlatformCatch = mapper.readValue(s, ParentPlatformCache.class);
            } catch (JsonProcessingException e) {
                log.error("解析上级平台信息出现异常:{}",e.getMessage(),e);
            }
        }
        return parentPlatformCatch;
    }

    /**
     * 移除上级平台注册缓存信息
     *
     * @param callId callId
     */
    @Override
    public void delPlatformRegisterInfo(String callId) {
        redisTemplate.delete(VideoManagerConstant.PLATFORM_REGISTER_INFO+callId);
    }

    /**
     * 移除上级平台注册信息
     *
     * @param platformGbId platformGbId
     */
    @Override
    public void delPlatformCacheInfo(String platformGbId) {
        redisTemplate.delete(VideoManagerConstant.PLATFORM_CACHE_INFO+platformGbId);
    }

    /**
     * 更新平台注册信息
     *
     * @param parentPlatform parentPlatform
     */
    @Override
    public void updatePlatformRegister(ParentPlatform parentPlatform) {
        redisTemplate.opsForValue().set(VideoManagerConstant.PLATFORM_REGISTER+parentPlatform.getServerGbId(),
                "",
                parentPlatform.getExpires(), TimeUnit.SECONDS);
    }

    /**
     * 更新平台心跳信息
     *
     * @param parentPlatform parentPlatform
     */
    @Override
    public void updatePlatformKeepalive(ParentPlatform parentPlatform) {
        redisTemplate.opsForValue().set(VideoManagerConstant.PLATFORM_KEEPALIVE+parentPlatform.getServerGbId(),
                "",
                parentPlatform.getKeepTimeout(), TimeUnit.SECONDS);
    }

    /**
     * 更新上级平台缓存信息
     *
     * @param parentPlatformCatch parentPlatformCatch
     */
    @Override
    public void updatePlatformCacheInfo(ParentPlatformCache parentPlatformCatch) {
        try {
            redisTemplate.opsForValue().set(VideoManagerConstant.PLATFORM_CACHE_INFO+parentPlatformCatch.getId(),
                    mapper.writeValueAsString(parentPlatformCatch));
        } catch (JsonProcessingException e) {
            log.error("更新上级平台缓存信息出现异常:{}",e.getMessage(),e);
        }
    }

    /**
     * 更新平台注册信息
     *
     * @param callIdFromHeader callIdFromHeader
     * @param serverGbId       serverGbId
     */
    @Override
    public void updatePlatformRegisterInfo(String callIdFromHeader, String serverGbId) {
        redisTemplate.opsForValue().set(VideoManagerConstant.PLATFORM_REGISTER_INFO+callIdFromHeader,serverGbId,30,TimeUnit.SECONDS);
    }

    /**
     * 查询是否有当前key
     *
     * @param key key
     * @return
     */
    @Override
    public Boolean hasRegister(String key) {
        return redisTemplate.hasKey(key);
    }
}
