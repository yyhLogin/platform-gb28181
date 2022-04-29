package com.yyh.gb28181.service;

import com.yyh.common.utils.CommonResult;
import com.yyh.gb28181.callback.SipSubscribe;
import com.yyh.media.entity.StreamInfo;
import com.yyh.media.subscribe.PlayHookSubscribe;
import com.yyh.web.entity.GbDevice;
import com.yyh.web.entity.MediaServer;
import org.springframework.web.context.request.async.DeferredResult;

import java.time.LocalDateTime;

/**
 * @author: yyh
 * @date: 2021-12-23 11:00
 * @description: IPlayService
 **/
public interface IPlayService {

    /**
     * 实时预览视频
     * @param deviceId 设备id
     * @param channelId 通道id
     * @param playCallback 点播收流回调
     * @param errorCallback sip错误回调
     * @return DeferredResult<CommonResult<StreamInfo>>
     */
    DeferredResult<CommonResult<StreamInfo>> play(String deviceId,
                                                  String channelId,
                                                  PlayHookSubscribe playCallback,
                                                  SipSubscribe errorCallback);


    /**
     * 录像回放视频
     * @param channelId 设备id
     * @param deviceId 通道id
     * @param bTime 开始时间
     * @param eTime 结束时间
     * @param playCallback 播放回调
     * @param errorCallback 错误回调
     * @return DeferredResult<CommonResult<StreamInfo>>
     */
    DeferredResult<CommonResult<StreamInfo>> playback(String deviceId,
                                                      String channelId,
                                                      LocalDateTime bTime,
                                                      LocalDateTime eTime,
                                                      PlayHookSubscribe playCallback,
                                                      SipSubscribe errorCallback);

    /**
     * 查询设备
     * @param deviceId deviceId
     * @param channelId channelId
     * @return GbDevice
     */
    GbDevice queryGbDevice(String deviceId, String channelId);

    /**
     * 查询该设备分配的媒体服务器
     * @param deviceId deviceId
     * @param channelId channelId
     * @return MediaServer
     */
    MediaServer queryMediaServer(String deviceId, String channelId);

    /**
     * 停止实时预览
     * @param deviceId 设备id
     * @param channelId 通道id
     * @return DeferredResult<CommonResult<String>>
     */
    DeferredResult<CommonResult<String>> stop(String deviceId, String channelId);


    /**
     * 停止视频回放
     * @param deviceId 设备id
     * @param channelId 通道id
     * @return DeferredResult<CommonResult<String>>
     */
    DeferredResult<CommonResult<String>> stopPlayback(String deviceId, String channelId);

    /**
     * 开始历史媒体下载
     * @param deviceId 设备编号
     * @param channelId 通道编号
     * @param bTime 开始时间
     * @param eTime 结束时间
     * @param downloadSpeed 下载速率
     * @param playCallback 播放回调
     * @param errorCallback 错误回调
     * @return DeferredResult<CommonResult<StreamInfo>>
     */
    DeferredResult<CommonResult<StreamInfo>> download(String deviceId, String channelId, LocalDateTime bTime, LocalDateTime eTime, String downloadSpeed, PlayHookSubscribe playCallback, SipSubscribe errorCallback);
}
