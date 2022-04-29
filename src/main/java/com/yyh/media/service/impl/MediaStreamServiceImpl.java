package com.yyh.media.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yyh.media.entity.StreamInfo;
import com.yyh.media.service.IMediaStreamService;
import com.yyh.web.entity.MediaServer;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author: yyh
 * @date: 2022-01-06 14:22
 * @description: MediaStreamServiceImpl
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class MediaStreamServiceImpl  implements IMediaStreamService {

    private final RedisTemplate<String,String> redisTemplate;
    private final ObjectMapper mapper;
    /**
     * 提交媒体流信息
     *
     * @param key     key
     * @param hashKey hashKey
     * @param value   value
     */
    @Override
    public void putStream(String key, String hashKey, Map<String, Object> value) {
        try {
            redisTemplate.opsForHash().put(key,hashKey,mapper.writeValueAsString(value));
        } catch (JsonProcessingException e) {
            log.error("提交媒体流信息出现异常:{}",e.getMessage(),e);
        }
    }

    /**
     * 注销流
     *
     * @param key     key
     * @param hashKey hashKey
     */
    @Override
    public void removeStream(String key, String hashKey) {
        redisTemplate.opsForHash().delete(key,hashKey);
    }

    /**
     * 生成视频流播放信息
     *
     * @param mediaServer mediaServer
     * @param app         app
     * @param stream      stream
     * @param tracks      tracks
     * @return StreamInfo
     */
    @Override
    public StreamInfo generateStreamInfo(MediaServer mediaServer,
                                         String app,
                                         String stream,
                                         Object tracks) {
        return generateStreamInfo(mediaServer,app,stream,tracks,null);
    }

    /**
     * 生成视频流播放信息
     *
     * @param mediaServer mediaServer
     * @param app         app
     * @param stream      stream
     * @param tracks      tracks
     * @param addr        addr
     * @return StreamInfo
     */
    @Override
    public StreamInfo generateStreamInfo(MediaServer mediaServer,
                                         String app,
                                         String stream,
                                         Object tracks,
                                         String addr) {
        StreamInfo info = new StreamInfo();
        info.setStreamId(stream);
        info.setApp(app);
        if (addr == null) {
            addr = mediaServer.getStreamIp();
        }
        info.setMediaServerId(mediaServer.getServerId());
        info.setRtmp(String.format("rtmp://%s:%s/%s/%s", addr, mediaServer.getRtmpPort(), app,  stream));
        if (mediaServer.getRtmpSslPort() != 0) {
            info.setRtmps(String.format("rtmps://%s:%s/%s/%s", addr, mediaServer.getRtmpSslPort(), app,  stream));
        }
        info.setRtsp(String.format("rtsp://%s:%s/%s/%s", addr, mediaServer.getRtspPort(), app,  stream));
        if (mediaServer.getRtspSslPort() != 0) {
            info.setRtsps(String.format("rtsps://%s:%s/%s/%s", addr, mediaServer.getRtspSslPort(), app,  stream));
        }
        info.setFlv(String.format("http://%s:%s/%s/%s.flv", addr, mediaServer.getHttpPort(), app,  stream));
        info.setWsFlv(String.format("ws://%s:%s/%s/%s.flv", addr, mediaServer.getHttpPort(), app,  stream));
        info.setHls(String.format("http://%s:%s/%s/%s/hls.m3u8", addr, mediaServer.getHttpPort(), app,  stream));
        info.setWsHls(String.format("ws://%s:%s/%s/%s/hls.m3u8", addr, mediaServer.getHttpPort(), app,  stream));
        info.setFmp4(String.format("http://%s:%s/%s/%s.live.mp4", addr, mediaServer.getHttpPort(), app,  stream));
        info.setWsFmp4(String.format("ws://%s:%s/%s/%s.live.mp4", addr, mediaServer.getHttpPort(), app,  stream));
        info.setTs(String.format("http://%s:%s/%s/%s.live.ts", addr, mediaServer.getHttpPort(), app,  stream));
        info.setWsTs(String.format("ws://%s:%s/%s/%s.live.ts", addr, mediaServer.getHttpPort(), app,  stream));
        if (mediaServer.getHttpSslPort() != 0) {
            info.setHttpsFlv(String.format("https://%s:%s/%s/%s.flv", addr, mediaServer.getHttpSslPort(), app,  stream));
            info.setWssFlv(String.format("wss://%s:%s/%s/%s.flv", addr, mediaServer.getHttpSslPort(), app,  stream));
            info.setHttpsHls(String.format("https://%s:%s/%s/%s/hls.m3u8", addr, mediaServer.getHttpSslPort(), app,  stream));
            info.setWssHls(String.format("wss://%s:%s/%s/%s/hls.m3u8", addr, mediaServer.getHttpSslPort(), app,  stream));
            info.setHttpsFmp4(String.format("https://%s:%s/%s/%s.live.mp4", addr, mediaServer.getHttpSslPort(), app,  stream));
            info.setWssFmp4(String.format("wss://%s:%s/%s/%s.live.mp4", addr, mediaServer.getHttpSslPort(), app,  stream));
            info.setHttpsTs(String.format("https://%s:%s/%s/%s.live.ts", addr, mediaServer.getHttpSslPort(), app,  stream));
            info.setWssTs(String.format("wss://%s:%s/%s/%s.live.ts", addr, mediaServer.getHttpSslPort(), app,  stream));
            info.setWssTs(String.format("wss://%s:%s/%s/%s.live.ts", addr, mediaServer.getHttpSslPort(), app,  stream));
            info.setRtc(String.format("https://%s:%s/index/api/webrtc?app=%s&stream=%s&type=play", mediaServer.getStreamIp(), mediaServer.getHttpSslPort(), app,  stream));
        }
        info.setTracks(tracks);
        return info;
    }
}
