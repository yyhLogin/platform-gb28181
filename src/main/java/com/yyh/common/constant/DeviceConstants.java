package com.yyh.common.constant;

/**
 * @author: yyh
 * @date: 2021-12-24 10:59
 * @description: DeviceConstants
 **/
public interface DeviceConstants {
    /**
     * 标识前缀
     */
    String SIGNID = "001310";

    /**
     * 设备标识符当前的key
     */
    String STRING_KEY = "device:signid";

    /**
     * 对应设备和绑定的服务器的关系的key
     */
    String DEVICE = "device:";

    /**
     * 当前在线的设备的key
     */
    String DEVICE_ONLINE = "gb28181devol:";

    /**
     * 媒体服务
     */
    String MEDIA = "serverlist:media:";

    /**
     * 信令服务
     */
    String STRING_SERVER = "serverlist:signl:";
    /**
     * mq网关服务器
     */
    String STRING_SWGSERVER = "serverlist:swgsignl:";
}
