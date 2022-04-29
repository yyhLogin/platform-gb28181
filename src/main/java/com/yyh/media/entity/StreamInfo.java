package com.yyh.media.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: yyh
 * @date: 2022-01-11 15:15
 * @description: StreamInfo 媒体流信息
 **/
@Data
public class StreamInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private String app;
    private String streamId;
    private String deviceId;
    private String channelId;
    private String flv;
    private String httpsFlv;
    private String wsFlv;
    private String wssFlv;
    private String fmp4;
    private String httpsFmp4;
    private String wsFmp4;
    private String wssFmp4;
    private String hls;
    private String httpsHls;
    private String wsHls;
    private String wssHls;
    private String ts;
    private String httpsTs;
    private String wsTs;
    private String wssTs;
    private String rtmp;
    private String rtmps;
    private String rtsp;
    private String rtsps;
    private String rtc;
    private String mediaServerId;
    private Object tracks;
}
