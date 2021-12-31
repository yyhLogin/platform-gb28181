package com.yyh.media.event;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yyh.media.config.ZlmServerConfig;
import com.yyh.media.constants.MediaConstant;
import com.yyh.media.constants.ServerApiConstant;
import com.yyh.media.service.IMediaServerRestful;
import com.yyh.media.subscribe.HookType;
import com.yyh.media.utils.ConverterUtil;
import com.yyh.web.entity.MediaServer;
import com.yyh.web.service.IMediaServerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author: yyh
 * @date: 2021-12-22 15:31
 * @description: OnHookEventListener
 **/
@Slf4j
@Component
@AllArgsConstructor
public class OnHookEventListener implements ApplicationListener<OnHookEvent> {

    private final RedisTemplate<String,String> redisTemplate;
    private final IMediaServerService mediaServerService;
    private final IMediaServerRestful mediaServerRestful;
    private final ObjectMapper mapper;


    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Async
    @Override
    public void onApplicationEvent(@NotNull OnHookEvent event) {
        HookType hookType = event.getHookType();
        switch (hookType){
            case on_server_keepalive:
                onServerKeepalive(event.getMap());
                break;
            case on_server_started:
                onServerStarted(event.getMap());
                break;
            case on_stream_changed:
                onStreamChanged(event.getMap());
                break;
            case on_shell_login:
                onShellLogin(event.getMap());
                break;
            case on_stream_none_reader:
                onStreamNoneReader(event.getMap());
                break;
            case on_stream_not_found:
                onStreamNotFound(event.getMap());
                break;
            default:
                break;
        }
    }


    /**
     * 服务端心跳
     * @param map map
     */
    private void onServerKeepalive(Map<String,Object> map){
        String mediaServerId = MapUtil.getStr(map,"mediaServerId");
        if (StrUtil.isNotBlank(mediaServerId)) {
            List<MediaServer> list = mediaServerService.list(Wrappers.<MediaServer>lambdaQuery()
                    .eq(MediaServer::getServerId, mediaServerId));
            if (list==null||list.size()==0){
                log.warn("媒体:{}未注册",mediaServerId);
                return;
            }
            try {
                redisTemplate.opsForValue().set(
                        MediaConstant.MEDIA_ONLINE_SERVER+mediaServerId,
                        mapper.writeValueAsString(map),
                        list.get(0).getHookAliveInterval() + 5,
                        TimeUnit.SECONDS);
                log.info("[ ZLM HOOK ]:on_server_keepalive:{}->update cache:{}",mediaServerId,true);
            } catch (JsonProcessingException e) {
                log.error("[ ZLM HOOK ]:on_server_keepalive:{}->has error {}",mediaServerId,e.getMessage(),e);
            }
        }
    }

    /**
     * 媒体服务启动
     * @param map map
     */
    private void onServerStarted(Map<String,Object> map){
        String mediaServerId = MapUtil.getStr(map,"mediaServerId");
        if (StrUtil.isNotBlank(mediaServerId)){
            List<MediaServer> list = mediaServerService.list(Wrappers.<MediaServer>lambdaQuery()
                    .eq(MediaServer::getServerId, mediaServerId));
            if (list==null||list.size()==0){
                return;
            }
            MediaServer mediaServer = list.get(0);
            String ip = MapUtil.getStr(map, "ip");
            Integer port = MapUtil.getInt(map, "http.port");
            String secret = MapUtil.getStr(map, "api.secret");
            String url = String.format(ServerApiConstant.GET_SERVER_CONFIG,ip,port,secret);
            ArrayList<Map<String, Object>> arrayList = mediaServerRestful.queryMediaServerInfo(url);
            if (arrayList!=null&&arrayList.size()>0){
                for (Map<String,Object> item :arrayList){
                    try {
                        ZlmServerConfig config = mapper.readValue(mapper.writeValueAsString(item), ZlmServerConfig.class);
                        ConverterUtil.converterZlMediaServerConfig2MediaServer(config,mediaServer);
                        mediaServer.setUpdateTime(LocalDateTime.now());
                        mediaServerService.updateById(mediaServer);
                        redisTemplate.opsForValue().set(
                                MediaConstant.MEDIA_SERVER+config.getGeneralMediaServerId(),
                                    mapper.writeValueAsString(config));
                    } catch (JsonProcessingException e) {
                        log.error("json格式化异常:{}",e.getMessage(),e);
                    }
                }
            }
        }
        log.info("[ ZLM HOOK ]:on_server_starter:{}",mediaServerId);
    }

    private void onStreamChanged(Map<String, Object> map) {
        //log.info("[ ZLM HOOK ]:on_stream_changed:{}",map);
    }


    private void onShellLogin(Map<String, Object> map) {
        //.info("[ ZLM HOOK ]:on_shell_login:{}",map);
    }
    private void onStreamNoneReader(Map<String, Object> map) {
    }

    private void onStreamNotFound(Map<String, Object> map) {
    }
}
