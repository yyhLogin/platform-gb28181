package com.yyh.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyh.web.entity.DeviceChannel;
import com.yyh.web.mapper.GbDeviceChannelMapper;
import com.yyh.web.service.IGbDeviceChannelService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: yyh
 * @date: 2022-04-24 15:42
 * @description: GbDeviceChannelServiceImpl
 **/
@Service
public class GbDeviceChannelServiceImpl extends ServiceImpl<GbDeviceChannelMapper, DeviceChannel> implements IGbDeviceChannelService {

    /**
     * 根据平台id查询关联的通道id
     *
     * @param serverGbId 平台id
     * @return List<DeviceChannel>
     */
    @Override
    public List<DeviceChannel> queryByPlatformId(String serverGbId) {
        return baseMapper.queryByPlatformId(serverGbId);
    }
}
