package com.yyh.media.subscribe;

/**
 * @author: yyh
 * @date: 2021-12-22 10:07
 * @description: HookType
 **/
public enum HookType{
    /**
     * 流量统计事件，播放器或推流器断开时并且耗用流量超过特定阈值时会触发此事件，
     * 阈值通过配置文件general.flowThreshold配置；此事件对回复不敏感。
     */
    on_flow_report,
    /**
     * 访问http文件服务器上hls之外的文件时触发
     */
    on_http_access,
    /**
     * 播放器鉴权事件，rtsp/rtmp/http-flv/ws-flv/hls的播放都将触发此鉴权事件；
     * 如果流不存在，那么先触发on_play事件然后触发on_stream_not_found事件。
     * 播放rtsp流时，如果该流启动了rtsp专属鉴权(on_rtsp_realm)那么将不再触发on_play事件。
     */
    on_play,
    /**
     * rtsp/rtmp/rtp推流鉴权事件。
     */
    on_publish,
    /**
     * 录制mp4完成后通知事件；此事件对回复不敏感
     */
    on_record_mp4,
    /**
     * 该rtsp流是否开启rtsp专用方式的鉴权事件，开启后才会触发on_rtsp_auth事件。
     * 需要指出的是rtsp也支持url参数鉴权，它支持两种方式鉴权
     */
    on_rtsp_auth,
    /**
     * rtsp专用的鉴权事件，先触发on_rtsp_realm事件然后才会触发on_rtsp_auth事件。
     */
    on_rtsp_realm,
    /**
     * shell登录鉴权，ZLMediaKit提供简单的telnet调试方式
     * 使用telnet 127.0.0.1 9000能进入MediaServer进程的shell界面。
     */
    on_shell_login,
    /**
     * rtsp/rtmp流注册或注销时触发此事件；此事件对回复不敏感
     */
    on_stream_changed,
    /**
     * 流无人观看时事件，用户可以通过此事件选择是否关闭无人看的流。
     */
    on_stream_none_reader,
    /**
     * 流未找到事件，用户可以在此事件触发时，立即去拉流，这样可以实现按需拉流；此事件对回复不敏感。
     */
    on_stream_not_found,
    /**
     * 服务器启动事件，可以用于监听服务器崩溃重启；此事件对回复不敏感。
     */
    on_server_started,
    /**
     * 服务器定时上报时间，上报间隔可配置，默认10s上报一次
     */
    on_server_keepalive
}
