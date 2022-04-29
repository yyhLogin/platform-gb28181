package com.yyh.gb28181.constant;

/**
 * @author: yyh
 * @date: 2021-12-06 15:29
 * @description: VideoManagerConstant
 **/
public interface VideoManagerConstant {

    String EVENT_ONLINE_REGISTER = "1";

    String EVENT_ONLINE_OUTLINE = "2";

    String EVENT_ONLINE_KEEPALIVE = "3";

    String EVENT_ONLINE_MESSAGE = "4";

    String DEVICE_GB28181 = "device_gb28181:";
    /**
     * 设备通道
     */
    String DEVICE_CHANEL_28181 = "gb28181:device:channel:";


    String DEVICE_ONLINE = "online:";

    String DEVICE_ONLINE_STATE = "1";
    String DEVICE_OFFLINE_STATE = "0";

    /**
     * 设备在线监控过期时间
     */
    Integer ONLINE_EXPIRED_SECOND = 90;

    /**
     * 信令交互默认字符格式
     */
    String DEVICE_DEFAULT_CHARSET = "gb2312";

    /**
     * 视音频回放完成标志
     */
    String PLAYBACK_STOP_STATUS = "121";


    //*************************平台相关key***************************

    /**
     * 注册信息
     */
    String PLATFORM_REGISTER_INFO = "gb28181:platform:registerInfo:";
    String PLATFORM_REGISTER = "gb28181:platform:register:";
    /**
     * 心跳信息
     */
    String PLATFORM_KEEPALIVE = "gb28181:platform:keepalive:";
    /**
     * 缓存信息
     */
    String PLATFORM_CACHE_INFO = "gb28181:platform:cacheInfo:";
    String PLATFORM_CACHE = "gb28181:platform:cache:";



}
