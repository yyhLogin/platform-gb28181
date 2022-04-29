package com.yyh.web.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yyh.web.entity.DeviceChannel;
import com.yyh.web.dto.GbDeviceDTO;
import com.yyh.web.entity.GbDevice;

import java.util.List;

/**
 * @author: yyh
 * @date: 2021-12-06 16:48
 * @description: IGbDeviceService
 **/
public interface IGbDeviceService extends IService<GbDevice> {
    /**
     * 根据host和port获取国标设备信息
     * @param host host
     * @param port port
     * @return GbDevice
     */
    GbDevice getDeviceByHostAndPort(String host, int port);

    /**
     * 分页查询国标设备
     * @param page 分页参数
     * @param device 查询参数
     * @return Page<GbDevice>
     */
    Page<GbDevice> queryDeviceByPage(Page<GbDevice> page, GbDeviceDTO device);

    /**
     * 更新设备的流传输模式
     * @param deviceId 设备id
     * @param streamMode 流传输模式
     * @return Boolean
     */
    Boolean updateStreamMode(String deviceId, String streamMode);

    /**
     * 查询设备的通道信息
     * @param deviceId 设备id
     * @return List<DeviceChannel>
     */
    List<DeviceChannel> queryDeviceChannelByDeviceId(String deviceId);

    /**
     * 更新设备信息
     * @param deviceId 设备id
     * @param name 设备名称
     * @param charSet 设备通信字符集
     * @param sub 目录订阅周期(0-不订阅)
     * @return Boolean
     */
    Boolean updateDeviceById(String deviceId, String name, String charSet, String sub);
}
