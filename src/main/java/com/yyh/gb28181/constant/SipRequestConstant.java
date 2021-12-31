package com.yyh.gb28181.constant;

/**
 * @author: yyh
 * @date: 2021-11-29 16:24
 * @description: SipRequestConstant
 **/
public interface SipRequestConstant {

    /**
     * UDP传输协议
     */
    String UDP = "UDP";
    /**
     * TCP传输协议
     */
    String TCP = "TCP";

    /**
     * tcp被动模式
     */
    String TCP_PASSIVE = "TCP-PASSIVE";

    /**
     * tcp主动模式
     */
    String TCP_ACTIVE = "TCP-ACTIVE";

    /**
     * 查询
     */
    String QUERY = "Query";

    /**
     * NOTIFY
     */
    String NOTIFY = "NOTIFY";

    /**
     * 响应
     */
    String RESPONSE = "Response";

    /**
     * 注册
     */
    String REGISTER = "REGISTER";

    /**
     * 总消息
     */
    String MESSAGE = "MESSAGE";

    /**
     * 心跳维持消息
     */
    String MESSAGE_NOTIFY_KEEPALIVE = "MESSAGE/NOTIFY/KEEPALIVE";

    /**
     * 响应设备信息
     */
    String MESSAGE_RESPONSE_DEVICEINFO = "MESSAGE/RESPONSE/DEVICEINFO";

    /**
     * 响应设备通道信息
     */
    String MESSAGE_RESPONSE_CATALOG = "MESSAGE/RESPONSE/CATALOG";
}
