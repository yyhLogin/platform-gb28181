package com.yyh.media.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yyh.common.constant.CommonResultConstants;
import com.yyh.common.utils.CommonResult;
import com.yyh.config.ConverterType;
import com.yyh.gb28181.command.ISipCommander;
import com.yyh.gb28181.constant.VideoManagerConstant;
import com.yyh.media.config.SsrcConfig;
import com.yyh.media.constants.MediaConstant;
import com.yyh.media.constants.ServerApiConstant;
import com.yyh.media.entity.SsrcInfo;
import com.yyh.media.service.IMediaServerRestful;
import com.yyh.media.service.IPlayService;
import com.yyh.web.entity.GbDevice;
import com.yyh.web.entity.MediaServer;
import com.yyh.web.service.IGbDeviceService;
import com.yyh.web.service.IMediaServerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * @author: yyh
 * @date: 2021-12-23 11:01
 * @description: PlayServiceImpl
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class PlayServiceImpl implements IPlayService {

    private final RedisTemplate<String,String> redisTemplate;

    private final IMediaServerService mediaServerService;

    private final IMediaServerRestful restful;

    private final ObjectMapper mapper;

    private final ISipCommander sipCommander;

    private final IGbDeviceService deviceService;

    /**
     * 实时预览视频
     *
     * @param deviceId  设备id
     * @param channelId 通道id
     * @return DeferredResult<CommonResult < String>>
     */
    @Override
    public DeferredResult<CommonResult<String>> play(String deviceId, String channelId) {
        DeferredResult<CommonResult<String>> result = new DeferredResult<>(10000L);
        // TODO 判断当前设备通道是否在传流
        GbDevice byId = deviceService.getById(deviceId);
        if (byId==null){
            log.warn("[ device 28181 ] 设备未注册:{}",deviceId);
            result.setResult(CommonResult.<String>builder()
                    .code(CommonResultConstants.FAIL)
                    .msg("设备未注册"+deviceId).build());
            return result;
        }
        //检测是否在线
        Boolean hasKey = redisTemplate.hasKey(VideoManagerConstant.DEVICE_ONLINE + deviceId);
        if (hasKey==null||!hasKey){
            log.warn("[ device 28181 ] 设备离线:{}",deviceId);
            result.setResult(CommonResult.<String>builder()
                    .code(CommonResultConstants.FAIL)
                    .msg("设备不在线"+deviceId).build());
            return result;
        }
        //检查通道是否存在
        Object o = redisTemplate.opsForHash().get(VideoManagerConstant.DEVICE_CHANEL_28181 + deviceId, channelId);
        if (o==null){
            log.warn("[ device 28181 ] 通道不存在:{}",channelId);
            result.setResult(CommonResult.<String>builder()
                    .code(CommonResultConstants.FAIL)
                    .msg("通道不存在"+channelId).build());
            return result;
        }
        //查询媒体服务器
        MediaServer server = mediaServerService.queryMediaServerByDeviceId(deviceId);
        if (server==null){
            log.warn("[ device 28181 ] 获取媒体服务失败:{}",deviceId);
            result.setResult(CommonResult.<String>builder()
                    .code(CommonResultConstants.FAIL)
                    .msg("媒体服务不在线或查询媒体服务错误").build());
            return result;
        }
        SsrcInfo ssrc = creatSsrcConfig(server,deviceId,channelId,false);
        //
        //发送sip指令
        sipCommander.playRealtimeStream(byId,ssrc,channelId,server,null,null);
        log.info("");
        return result;
    }

    private synchronized SsrcInfo creatSsrcConfig(MediaServer mediaServer,String deviceId,String channelId, boolean isPlayback){
        String key = MediaConstant.SSRC_SERVER+mediaServer.getServerId();
        String ssrc = StrUtil.EMPTY;
        Boolean hasKey = redisTemplate.hasKey(key);
        String prefix = null;
        if (hasKey!=null&&hasKey){
            Object unused = redisTemplate.opsForHash().get(key, "unused");
            Object used = redisTemplate.opsForHash().get(key, "used");
            prefix = Objects.requireNonNull(redisTemplate.opsForHash().get(key, "prefix")).toString();
            try {
                assert unused != null;
                assert used != null;
                ArrayList<String> un = mapper.readValue(unused.toString(), ConverterType.ArrayList_STRING_TYPE);
                ArrayList<String> u = mapper.readValue(used.toString(), ConverterType.ArrayList_STRING_TYPE);
                int i = RandomUtil.randomInt(un.size());
                ssrc = un.get(i);
                un.remove(i);
                u.add(ssrc);
                redisTemplate.opsForHash().put(key,"unused",mapper.writeValueAsString(un));
                redisTemplate.opsForHash().put(key,"used",mapper.writeValueAsString(u));
                log.info("获取到ssrc:{},剩余容量:{},可用容量:{}",ssrc,un.size(),u.size());
            } catch (JsonProcessingException e) {
                log.error("获取可用ssrc出现异常:{}",e.getMessage(),e);
            }
        }else {
            ssrc = "0000";
        }
        if (isPlayback){
            ssrc = "1"+prefix+ssrc;
        }else {
            ssrc = "0"+prefix+ssrc;
        }
        String streamId;
        if (MediaConstant.IS_TRUE.equals(mediaServer.getRtpEnable())){
            streamId = String.format("%s_%s", deviceId, channelId);
        }else {
            streamId = String.format("%08x", Integer.parseInt(ssrc)).toUpperCase();
        }
        int rtpServerPort = mediaServer.getRtpProxyPort();
        if (MediaConstant.IS_TRUE.equals(mediaServer.getRtpEnable())){
            String rtpPortRange = mediaServer.getRtpPortRange();
            String[] split = rtpPortRange.split("-");
            if (split.length==2){
                rtpServerPort = RandomUtil.randomInt(Integer.parseInt(split[0]),Integer.parseInt(split[1]));
            }else {
                rtpServerPort = RandomUtil.randomInt(30000,30500);
            }
            String url = String.format(ServerApiConstant.OPEN_RTP_SERVER,mediaServer.getIp(),mediaServer.getHttpPort());
            Map<String, Object> map = restful.openRtpServer(url, mediaServer.getSecret(), rtpServerPort, "1", streamId);
            Integer code = MapUtil.getInt(map, "code");
            if (code==MediaConstant.RESTFUL_SUCCESS){
                log.info("获取28181推流端口:{}",MapUtil.getInt(map,"port"));
            }
        }
        return new SsrcInfo(rtpServerPort, ssrc, streamId);
    }


}
