package com.yyh.gb28181.command;

import com.yyh.gb28181.callback.SipCallback;
import com.yyh.media.config.SsrcConfig;
import com.yyh.media.config.ZlmServerConfig;
import com.yyh.media.entity.SsrcInfo;
import com.yyh.web.entity.GbDevice;
import com.yyh.web.entity.MediaServer;

/**
 * @author: yyh
 * @date: 2021-12-07 09:45
 * @description: ISIPCommander
 **/
public interface ISipCommander {

    /**
     * 查询设备信息指令
     * @param device 设备
     * @return Boolean
     */
    Boolean deviceInfoQuery(GbDevice device);

    /**
     * 查询设备通道指令
     * @param device device
     * @param errorCallback 错误回调
     * @return Boolean
     */
    Boolean deviceCatalogQuery(GbDevice device, SipCallback errorCallback);


    /**
     *请求预览视频流
     * @param device 设备信息
     * @param ssrcInfo ssrc
     * @param channelId 通道信息
     * @param server 媒体服务信息
     * @param okCallback 成功回调
     * @param errorCallback 失败回调
     */
    void playRealtimeStream(GbDevice device, SsrcInfo ssrcInfo, String channelId, MediaServer server, SipCallback okCallback, SipCallback errorCallback);
}
