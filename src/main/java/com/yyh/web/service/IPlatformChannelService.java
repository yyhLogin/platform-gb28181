package com.yyh.web.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yyh.web.dto.PlatformChannelDTO;
import com.yyh.web.entity.DeviceChannel;
import com.yyh.web.entity.PlatformChannel;
import com.yyh.web.vo.PlatformChannelVO;

/**
 * @author: yyh
 * @date: 2022-04-24 18:17
 * @description: IPlatformChannelService
 **/
public interface IPlatformChannelService extends IService<PlatformChannel> {

    /**
     * 分页查询级联平台的所有所有通道
     * @param current 页数
     * @param size 每页数量
     * @param platformId 上级平台ID
     * @param catalogId 目录ID
     * @param query 查询内容
     * @param online 是否在线
     * @param channelType 通道类型
     * @return Page<PlatformChannelVO>
     */
    Page<PlatformChannelVO> queryChannelByPage(int current, int size, String platformId, String catalogId, String query, Boolean online, Boolean channelType);

    /**
     * 将通道共享到上级平台
     * @param channel
     * @return Boolean
     */
    Boolean pushChannel2Platform(PlatformChannelDTO channel);

    /**
     * 将通道从上级平台下线
     * @param channel 通道信息
     * @return Boolean
     */
    Boolean downChannel2Platform(PlatformChannelDTO channel);

    /**
     * 根据上级平台id和通道id查询通道
     * @param platformId platformId
     * @param channelId channelId
     * @return DeviceChannel
     */
    DeviceChannel queryChannelByPlatformIdAndChannelId(String platformId, String channelId);
}
