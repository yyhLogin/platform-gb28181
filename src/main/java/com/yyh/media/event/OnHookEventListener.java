package com.yyh.media.event;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yyh.config.ConverterType;
import com.yyh.gb28181.callback.EventResult;
import com.yyh.gb28181.command.ISipCommander;
import com.yyh.media.callback.CallbackPlayDeferredHandle;
import com.yyh.media.component.SsrcManagement;
import com.yyh.media.config.ZlmServerConfig;
import com.yyh.media.constants.MediaConstant;
import com.yyh.media.constants.ServerApiConstant;
import com.yyh.media.session.StreamSessionManager;
import com.yyh.media.subscribe.PlayHookSubscribe;
import com.yyh.media.service.IMediaServerRestful;
import com.yyh.media.service.IMediaStreamService;
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

import javax.sip.ResponseEvent;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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

    private final Map<String, Set<String>> streamMap = new ConcurrentHashMap<>();
    /**
     * 流类型
     */
    private final List<String> SCHEMA_LIST = Arrays.asList("hls","rtsp","rtmp","ts","fmp4");

    private final RedisTemplate<String,String> redisTemplate;
    private final IMediaServerService mediaServerService;
    private final IMediaServerRestful mediaServerRestful;
    private final ObjectMapper mapper;
    private final CallbackPlayDeferredHandle callbackPlayDeferredHandle;
    private final IMediaStreamService streamService;
    private final PlayHookSubscribe playHookSubscribe;
    private final StreamSessionManager streamSessionManager;
    private final ISipCommander sipCommander;
    private final SsrcManagement ssrcManagement;

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

    /**
     * 流变化hook
     * @param map map
     */
    private void onStreamChanged(Map<String, Object> map) {
        String app = MapUtil.getStr(map, "app");
        String stream = MapUtil.getStr(map, "stream");
        String schema = MapUtil.getStr(map, "schema");
        boolean register = MapUtil.getBool(map, "regist");
        if (register){
            streamService.putStream(MediaConstant.STREAM_SERVER+stream,schema,map);
            if (MediaConstant.APP_RTP.equalsIgnoreCase(app) &&
                    MediaConstant.SCHEMA_RTMP.equalsIgnoreCase(schema)){
                playHookSubscribe.invokeSubscribe(stream,map);
            }
        }else {
            //移除流
            String mediaServerId = MapUtil.getStr(map, "mediaServerId");
            streamService.removeStream(MediaConstant.STREAM_SERVER+stream,schema);
            //注销ssrc
            String deviceId = stream.split("_")[0];
            String channelId = stream.split("_")[1];
            if (MediaConstant.SCHEMA_RTMP.equalsIgnoreCase(schema)){
                redisTemplate.delete(MediaConstant.PLAY_SERVER+deviceId+"_"+channelId);
            }
            String ssrc = streamSessionManager.getSSRC(deviceId, channelId);
            if (StrUtil.isNotBlank(ssrc)){
                if (MediaConstant.SCHEMA_RTMP.equalsIgnoreCase(schema)){
                    log.info("准备发送bye指令");
                    sipCommander.streamByeCmd(deviceId,channelId,(event)->{
                        ResponseEvent responseEvent = (ResponseEvent) event.event;
                        log.info("发送bye指令成功{}",responseEvent);
                    });
                }
                streamSessionManager.remove(deviceId,channelId);
                //重置ssrc
                String substring = ssrc.substring(ssrc.length() - 4);
                ssrcManagement.push(mediaServerId,substring);
//                String key = MediaConstant.SSRC_SERVER+mediaServerId;
//                Object unused = redisTemplate.opsForHash().get(key, "unused");
//                Object used = redisTemplate.opsForHash().get(key, "used");
//                try {
//                    assert unused != null;
//                    assert used != null;
//                    ArrayList<String> un = mapper.readValue(unused.toString(), ConverterType.ARRAY_LIST_STRING_TYPE);
//                    ArrayList<String> u = mapper.readValue(used.toString(), ConverterType.ARRAY_LIST_STRING_TYPE);
//                    un.add(substring);
//                    u.remove(substring);
//                    redisTemplate.opsForHash().put(key,"unused",mapper.writeValueAsString(un));
//                    redisTemplate.opsForHash().put(key,"used",mapper.writeValueAsString(u));
//                } catch (JsonProcessingException e) {
//                    log.error("重置ssrc出现异常:{}",e.getMessage(),e);
//                }
            }
        }
    }


    private void onShellLogin(Map<String, Object> map) {
        //.info("[ ZLM HOOK ]:on_shell_login:{}",map);
    }

    /**
     * 流无人播放
     * @param map map
     */
    private void onStreamNoneReader(Map<String, Object> map) {
        log.info("流无人播放:{}",map);
        String serverId = MapUtil.getStr(map, "mediaServerId");
        String app = MapUtil.getStr(map, "app");
        String stream = MapUtil.getStr(map, "stream");
        String schema = MapUtil.getStr(map, "schema");
        //TODO 校验该流是否在推流，如果在推流则不用关闭流
        if (MediaConstant.APP_RTP.equalsIgnoreCase(app)){
            MediaServer mediaServer = mediaServerService.queryMediaServerByServerId(serverId);
            String deviceId = stream.split("_")[0];
            String channelId = stream.split("_")[1];
            sipCommander.streamByeCmd(deviceId,channelId);
            String url = String.format(ServerApiConstant.CLOSE_RTP_SERVER,
                    mediaServer.getIp(),
                    mediaServer.getHttpPort());
            Map<String, Object> res = mediaServerRestful.closeRtpServer(url, mediaServer.getSecret(), deviceId + "_" + channelId);
            Integer code = MapUtil.getInt(res, "code");
            if (MediaConstant.RESTFUL_SUCCESS == code){
                log.info("closeRtpServer success | {}",deviceId+"_"+channelId);
            }else {
                log.error("closeRtpServer error  | {} , code -> {}",deviceId+"_"+channelId,code);
            }
        }
    }

    private void onStreamNotFound(Map<String, Object> map) {
    }
}
