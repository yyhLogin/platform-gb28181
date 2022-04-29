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

    String REQUEST = "REQUEST";

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
    String RESPONSE = "RESPONSE";

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
     * 主动上报位置信息
     */
    String MESSAGE_NOTIFY_MOBILE_POSITION = "MESSAGE/NOTIFY/MOBILEPOSITION";

    /**
     * 响应设备信息
     */
    String MESSAGE_RESPONSE_DEVICEINFO = "MESSAGE/RESPONSE/DEVICEINFO";

    /**
     * 录像信息
     */
    String MESSAGE_RESPONSE_RECORDINFO = "MESSAGE/RESPONSE/RECORDINFO";

    /**
     * 报警信息查询接收
     */
    String MESSAGE_RESPONSE_ALARM = "MESSAGE/RESPONSE/ALARM";

    /**
     * 响应设备通道信息
     */
    String MESSAGE_RESPONSE_CATALOG = "MESSAGE/RESPONSE/CATALOG";

    /**
     * 响应设备通道信息
     */
    String MESSAGE_RESPONSE_DEVICESTATUS = "MESSAGE/RESPONSE/DEVICESTATUS";

    /**
     * 设备配置查询响应
     */
    String MESSAGE_RESPONSE_CONFIGDOWNLOAD = "MESSAGE/RESPONSE/CONFIGDOWNLOAD";
    /**
     * 设备配置设定响应
     */
    String MESSAGE_RESPONSE_DEVICECONFIG = "MESSAGE/RESPONSE/DEVICECONFIG";
    /**
     * 设备控制响应
     */
    String MESSAGE_RESPONSE_DEVICECONTROL = "MESSAGE/RESPONSE/DEVICECONTROL";
    /**
     * 设备预置位查询响应
     */
    String MESSAGE_RESPONSE_PRESETQUERY = "MESSAGE/RESPONSE/PRESETQUERY";


    /**
     * 视频回放完成
     */
    String MESSAGE_NOTIFY_MEDIASTATUS = "MESSAGE/NOTIFY/MEDIASTATUS";

    /**
     * 上级平台查询设备信息
     */
    String MESSAGE_QUERY_DEVICEINFO = "MESSAGE/QUERY/DEVICEINFO";
    /**
     * 上级平台查询catalog
     */
    String MESSAGE_QUERY_CATALOG = "MESSAGE/QUERY/CATALOG";

    /**
     * 订阅的位置信息上报
     */
    String NOTIFY_MOBILEPOSITION = "NOTIFY/NOTIFY/MOBILEPOSITION";

    /**
     * 报警通知
     */
    String NOTIFY_NOTIFY_ALARM = "NOTIFY/NOTIFY/ALARM";

    /**
     * 平台点播
     */
    String INVITE = "INVITE";
}
