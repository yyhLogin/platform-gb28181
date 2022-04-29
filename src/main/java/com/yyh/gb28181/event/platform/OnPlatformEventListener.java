package com.yyh.gb28181.event.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.yyh.gb28181.command.ISipCommander4Platform;
import com.yyh.gb28181.constant.VideoManagerConstant;
import com.yyh.gb28181.entity.ParentPlatformCache;
import com.yyh.gb28181.service.IRedisCacheStorage;
import com.yyh.web.entity.ParentPlatform;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.print.DocFlavor;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author: yyh
 * @date: 2022-04-20 14:25
 * @description: OnPlatformEventListener
 **/
@Slf4j
@Component
@AllArgsConstructor
public class OnPlatformEventListener implements ApplicationListener<OnPlatformEvent> {

    final static ThreadPoolExecutor executor = new ThreadPoolExecutor(
            5,
            10,
            60,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100),
            new ThreadFactoryBuilder()
                    .setNameFormat("future-td-%d")
                    .setDaemon(true)
                    .build(),
            new ThreadPoolExecutor.AbortPolicy()
    );

    /**
     * 心跳失败次数
     */
    private final int count = 3;

    private final ISipCommander4Platform sipCommander4Platform;
    private final IRedisCacheStorage redisCacheStorage;

    private final ObjectMapper mapper;

    @Override
    public void onApplicationEvent(@NotNull OnPlatformEvent event) {
        OnPlatformEnum type = event.getType();
        ParentPlatform platform = event.getPlatform();
        switch (type){
            case REGISTER:
                //平台注册
                register(platform);
                break;
            case LOGOUT:
                break;
            case KEEPALIVE:
                keepalive(platform);
                break;
            default:
                break;
        }
    }

    private void register(ParentPlatform platform){
        ParentPlatformCache cache = new ParentPlatformCache();
        cache.setParentPlatform(platform);
        cache.setId(platform.getServerGbId());
        cache.setKeepAliveReply(0);
        cache.setRegisterAliveReply(0);
        redisCacheStorage.updatePlatformCacheInfo(cache);
        sipCommander4Platform.register(platform,null,null);
    }

    private void keepalive(ParentPlatform platform){
        String serverGbId = platform.getServerGbId();
        ParentPlatformCache cache = redisCacheStorage.queryPlatformCacheInfo(serverGbId);
        if (cache==null){
            log.warn("[ 级联 ]:>>>平台未注册:{}",serverGbId);
            return;
        }
        Boolean aBoolean = redisCacheStorage.hasRegister(VideoManagerConstant.PLATFORM_REGISTER + serverGbId);
        if (aBoolean!=null&&aBoolean) {
            //可以注册
            cache.setParentPlatform(platform);
            if (cache.getKeepAliveReply()>=count){
                log.warn("[ 级联 ]:>>>平台发送心跳失败超过阈值:{} | {}",count,serverGbId);
                //重新注册
            }else {
                cache.setKeepAliveReply(cache.getKeepAliveReply()+1);
                redisCacheStorage.updatePlatformKeepalive(platform);
                //发送心跳
                sipCommander4Platform.keepalive(platform,(eventResult -> {
                    log.info("[ 级联 ]:>>>平台发送心跳回调成功:{}",platform.getServerGbId());
                    cache.setKeepAliveReply(0);
                    redisCacheStorage.updatePlatformCacheInfo(cache);
                }));
            }
        }else {
            log.warn("[ 级联 ]:>>>待发送心跳未注册:{}",serverGbId);
        }
    }
}
