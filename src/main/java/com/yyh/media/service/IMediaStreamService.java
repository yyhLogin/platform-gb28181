package com.yyh.media.service;

import com.yyh.media.entity.StreamInfo;
import com.yyh.web.entity.MediaServer;

import java.util.Map;

/**
 * @author: yyh
 * @date: 2022-01-06 14:22
 * @description: IMediaStreamService
 **/
public interface IMediaStreamService {

    /**
     * 提交媒体流信息
     * @param key key
     * @param hashKey hashKey
     * @param value value
     */
    void putStream(String key, String hashKey, Map<String,Object> value);

    /**
     * 注销流
     * @param key key
     * @param hashKey hashKey
     */
    void removeStream(String key, String hashKey);

    /**
     * 生成视频流播放信息
     * @param mediaServer mediaServer
     * @param app app
     * @param stream stream
     * @param tracks tracks
     * @return StreamInfo
     */
    StreamInfo generateStreamInfo(MediaServer mediaServer, String app, String stream, Object tracks);

    /**
     * 生成视频流播放信息
     * @param mediaServer mediaServer
     * @param app app
     * @param stream stream
     * @param tracks tracks
     * @param addr addr
     * @return StreamInfo
     */
    StreamInfo generateStreamInfo(MediaServer mediaServer, String app, String stream, Object tracks,String addr);


}
