package com.yyh.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yyh.web.entity.DeviceChannel;

import java.util.List;

/**
 * @author: yyh
 * @date: 2022-04-24 15:42
 * @description: IGbDeviceChannelService
 **/
public interface IGbDeviceChannelService extends IService<DeviceChannel> {
    /**
     * 根据平台id查询关联的通道id
     * @param serverGbId 平台id
     * @return List<DeviceChannel>
     */
    List<DeviceChannel> queryByPlatformId(String serverGbId);
}
