package com.yyh.gb28181.service;

import com.yyh.gb28181.entity.ParentPlatformCache;
import com.yyh.web.entity.ParentPlatform;

/**
 * @author: yyh
 * @date: 2022-04-20 16:04
 * @description: IPlatformCacheStorage
 **/
public interface IRedisCacheStorage {

    /**
     * 查找上级平台注册缓存信息
     * @param callId callId
     * @return String
     */
    String queryPlatformRegisterInfo(String callId);

    /**
     * 查找上级平台注册信息
     * @param platformGbId platformGbId
     * @return ParentPlatformCatch
     */
    ParentPlatformCache queryPlatformCacheInfo(String platformGbId);

    /**
     * 移除上级平台注册缓存信息
     * @param callId callId
     */
    void delPlatformRegisterInfo(String callId);


    /**
     * 移除上级平台注册信息
     * @param platformGbId platformGbId
     */
    void delPlatformCacheInfo(String platformGbId);

    /**
     * 更新平台注册信息
     * @param parentPlatform parentPlatform
     */
    void updatePlatformRegister(ParentPlatform parentPlatform);

    /**
     * 更新平台心跳信息
     * @param parentPlatform parentPlatform
     */
    void updatePlatformKeepalive(ParentPlatform parentPlatform);

    /**
     * 更新上级平台缓存信息
     * @param parentPlatformCatch parentPlatformCatch
     */
    void updatePlatformCacheInfo(ParentPlatformCache parentPlatformCatch);

    /**
     * 更新平台注册信息
     * @param callIdFromHeader callIdFromHeader
     * @param serverGbId serverGbId
     */
    void updatePlatformRegisterInfo(String callIdFromHeader, String serverGbId);

    /**
     * 查询是否有当前key
     * @param key key
     * @return
     */
    Boolean hasRegister(String key);
}
