package com.yyh.media.constants;

/**
 * @author: yyh
 * @date: 2021-12-16 17:24
 * @description: ServerApiConstant
 **/
public interface ServerApiConstant {

    /**
     * 获取媒体服务器信息
     */
    String GET_SERVER_CONFIG = "http://%s:%d/index/api/getServerConfig?secret=%s";
    /**
     * 设置媒体服务器信息
     */
    String SET_SERVER_CONFIG = "http://%s:%d/index/api/setServerConfig";

    /**
     * 创建GB28181 RTP接收端口，如果该端口接收数据超时，则会自动被回收(不用调用closeRtpServer接口)
     */
    String OPEN_RTP_SERVER = "http://%s:%d/index/api/openRtpServer";

    /**
     * 关闭GB28181 RTP接收端口
     */
    String CLOSE_RTP_SERVER = "http://%s:%d/index/api/closeRtpServer";

    /**
     * 获取流列表，可选筛选参数
     */
    String GET_MEDIA_LIST = "http://%s:%d/index/api/getMediaList?secret=%s";


    //*******************hook url*********************

    /**
     * hook 停止
     */
    String HOOK_STOP = "0";
    /**
     * hook 启用
     */
    String HOOK_OPEN = "1";

    /**
     * 秘钥
     */
    String SECRET = "secret";

    /**
     * hook启用
     */
    String HOOK_ENABLE = "hook.enable";
    /**
     * http 字符集
     */
    String HTTP_CHARSET = "http.charSet";

    /**
     * 服务端心跳保持
     */
    String ON_SERVER_KEEPALIVE = "http://%s:%d/index/hook/on_server_keepalive";

    /**
     * 服务端心跳保持 key
     */
    String ON_SERVER_KEEPALIVE_KEY = "hook.on_server_keepalive";

    /**
     * 服务端启动
     */
    String ON_SERVER_STARTED = "http://%s:%d/index/hook/on_server_started";

    /**
     * 服务端启动 key
     */
    String ON_SERVER_STARTED_KEY = "hook.on_server_started";

    /**
     * 流变化
     */
    String ON_STREAM_CHANGED = "http://%s:%d/index/hook/on_stream_changed";

    /**
     * 流变化 key
     */
    String ON_STREAM_CHANGED_KEY = "hook.on_stream_changed";

    /**
     * 流无人观看
     */
    String ON_STREAM_NONE_READER = "http://%s:%d/index/hook/on_stream_none_reader";

    /**
     * 流无人观看 key
     */
    String ON_STREAM_NONE_READER_KEY = "hook.on_stream_none_reader";

    /**
     * 流变化
     */
    String ON_SHELL_LOGIN = "http://%s:%d/index/hook/on_shell_login";

    /**
     * 流变化 key
     */
    String ON_SHELL_LOGIN_KEY = "hook.on_shell_login";

    //************************************************
}
