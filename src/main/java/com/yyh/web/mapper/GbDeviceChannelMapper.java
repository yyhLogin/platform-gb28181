package com.yyh.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyh.web.entity.DeviceChannel;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author: yyh
 * @date: 2022-04-24 15:40
 * @description: GbDeviceChannel
 **/
public interface GbDeviceChannelMapper extends BaseMapper<DeviceChannel> {


    /**
     * 根据平台id查询符合的设备通道
     * @param platformId 平台id
     * @return List<DeviceChannel>
     */
    @Select(value = {" <script>" +
            "SELECT " +
            "    dc.*\n" +
            " FROM gb_device_channel dc " +
            " LEFT JOIN gb_platform_channel pc on pc.device_channel_id = dc.id " +
            " WHERE pc.platform_id = #{platformId} " +
            " ORDER BY dc.device_id, dc.channel_id ASC" +
            " </script>"})
    List<DeviceChannel> queryByPlatformId(String platformId);
}
