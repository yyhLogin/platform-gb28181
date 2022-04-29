package com.yyh.media.constants;

/**
 * @author: yyh
 * @date: 2021-12-20 16:39
 * @description: MediaConstant
 **/
public interface MediaConstant {
    /**
     * 最大并发流
     */
    Integer MAX_STREAM_COUNT = 1000;
    /**
     * true
     */
    String IS_TRUE = "1";
    /**
     * false
     */
    String IS_FALSE = "0";
    /**
     * 媒体服务key
     */
    String MEDIA_SERVER = "gb28181:server:media:";
    /**
     * 媒体流 key
     */
    String STREAM_SERVER = "gb28181:server:stream:";
    /**
     * 播放信息
     */
    String PLAY_SERVER = "gb28181:server:play:";
    /**
     * 回放信息
     */
    String PLAYBACK_SERVER = "gb28181:server:playback:";
    /**
     * 在线的媒体服务
     */
    String MEDIA_ONLINE_SERVER = "gb28181:server:online:";
    /**
     * ssrc
     */
    String SSRC_SERVER = "gb28181:server:ssrc:";
    /**
     * seq
     */
    String SEQ_SERVER = "gb28181:server:seq:";
    /**
     * restful 正常返回 0
     */
    int RESTFUL_SUCCESS = 0;
    /**
     * restful 返回code
     */
    String RESTFUL_CODE = "code";
    /**
     * restful 返回data
     */
    String RESTFUL_DATA = "data";
    /**
     * restful 返回msg
     */
    String RESTFUL_MSG = "msg";
    /**
     * restful 返回msg->success
     */
    String RESTFUL_SUCCESS_MSG = "success";

    /**
     * app名称
     */
    String APP_RTP = "rtp";
    /**
     * 流格式
     */
    String SCHEMA_RTMP = "rtmp";
}
