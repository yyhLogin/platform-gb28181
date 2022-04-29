package com.yyh.web.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yyh.common.constant.CommonResultConstants;
import com.yyh.common.exception.PlatformException;
import com.yyh.gb28181.constant.VideoManagerConstant;
import com.yyh.web.entity.DeviceChannel;
import com.yyh.web.dto.GbDeviceDTO;
import com.yyh.web.entity.GbDevice;
import com.yyh.web.mapper.GbDeviceMapper;
import com.yyh.web.service.IGbDeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author: yyh
 * @date: 2021-12-06 16:49
 * @description: GbDeviceServiceImpl
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class GbDeviceServiceImpl extends ServiceImpl<GbDeviceMapper, GbDevice> implements IGbDeviceService {

    private final RedisTemplate<String,String> redisTemplate;

    private final ObjectMapper mapper;

    /**
     * 根据host和port获取国标设备信息
     *
     * @param host host
     * @param port port
     * @return GbDevice
     */
    @Override
    public GbDevice getDeviceByHostAndPort(String host, int port) {
        List<GbDevice> list = this.list(Wrappers.<GbDevice>lambdaQuery().eq(GbDevice::getIp, host).eq(GbDevice::getPort, port));
        if (list==null){
            throw new PlatformException(CommonResultConstants.FAIL,String.format("未找到设备:%s:%s",host,port));
        }
        return list.get(0);
    }

    /**
     * 分页查询国标设备
     *
     * @param page   分页参数
     * @param device 查询参数
     * @return Page<GbDevice>
     */
    @Override
    public Page<GbDevice> queryDeviceByPage(Page<GbDevice> page, GbDeviceDTO device) {
        LambdaQueryWrapper<GbDevice> eq = Wrappers.<GbDevice>lambdaQuery()
                .like(StrUtil.isNotBlank(device.getGbId()), GbDevice::getGbId, device.getGbId())
                .like(StrUtil.isNotBlank(device.getName()), GbDevice::getName, device.getName())
                .like(StrUtil.isNotBlank(device.getManufacturer()), GbDevice::getManufacturer, device.getManufacturer())
                .like(StrUtil.isNotBlank(device.getModel()), GbDevice::getModel, device.getModel())
                .like(StrUtil.isNotBlank(device.getFirmware()), GbDevice::getFirmware, device.getFirmware())
                .eq(StrUtil.isNotBlank(device.getTransport()), GbDevice::getTransport, device.getTransport())
                .eq(StrUtil.isNotBlank(device.getStreamMode()), GbDevice::getStreamMode, device.getStreamMode())
                .eq(StrUtil.isNotBlank(device.getOnline()), GbDevice::getOnline, device.getOnline())
                .eq(StrUtil.isNotBlank(device.getCharset()), GbDevice::getCharset, device.getCharset());
        return this.page(page,eq);
    }

    /**
     * 更新设备的流传输模式
     *
     * @param deviceId   设备id
     * @param streamMode 流传输模式
     * @return Boolean
     */
    @Override
    public Boolean updateStreamMode(String deviceId, String streamMode) {
        LambdaUpdateWrapper<GbDevice> set = Wrappers.<GbDevice>lambdaUpdate()
                .set(GbDevice::getStreamMode, streamMode)
                .eq(GbDevice::getGbId,deviceId);
        return this.update(set);
    }

    /**
     * 查询设备的通道信息
     *
     * @param deviceId 设备id
     * @return List<DeviceChannel>
     */
    @Override
    public List<DeviceChannel> queryDeviceChannelByDeviceId(String deviceId) {
        List<DeviceChannel> list = new ArrayList<>();
        String key = VideoManagerConstant.DEVICE_CHANEL_28181+deviceId;
        Boolean hasKey = redisTemplate.hasKey(key);
        if (hasKey!=null&&hasKey){
            Set<Object> keys = redisTemplate.opsForHash().keys(key);
            if (keys.size()==0){
                return list;
            }
            for (Object obj : keys){
                Object o = redisTemplate.opsForHash().get(key, obj.toString());
                try {
                    assert o != null;
                    list.add(mapper.readValue(o.toString(),DeviceChannel.class));
                } catch (JsonProcessingException e) {
                    log.info("解析设备通道信息出现异常:{}",e.getMessage(),e);
                }
            }
        }
        return list;
    }

    /**
     * 更新设备信息
     *
     * @param deviceId 设备id
     * @param name     设备名称
     * @param charSet  设备通信字符集
     * @param sub      目录订阅周期(0-不订阅)
     * @return Boolean
     */
    @Override
    public Boolean updateDeviceById(String deviceId, String name, String charSet, String sub) {
        LambdaUpdateWrapper<GbDevice> eq = Wrappers.<GbDevice>lambdaUpdate()
                .set(StrUtil.isNotBlank(name), GbDevice::getName, name)
                .set(StrUtil.isNotBlank(charSet), GbDevice::getCharset, charSet)
                .set(StrUtil.isNotBlank(sub), GbDevice::getSubscribeCycleForCatalog, sub)
                .eq(GbDevice::getGbId, deviceId);
        return this.update(eq);
    }
}
