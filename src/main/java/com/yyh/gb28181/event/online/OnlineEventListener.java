package com.yyh.gb28181.event.online;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yyh.gb28181.command.ISipCommander;
import com.yyh.gb28181.constant.SipRequestConstant;
import com.yyh.gb28181.constant.VideoManagerConstant;
import com.yyh.media.constants.MediaConstant;
import com.yyh.web.entity.GbDevice;
import com.yyh.web.service.IGbDeviceService;
import com.yyh.web.service.IMediaServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author: yyh
 * @date: 2021-12-06 14:30
 * @description: OnlineEventListener
 **/
@Slf4j
@Component
public class OnlineEventListener implements ApplicationListener<OnlineEvent> {

    @Resource
    private RedisTemplate<String,String> redisTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private IGbDeviceService deviceService;

    @Resource
    private ISipCommander sipCommander;

    @Resource
    private IMediaServerService mediaServerService;

    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Async
    @Override
    public void onApplicationEvent(OnlineEvent event) {
        if (StrUtil.isBlank(event.getFrom())){
            log.info("OnlineEvent事件来源未知,不能处理");
        }
        switch (event.getFrom()){
            // 注册时触发的在线事件，先在redis中增加超时监听
            case VideoManagerConstant.EVENT_ONLINE_REGISTER:
                register(event.getDevice());
                queryDevice(event.getDevice());
                mediaServerService.distributeMediaServer(event.getDevice().getGbId());
                break;
            // 设备主动发送心跳触发的在线事件
            case VideoManagerConstant.EVENT_ONLINE_KEEPALIVE:
                keepAlive(event.getId());
                break;
            // 设备主动发送消息触发的在线事件
            case VideoManagerConstant.EVENT_ONLINE_MESSAGE:
                break;
            // 设备发送离线消息
            case VideoManagerConstant.EVENT_ONLINE_OUTLINE:
                outline(event.getId());
                mediaServerService.undistributedMediaServer(event.getId());
                break;
            default:
                break;
        }
    }

    /**
     * 查询设备信息
     * @param device device
     */
    private void queryDevice(GbDevice device) {
        Boolean queryInfo = sipCommander.deviceInfoQuery(device);
        Boolean catalog = sipCommander.deviceCatalogQuery(device, null);
        log.info("查询设备信息指令: queryInfo|{},catalog|{}",queryInfo,catalog);
    }

    /**
     * 注册
     * @param device device
     */
    private void register(GbDevice device){
        String key = VideoManagerConstant.DEVICE_ONLINE + device.getGbId();
        redisTemplate.opsForValue().set(key,"online",VideoManagerConstant.ONLINE_EXPIRED_SECOND, TimeUnit.SECONDS);
        GbDevice byId = deviceService.getById(device.getGbId());
        if (byId==null){
            device.setStreamMode(SipRequestConstant.UDP);
            device.setCharset(VideoManagerConstant.DEVICE_DEFAULT_CHARSET);
            device.setCreateTime(LocalDateTime.now());
            device.setRegisterTime(LocalDateTime.now());
            device.setOnline(VideoManagerConstant.DEVICE_ONLINE_STATE);
            deviceService.save(device);
        }else {
            byId.setRegisterTime(LocalDateTime.now());
            byId.setUpdateTime(LocalDateTime.now());
            byId.setIp(device.getIp());
            byId.setPort(device.getPort());
            byId.setHostAddress(device.getHostAddress());
            byId.setExpires(device.getExpires());
            byId.setTransport(device.getTransport());
            deviceService.updateById(byId);
        }
    }

    /**
     * 心跳
     * @param id id
     */
    private void keepAlive(String id){
        String key = VideoManagerConstant.DEVICE_ONLINE + id;
        redisTemplate.opsForValue().set(key,"online", VideoManagerConstant.ONLINE_EXPIRED_SECOND, TimeUnit.SECONDS);
        GbDevice byId = deviceService.getById(id);
        if (byId==null){
            log.error("Keepalive error | 设备不存在 :{}",id);
            return;
        }
        byId.setKeepaliveTime(LocalDateTime.now());
        byId.setOnline(VideoManagerConstant.DEVICE_ONLINE_STATE);
        deviceService.updateById(byId);
    }

    /**
     * 下线
     * @param id id
     */
    private void outline(String id){
        String key = VideoManagerConstant.DEVICE_ONLINE + id;
        redisTemplate.delete(key);
        GbDevice byId = deviceService.getById(id);
        if (byId==null){
            log.error("Keepalive error | 设备不存在 :{}",id);
            return;
        }
        byId.setOnline(VideoManagerConstant.DEVICE_OFFLINE_STATE);
        deviceService.updateById(byId);
    }
}
