package com.yyh.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.databind.util.ObjectBuffer;
import com.yyh.media.config.ZlmServerConfig;
import com.yyh.web.entity.GbDevice;
import com.yyh.web.entity.MediaServer;

import java.util.Map;

/**
 * @author: yyh
 * @date: 2021-12-17 15:41
 * @description: IMediaServerService
 **/
public interface IMediaServerService extends IService<MediaServer> {

    /**
     * 初始化媒体服务器
     * @param mediaServer mediaServer
     * @param config config
     * @param isUpdate isUpdate
     */
    void initMediaServer(MediaServer mediaServer, ZlmServerConfig config, Boolean isUpdate);

    /**
     * 分配媒体服务器
     * @param device 设备id
     * @return String
     */
    String distributeMediaServer(String device);

    /**
     * 取消分配媒体服务器
     * @param device 设备id
     */
    void undistributedMediaServer(String device);

    /**
     * 查询当前设备的媒体服务器
     * @param device 设备id
     * @return ZlmServerConfig
     */
    ZlmServerConfig queryMediaServerByDevice(String device);

    /**
     * 根据key获取媒体服务器信息
     * @param key key
     * @return ZlmServerConfig
     */
    ZlmServerConfig queryMediaServerByKey(String key);

    /**
     * 查询媒体服务器
     * @param deviceId 设备id
     * @return MediaServer
     */
    MediaServer queryMediaServerByDeviceId(String deviceId);
}
