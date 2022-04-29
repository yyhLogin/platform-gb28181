package com.yyh.gb28181.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.RandomUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yyh.common.constant.CommonResultConstants;
import com.yyh.common.exception.PlatformException;
import com.yyh.common.utils.CommonResult;
import com.yyh.gb28181.callback.SipSubscribe;
import com.yyh.gb28181.command.ISipCommander;
import com.yyh.gb28181.config.SipServerProperties;
import com.yyh.gb28181.constant.VideoManagerConstant;
import com.yyh.gb28181.service.IPlayService;
import com.yyh.gb28181.session.VideoStreamSessionManager;
import com.yyh.media.callback.CallbackPlayDeferredHandle;
import com.yyh.media.callback.DeferredResultHandle;
import com.yyh.media.component.SsrcManagement;
import com.yyh.media.constants.MediaConstant;
import com.yyh.media.constants.ServerApiConstant;
import com.yyh.media.entity.SsrcInfo;
import com.yyh.media.entity.StreamInfo;
import com.yyh.media.service.impl.MediaStreamServiceImpl;
import com.yyh.media.subscribe.PlayHookSubscribe;
import com.yyh.media.service.IMediaServerRestful;
import com.yyh.web.entity.GbDevice;
import com.yyh.web.entity.MediaServer;
import com.yyh.web.service.IGbDeviceService;
import com.yyh.web.service.IMediaServerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.async.DeferredResult;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author: yyh
 * @date: 2021-12-23 11:01
 * @description: PlayServiceImpl
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class PlayServiceImpl implements IPlayService {

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final RedisTemplate<String,String> redisTemplate;

    private final IMediaServerService mediaServerService;

    private final IMediaServerRestful restful;

    private final ObjectMapper mapper;

    private final ISipCommander sipCommander;

    private final IGbDeviceService deviceService;

    private final CallbackPlayDeferredHandle deferredHandle;

    private final PlayHookSubscribe playHookSubscribe;

    private final MediaStreamServiceImpl mediaStreamService;

    private final VideoStreamSessionManager videoStreamSessionManager;

    private final DeferredResultHandle deferredResultHandle;

    private final SsrcManagement ssrcManagement;

    private final SipServerProperties sipServerProperties;

    /**
     * 实时预览视频
     *
     * @param deviceId      设备id
     * @param channelId     通道id
     * @param playCallback  点播收流回调
     * @param errorCallback sip错误回调
     * @return DeferredResult<CommonResult<StreamInfo>>
     */
    @Override
    public DeferredResult<CommonResult<StreamInfo>> play(String deviceId,
                                                         String channelId,
                                                         PlayHookSubscribe playCallback,
                                                         SipSubscribe errorCallback) {
        // TODO 判断当前设备通道是否在传流,传流跳过点播,直接返回流
        DeferredResult<CommonResult<StreamInfo>> result = new DeferredResult<>(10000L);
        GbDevice device = queryGbDevice(deviceId, channelId);
        MediaServer mediaServer = queryMediaServer(deviceId, channelId);
        String uuid = UUID.randomUUID().toString();
        String key = DeferredResultHandle.CALLBACK_CMD_PLAY+"_"+deviceId+"_"+channelId;
        String url = String.format(ServerApiConstant.GET_MEDIA_LIST,mediaServer.getIp(),mediaServer.getHttpPort(),mediaServer.getSecret());
        ArrayList<Map<String, Object>> mediaList = restful.getMediaList(url, null, null, MediaConstant.APP_RTP, deviceId + "_" + channelId);
        if (mediaList!=null){
            Boolean hasKey = redisTemplate.hasKey(MediaConstant.PLAY_SERVER + deviceId + "_" + channelId);
            if (hasKey!=null&&hasKey){
                Map<String, Object> map = mediaList.get(0);
                CommonResult<StreamInfo> streamInfo = getStreamInfo(map, mediaServer, deviceId, channelId);
                result.setResult(streamInfo);
                // 释放所有点播请求
                ///deferredHandle.invokeHandle(key,uuid,streamInfo);
                return result;
            }
            sipCommander.streamByeCmd(deviceId,channelId);
        }
        if (deferredHandle.hasKey(key)){
            deferredHandle.put(key,uuid,result);
            log.info("当前已经有其它客户端在点播当前设备,跳过点播流程:{}->{}",deviceId,channelId);
            return result;
        }
        deferredHandle.put(key,uuid,result);
        result.onTimeout(()->{
            log.warn("设备点播超时:{}->{}",deviceId,channelId);
            CommonResult<StreamInfo> timeout = CommonResult.<StreamInfo>builder()
                    .code(CommonResultConstants.FAIL)
                    .msg("设备点播超时")
                    .build();
            // TODO 点播超时关闭媒体服务流
            // TODO 通知设备停止推流
            sipCommander.streamByeCmd(deviceId,channelId);
            deferredHandle.invokeHandle(key,uuid,timeout);
        });
        result.onCompletion(()->{
            // TODO 点播成功调用截图
            log.info("点播完成:{}->{}",deviceId, channelId);
        });
        //构建ssrc
        SsrcInfo ssrc = creatSsrcConfig(mediaServer,deviceId,channelId,false);
        //发送sip指令
        sipCommander.playRealtimeStream(device,ssrc,channelId,mediaServer,(server,map)->{
            log.info("收到推流信息");
            CommonResult<StreamInfo> streamInfo = getStreamInfo(map, server, deviceId, channelId);
            // 写入播放记录
            redisTemplate.opsForValue().set(MediaConstant.PLAY_SERVER+deviceId+"_"+channelId,"0");
            // 释放所有点播请求
            deferredHandle.invokeHandle(key,uuid,streamInfo);
        },(error->{
            log.error("点播失败:{}",error.msg);
        }));
        return result;
    }

    /**
     * 录像回放视频
     *
     * @param channelId     设备id
     * @param deviceId      通道id
     * @param bTime         开始时间
     * @param eTime         结束时间
     * @param playCallback  播放回调
     * @param errorCallback 错误回调
     * @return DeferredResult<CommonResult < StreamInfo>>
     */
    @Override
    public DeferredResult<CommonResult<StreamInfo>> playback(String deviceId,
                                                             String channelId,
                                                             LocalDateTime bTime,
                                                             LocalDateTime eTime,
                                                             PlayHookSubscribe playCallback,
                                                             SipSubscribe errorCallback) {
        DeferredResult<CommonResult<StreamInfo>> result = new DeferredResult<>(10000L);
        GbDevice device = queryGbDevice(deviceId, channelId);
        MediaServer mediaServer = queryMediaServer(deviceId, channelId);
        String uuid = UUID.randomUUID().toString();
        String key = DeferredResultHandle.CALLBACK_CMD_PLAYBACK+"_"+deviceId+"_"+channelId;
        String url = String.format(ServerApiConstant.GET_MEDIA_LIST,mediaServer.getIp(),mediaServer.getHttpPort(),mediaServer.getSecret());
        if (deferredHandle.hasKey(key)){
            deferredHandle.put(key,uuid,result);
            log.info("当前已经有其它客户端在点播当前设备,跳过点播流程:{}->{}",deviceId,channelId);
            return result;
        }
        deferredHandle.put(key,uuid,result);
        //构建ssrc
        result.onTimeout(()->{
            log.warn("设备录像回放超时:{}->{}",deviceId,channelId);
            CommonResult<StreamInfo> timeout = CommonResult.<StreamInfo>builder()
                    .code(CommonResultConstants.FAIL)
                    .msg("设备录像回放超时")
                    .build();
            // TODO 点播超时关闭媒体服务流
            // TODO 通知设备停止推流
            sipCommander.streamByeCmd(deviceId,channelId);
            deferredHandle.invokeHandle(key,uuid,timeout);
        });
        ArrayList<Map<String, Object>> mediaList = restful.getMediaList(url, null, null, MediaConstant.APP_RTP, deviceId + "_" + channelId);
        if ( mediaList != null ){
            // 当前已经有播放流，这里先发送停止流请求
            sipCommander.streamByeCmd(deviceId,channelId,(event)->{
                String keys = MediaConstant.PLAY_SERVER + deviceId + "_" + channelId;
                Boolean delete = redisTemplate.delete(keys);
                String closeUrl = String.format(ServerApiConstant.CLOSE_RTP_SERVER,mediaServer.getIp(),mediaServer.getHttpPort());
                Map<String, Object> res = restful.closeRtpServer(closeUrl, mediaServer.getSecret(), deviceId + "_" + channelId);
                Integer code = MapUtil.getInt(res, "code");
                if (MediaConstant.RESTFUL_SUCCESS == code){
                    log.info("closeRtpServer success | {}",deviceId+"_"+channelId);
                    SsrcInfo ssrcInfo = creatSsrcConfig(mediaServer, deviceId,channelId, true);
                    //发送sip指令
                    sipCommander.playbackStream(device,ssrcInfo,channelId,mediaServer,
                            bTime.toEpochSecond(ZoneOffset.of("+8")),
                            eTime.toEpochSecond(ZoneOffset.of("+8")),
                            (server, map)->{
                                log.info("收到录像回放推流信息");
                                CommonResult<StreamInfo> streamInfo = getStreamInfo(map, server, deviceId, channelId);
                                // 释放所有点播请求
                                deferredHandle.invokeHandle(key,uuid,streamInfo);
                            },(error->{
                                log.error("点播失败:{}",error.msg);
                            }));
                }else {
                    log.error("closeRtpServer error  | {} , code -> {}",deviceId+"_"+channelId,code);
                }
                log.info("移除播放key:{}->{}",keys,delete);
            });
            return result;
        }
        SsrcInfo ssrcInfo = creatSsrcConfig(mediaServer, deviceId,channelId, true);
        //发送sip指令
        sipCommander.playbackStream(device,ssrcInfo,channelId,mediaServer,
                bTime.toEpochSecond(ZoneOffset.of("+8")),
                eTime.toEpochSecond(ZoneOffset.of("+8")),
                (server, map)->{
            log.info("收到录像回放推流信息");
            CommonResult<StreamInfo> streamInfo = getStreamInfo(map, server, deviceId, channelId);
            // 释放所有点播请求
            deferredHandle.invokeHandle(key,uuid,streamInfo);
        },(error->{
            log.error("点播失败:{}",error.msg);
        }));
        return result;
    }

    /**
     * 停止实时预览
     *
     * @param deviceId  设备id
     * @param channelId 通道id
     * @return CommonResult<Boolean>
     */
    @Override
    public DeferredResult<CommonResult<String>> stop(String deviceId, String channelId) {
        DeferredResult<CommonResult<String>> result = new DeferredResult<>(10000L);
        MediaServer mediaServer = queryMediaServer(deviceId, channelId);
        String url = String.format(ServerApiConstant.GET_MEDIA_LIST,mediaServer.getIp(),mediaServer.getHttpPort(),mediaServer.getSecret());
        ArrayList<Map<String, Object>> mediaList = restful.getMediaList(url, null, null, MediaConstant.APP_RTP, deviceId + "_" + channelId);
        if (mediaList!=null){
            Map<String, Object> map = mediaList.get(0);
            Integer totalReaderCount = MapUtil.getInt(map, "totalReaderCount");
            if ( totalReaderCount == 0 ){
                String uuid = UUID.randomUUID().toString();
                GbDevice device = queryGbDevice(deviceId, channelId);
                // 录像查询以channelId作为deviceId查询
                String key = DeferredResultHandle.CALLBACK_CMD_STOP + deviceId + channelId;
                deferredResultHandle.put(key, uuid, result);
                sipCommander.streamByeCmd(deviceId,channelId,(event)->{
                    String keys = MediaConstant.PLAY_SERVER + deviceId + "_" + channelId;
                    Boolean delete = redisTemplate.delete(keys);
                    String closeUrl = String.format(ServerApiConstant.CLOSE_RTP_SERVER,mediaServer.getIp(),mediaServer.getHttpPort());
                    Map<String, Object> res = restful.closeRtpServer(closeUrl, mediaServer.getSecret(), deviceId + "_" + channelId);
                    Integer code = MapUtil.getInt(res, "code");
                    if (MediaConstant.RESTFUL_SUCCESS == code){
                        log.info("closeRtpServer success | {}",deviceId+"_"+channelId);
                    }else {
                        log.error("closeRtpServer error  | {} , code -> {}",deviceId+"_"+channelId,code);
                    }
                    log.info("移除播放key:{}->{}",keys,delete);
                    CommonResult<String> commonResult = CommonResult.success("SUCCESS");
                    deferredResultHandle.invokeAllResult(key,commonResult);
                });
                return result;
            }
        }else {
            CommonResult<String> commonResult = CommonResult.success("stream reader count > 0");
            result.setResult(commonResult);
        }
        return result;
    }

    /**
     * 停止视频回放
     *
     * @param deviceId  设备id
     * @param channelId 通道id
     * @return DeferredResult<CommonResult < String>>
     */
    @Override
    public DeferredResult<CommonResult<String>> stopPlayback(String deviceId, String channelId) {
        return stop(deviceId,channelId);
    }

    /**
     * 开始历史媒体下载
     *
     * @param deviceId      设备编号
     * @param channelId     通道编号
     * @param bTime         开始时间
     * @param eTime         结束时间
     * @param downloadSpeed 下载速率
     * @param playCallback  播放回调
     * @param errorCallback 错误回调
     * @return DeferredResult<CommonResult < StreamInfo>>
     */
    @Override
    public DeferredResult<CommonResult<StreamInfo>> download(String deviceId, String channelId, LocalDateTime bTime, LocalDateTime eTime, String downloadSpeed, PlayHookSubscribe playCallback, SipSubscribe errorCallback) {
        DeferredResult<CommonResult<StreamInfo>> result = new DeferredResult<>(10000L);
        GbDevice device = queryGbDevice(deviceId, channelId);
        MediaServer mediaServer = queryMediaServer(deviceId, channelId);
        String uuid = UUID.randomUUID().toString();
        String key = DeferredResultHandle.CALLBACK_CMD_DOWNLOAD+"_"+deviceId+"_"+channelId;
        String url = String.format(ServerApiConstant.GET_MEDIA_LIST,mediaServer.getIp(),mediaServer.getHttpPort(),mediaServer.getSecret());
        if (deferredHandle.hasKey(key)){
            deferredHandle.put(key,uuid,result);
            log.info("当前已经有其它客户端在点播当前设备,跳过点播流程:{}->{}",deviceId,channelId);
            return result;
        }
        deferredHandle.put(key,uuid,result);
        result.onTimeout(()->{
            log.warn("历史媒体下载超时:{}->{}",deviceId,channelId);
            CommonResult<StreamInfo> timeout = CommonResult.<StreamInfo>builder()
                    .code(CommonResultConstants.FAIL)
                    .msg("历史媒体下载超时")
                    .build();
            // TODO 点播超时关闭媒体服务流
            // TODO 通知设备停止推流
            sipCommander.streamByeCmd(deviceId,channelId);
            deferredHandle.invokeHandle(key,uuid,timeout);
        });
        ArrayList<Map<String, Object>> mediaList = restful.getMediaList(url, null, null, MediaConstant.APP_RTP, deviceId + "_" + channelId);
        if (mediaList!=null){
            // 已经有了流，应该先停止
            sipCommander.streamByeCmd(deviceId, channelId, (eventResult) -> {
                String keys = MediaConstant.PLAY_SERVER + deviceId + "_" + channelId;
                Boolean delete = redisTemplate.delete(keys);
                String closeUrl = String.format(ServerApiConstant.CLOSE_RTP_SERVER,mediaServer.getIp(),mediaServer.getHttpPort());
                Map<String, Object> res = restful.closeRtpServer(closeUrl, mediaServer.getSecret(), deviceId + "_" + channelId);
                Integer code = MapUtil.getInt(res, "code");
                if (MediaConstant.RESTFUL_SUCCESS == code){
                    log.info("closeRtpServer success | {}",deviceId+"_"+channelId);
                    SsrcInfo ssrcInfo = creatSsrcConfig(mediaServer, deviceId,channelId, true);
                    //发送sip指令
                    sipCommander.downloadStream(device,ssrcInfo,channelId,mediaServer,
                            bTime.toEpochSecond(ZoneOffset.of("+8")),
                            eTime.toEpochSecond(ZoneOffset.of("+8")),
                            downloadSpeed,
                            (server, map)->{
                                log.info("收到录像回放推流信息");
                                CommonResult<StreamInfo> streamInfo = getStreamInfo(map, server, deviceId, channelId);
                                // 释放所有点播请求
                                deferredHandle.invokeHandle(key,uuid,streamInfo);
                            },(error->{
                                log.error("点播失败:{}",error.msg);
                            }));
                }else {
                    log.error("closeRtpServer error  | {} , code -> {}",deviceId+"_"+channelId,code);
                }
                log.info("移除播放key:{}->{}",keys,delete);
            });
            return result;
        }
        return null;
    }

    /**
     * 查询要点播的设备信息
     * @param deviceId 设备id
     * @param channelId 通道id
     * @return GbDevice
     */
    @Override
    public GbDevice queryGbDevice(String deviceId, String channelId){
        GbDevice byId = deviceService.getById(deviceId);
        if (byId==null){
            log.warn("[ device 28181 ] 设备未注册:{}",deviceId);
            throw new PlatformException(CommonResultConstants.FAIL,"设备不存在");
        }
        //检测是否在线
        Boolean hasKey = redisTemplate.hasKey(VideoManagerConstant.DEVICE_ONLINE + deviceId);
        if (hasKey==null||!hasKey){
            log.warn("[ device 28181 ] 设备离线:{}",deviceId);
            throw new PlatformException(CommonResultConstants.FAIL,"设备不在线"+deviceId);
        }
        //检查通道是否存在
        Object o = redisTemplate.opsForHash().get(VideoManagerConstant.DEVICE_CHANEL_28181 + deviceId, channelId);
        if (o==null){
            log.warn("[ device 28181 ] 通道不存在:{}",channelId);
            throw new PlatformException(CommonResultConstants.FAIL,"通道不存在"+channelId);
        }
        return byId;
    }

    /**
     * 为设备分配媒体服务
     * @param deviceId 设备id
     * @param channelId 通道id
     * @return MediaServer
     */
    @Override
    public MediaServer queryMediaServer(String deviceId, String channelId){
        //查询媒体服务器
        MediaServer server = mediaServerService.queryMediaServerByDeviceId(deviceId);
        if (server==null){
            log.warn("[ device 28181 ] 获取媒体服务失败:{}",deviceId);
            throw new PlatformException(CommonResultConstants.FAIL,"媒体服务不在线或查询媒体服务错误");
        }
        return server;
    }

    /**
     * 构建ssrc信息
     * @param mediaServer 媒体服务器信息
     * @param deviceId 设备id
     * @param channelId 通道id
     * @param isPlayback 是否是回放
     * @return SsrcInfo
     */
    private synchronized SsrcInfo creatSsrcConfig(MediaServer mediaServer,
                                                  String deviceId,
                                                  String channelId,
                                                  boolean isPlayback){
        String key = MediaConstant.SSRC_SERVER+mediaServer.getServerId();
        String ssrc;
        Boolean hasKey = redisTemplate.hasKey(key);
        String prefix = null;
        if (hasKey!=null&&hasKey){
            ssrc = ssrcManagement.pop(mediaServer.getServerId());
            prefix = sipServerProperties.getDomain().substring(3,8);
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
            }else {
                log.info("获取28181推流端口失败:{}",MapUtil.getInt(map,"code"));
            }
        }
        return new SsrcInfo(rtpServerPort, ssrc, streamId);
    }

    /**
     * 构建点播成功实例
     * @param map map
     * @param server server
     * @param deviceId deviceId
     * @param channelId channelId
     * @return CommonResult<StreamInfo>
     */
    private CommonResult<StreamInfo> getStreamInfo(Map<String,Object> map,MediaServer server,String deviceId,String channelId){
        String stream = MapUtil.getStr(map, "stream");
        Object o = MapUtil.get(map, "tracks", Object.class);
        // 构建媒体流信息
        StreamInfo info = mediaStreamService.generateStreamInfo(server, MediaConstant.APP_RTP, stream, o, null);
        info.setDeviceId(deviceId);
        info.setChannelId(channelId);
        return CommonResult.<StreamInfo>builder()
                .code(CommonResultConstants.SUCCESS)
                .msg("点播成功")
                .data(info)
                .build();
    }
}
