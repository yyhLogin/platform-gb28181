package com.yyh.media.service;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author: yyh
 * @date: 2021-12-16 17:01
 * @description: IMediaServerRestful(媒体服务器接口)
 **/
public interface IMediaServerRestful {
    /**
     * 获取媒体服务器信息
     * @param url 查询接口
     * @return Map<String,Object>
     */
    ArrayList<Map<String, Object>> queryMediaServerInfo(String url);

    /**
     * 更新媒体服务器配置
     * @param url 请求url
     * @param map map
     * @return Boolean
     */
    Boolean updateMediaServerConfig(String url, Map<String,String> map);

    /**
     * 创建GB28181 RTP接收端口，如果该端口接收数据超时，则会自动被回收(不用调用closeRtpServer接口)
     * @param url api url
     * @param secret api操作密钥(配置文件配置)，如果操作ip是127.0.0.1，则不需要此参数
     * @param port 接收端口，0则为随机端口
     * @param enableTcp 启用UDP监听的同时是否监听TCP端口
     * @param streamId 该端口绑定的流ID，该端口只能创建这一个流(而不是根据ssrc创建多个)
     * @return Integer
     */
    Map<String,Object> openRtpServer(String url,String secret,Integer port,String enableTcp,String streamId);

    /**
     * 关闭GB28181 RTP接收端口
     * @param url api url
     * @param secret api操作密钥(配置文件配置)，如果操作ip是127.0.0.1，则不需要此参数
     * @param streamId 调用openRtpServer接口时提供的流ID
     * @return Map<String,Object>
     */
    Map<String,Object> closeRtpServer(String url,String secret,String streamId);


    /**
     * 获取流列表
     * @param url 请求url
     * @param schema 筛选协议，例如 rtsp或rtmp
     * @param vhost 筛选虚拟主机，例如__defaultVhost__
     * @param app 筛选应用名，例如 live
     * @param stream 筛选流id，例如 test
     * @return ArrayList<Map<String, Object>>
     */
    ArrayList<Map<String, Object>> getMediaList(String url,String schema,String vhost,String app,String stream);
}
