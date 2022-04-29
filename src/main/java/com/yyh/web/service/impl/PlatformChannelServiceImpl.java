package com.yyh.web.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyh.gb28181.event.platform.OnPlatformChannelUpdateEvent;
import com.yyh.web.dto.PlatformChannelDTO;
import com.yyh.web.entity.DeviceChannel;
import com.yyh.web.entity.PlatformChannel;
import com.yyh.web.mapper.PlatformChannelMapper;
import com.yyh.web.service.IPlatformChannelService;
import com.yyh.web.vo.PlatformChannelVO;
import org.checkerframework.checker.units.qual.C;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: yyh
 * @date: 2022-04-24 18:18
 * @description: IPlatformChannelServiceImpl
 **/
@Service
public class PlatformChannelServiceImpl extends ServiceImpl<PlatformChannelMapper, PlatformChannel> implements IPlatformChannelService {

    private final ApplicationEventPublisher publisher;

    public PlatformChannelServiceImpl(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }


    /**
     * 分页查询级联平台的所有所有通道
     *
     * @param current     页数
     * @param size        每页数量
     * @param platformId  上级平台ID
     * @param catalogId   目录ID
     * @param query       查询内容
     * @param online      是否在线
     * @param channelType 通道类型
     * @return Page<PlatformChannelVO>
     */
    @Override
    public Page<PlatformChannelVO> queryChannelByPage(int current, int size, String platformId, String catalogId, String query, Boolean online, Boolean channelType) {
        Page<PlatformChannelVO> page = new Page<>(current,size);
        return baseMapper.queryChannelByPage(page,platformId,catalogId,query,online,channelType);
    }

    /**
     * 将通道共享到上级平台
     *
     * @param channel 设备信息
     * @return Boolean
     */
    @Override
    public Boolean pushChannel2Platform(PlatformChannelDTO channel) {
        List<PlatformChannelVO> channels = channel.getChannels();
        for (PlatformChannelVO vo : channels){
            PlatformChannel p = new PlatformChannel();
            p.setCatalogId(vo.getCatalogId());
            p.setPlatformId(channel.getPlatformId());
            p.setDeviceChannelId(vo.getId());
            //判断当前设备是否共享到上级平台
            List<PlatformChannel> list = this.list(Wrappers.<PlatformChannel>lambdaQuery()
                    .eq(PlatformChannel::getPlatformId, channel.getPlatformId())
                    .eq(PlatformChannel::getDeviceChannelId, vo.getId()));
            if (list!=null&&list.size()>0){
                p.setId(list.get(0).getId());
                this.updateById(p);
            }else {
                this.save(p);
            }
        }
        OnPlatformChannelUpdateEvent event = new OnPlatformChannelUpdateEvent("channel",channel.getPlatformId());
        publisher.publishEvent(event);
        return true;
    }

    /**
     * 将通道从上级平台下线
     *
     * @param channel 通道信息
     * @return Boolean
     */
    @Override
    public Boolean downChannel2Platform(PlatformChannelDTO channel) {
        List<PlatformChannelVO> channels = channel.getChannels();
        for (PlatformChannelVO vo : channels){
            this.remove(Wrappers.<PlatformChannel>lambdaQuery()
                    .eq(PlatformChannel::getPlatformId, channel.getPlatformId())
                    .eq(PlatformChannel::getDeviceChannelId, vo.getId()));
        }
        OnPlatformChannelUpdateEvent event = new OnPlatformChannelUpdateEvent("channel",channel.getPlatformId());
        publisher.publishEvent(event);
        return true;
    }

    /**
     * 根据上级平台id和通道id查询通道
     *
     * @param platformId platformId
     * @param channelId  channelId
     * @return DeviceChannel
     */
    @Override
    public DeviceChannel queryChannelByPlatformIdAndChannelId(String platformId, String channelId) {
        return baseMapper.queryChannelById(platformId, channelId);
    }
}
