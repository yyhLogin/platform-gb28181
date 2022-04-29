package com.yyh.gb28181.command;

import com.yyh.gb28181.callback.SipCallback;
import com.yyh.media.config.SsrcConfig;
import com.yyh.media.config.ZlmServerConfig;
import com.yyh.media.entity.SsrcInfo;
import com.yyh.media.subscribe.HookEvent;
import com.yyh.web.entity.GbDevice;
import com.yyh.web.entity.MediaServer;

import java.time.LocalDateTime;

/**
 * @author: yyh
 * @date: 2021-12-07 09:45
 * @description: ISIPCommander
 **/
public interface ISipCommander {

    /**
     * 查询设备信息指令
     * @param device 设备
     * @return Boolean
     */
    Boolean deviceInfoQuery(GbDevice device);

    /**
     * 查询设备通道指令
     * @param device device
     * @param errorCallback 错误回调
     * @return Boolean
     */
    Boolean deviceCatalogQuery(GbDevice device, SipCallback errorCallback);


    /**
     *请求预览视频流
     * @param device 设备信息
     * @param ssrcInfo ssrc
     * @param channelId 通道信息
     * @param server 媒体服务信息
     * @param event hook 回调
     * @param errorCallback 失败回调
     */
    void playRealtimeStream(GbDevice device, SsrcInfo ssrcInfo, String channelId, MediaServer server, HookEvent event, SipCallback errorCallback);

    /**
     * 请求视频回放
     * @param device 设备信息
     * @param ssrcInfo ssrc
     * @param channelId 通道信息
     * @param server 媒体服务信息
     * @param event hook 回调
     * @param errorCallback 失败回调
     * @param bTime 开始时间
     * @param eTime 结束时间
     */
    void playbackStream(GbDevice device, SsrcInfo ssrcInfo, String channelId, MediaServer server, Long bTime, Long eTime, HookEvent event, SipCallback errorCallback);


    /**
     * 历史文件下载
     * @param device 设备信息
     * @param ssrcInfo ssrc
     * @param channelId 通道id
     * @param server 媒体服务信息
     * @param bTime 开始时间
     * @param eTime 结束时间
     * @param downloadSpeed 下载速率
     * @param event hook 回调
     * @param errorCallback sip错误回调
     */
    void downloadStream(GbDevice device, SsrcInfo ssrcInfo, String channelId, MediaServer server, Long bTime, Long eTime, String downloadSpeed, HookEvent event, SipCallback errorCallback);

    /**
     * 停止预览
     * @param deviceId deviceId
     * @param channelId channelId
     */
    void streamByeCmd(String deviceId, String channelId);

    /**
     * 停止预览
     * @param deviceId deviceId
     * @param channelId deviceId
     * @param okCallback okCallback
     */
    void streamByeCmd(String deviceId, String channelId,SipCallback okCallback);

    /**
     * 查询设备录像文件
     * @param device 设备信息
     * @param deviceId 设备id
     * @param channelId 通道id
     * @param bTime 开始时间
     * @param eTime 结束时间
     * @param sn 流水号
     * @param errorCallback 错误回调
     */
    void recordInfoQuery(GbDevice device,String deviceId, String channelId, LocalDateTime bTime, LocalDateTime eTime, int sn, SipCallback errorCallback);

    /**
     * 移动位置订阅
     * @param byId 设备信息
     * @param expires 订阅超时时间
     * @param interval 上报时间间隔
     * @return Boolean
     */
    Boolean mobilePositionSubscribe(GbDevice byId, Integer expires, Integer interval);

    /**
     * 查询报警信息
     * @param device		视频设备
     * @param startPriority	报警起始级别（可选）
     * @param endPriority	报警终止级别（可选）
     * @param alarmMethod	报警方式条件（可选）
     * @param alarmType		报警类型
     * @param startTime		报警发生起始时间（可选）
     * @param endTime		报警发生终止时间（可选）
     * @param sn		sn号
     * @param errorCallback	错误回调
     * @return				true = 命令发送成功
     */
    Boolean alarmInfoQuery(GbDevice device, String startPriority, String endPriority, String alarmMethod, String alarmType, LocalDateTime startTime, LocalDateTime endTime,Integer sn, SipCallback errorCallback);

    /**
     * 查询设备状态
     * @param device 设备信息
     * @param sn sn号
     * @param errorCallback 错误回调
     * @return Boolean
     */
    Boolean deviceStatusQuery(GbDevice device, String sn, SipCallback errorCallback);

    /**
     * 查询设备配置
     * @param device 设备id
     * @param channelId 通道id
     * @param configType 配置类型
     * @param errorCallback 错误回调
     * @return Boolean
     */
    Boolean deviceConfigQuery(GbDevice device, String channelId,String configType, SipCallback errorCallback);

    /**
     * 设备基础信息配置
     * @param device 国标设备
     * @param channelId 通道id
     * @param sn 流水号
     * @param name 设备/通道名称(可选)
     * @param expiration 注册过期时间(可选)
     * @param heartBeatInterval 心跳间隔时间(可选)
     * @param heartBeatCount 心跳超时次数(可选)
     * @param errorCallback 错误回调
     * @return Boolean
     */
    Boolean deviceBasicConfigCmd(GbDevice device, String channelId,int sn, String name, String expiration,
                                 String heartBeatInterval, String heartBeatCount, SipCallback errorCallback);

    /**
     * 远程启动控制命令
     * @param device 国标设备
     * @return boolean
     */
    boolean teleBootCmd(GbDevice device);

    /**
     * 音视频录像控制
     * @param device 国标设备信息
     * @param channelId 设备通道信息
     * @param recordCmdStr 录像远程控制
     * @param sn 流水号
     * @param errorCallback 错误回调
     * @return boolean
     */
    boolean recordCmd(GbDevice device,
                      String channelId,
                      String recordCmdStr,
                      int sn,
                      SipCallback errorCallback);


    /**
     * 布防/撤防命令
     * @param device 国标设备信息
     * @param guardCmdStr 布防撤防控制命令
     * @param sn 流水号
     * @param errorCallback 错误回调
     * @return boolean
     */
    boolean guardCmd(GbDevice device, String guardCmdStr,int sn, SipCallback errorCallback);

    /**
     * 关键帧发送命令
     * @param device 国标设备信息
     * @param channelId 通道id
     * @param errorCallback 错误回调
     * @return boolean
     */
    boolean iFrameCmd(GbDevice device, String channelId,SipCallback errorCallback);

    /**
     * 看守位控制命令
     * @param device 国标设备信息
     * @param channelId 通道id
     * @param enabled 看守位使能：1 = 开启，0 = 关闭
     * @param resetTime 自动归位时间间隔，开启看守位时使用，单位:秒(s)
     * @param presetIndex 调用预置位编号，开启看守位时使用，取值范围0~255
     * @param errorCallback 错误回调
     * @return boolean
     */
    boolean homePositionCmd(GbDevice device, String channelId, String enabled, String resetTime, String presetIndex, SipCallback errorCallback);

    /**
     * 拉框控制命令
     *
     * @param device    控制设备
     * @param channelId 通道id
     * @param cmdString 前端控制指令串
     * @return boolean
     */
    boolean dragZoomCmd(GbDevice device, String channelId, String cmdString);

    /**
     * 前端控制，包括PTZ指令、FI指令、预置位指令、巡航指令、扫描指令和辅助开关指令
     * @param device 设备信息
     * @param channelId 通道id
     * @param cmdCode 指令码
     * @param horizonSpeed 数据1
     * @param verticalSpeed 数据2
     * @param zoomSpeed 组合码2
     * @return boolean
     */
    boolean frontEndCmd(GbDevice device, String channelId, int cmdCode, int horizonSpeed, int verticalSpeed, int zoomSpeed);

    /**
     * 查询设备预置位
     * @param device 设备信息
     * @param channelId 通道id
     * @param snowflake 流水号
     * @param errorCallback 错误回调
     * @return boolean
     */
    boolean presetQuery(GbDevice device, String channelId, long snowflake, SipCallback errorCallback);
}
