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
    String DEVICE_CHANEL_28181 = "device_channel_28181:";


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



}
