package com.yyh.web.service.impl;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yyh.common.constant.CommonResultConstants;
import com.yyh.common.constant.DeviceConstants;
import com.yyh.common.constant.GlobalServerType;
import com.yyh.common.utils.CommonResult;
import com.yyh.config.ConverterType;
import com.yyh.gb28181.config.SipServerProperties;
import com.yyh.media.component.SsrcManagement;
import com.yyh.media.config.MediaProperties;
import com.yyh.media.config.SsrcConfig;
import com.yyh.media.config.ZlmServerConfig;
import com.yyh.media.constants.MediaConstant;
import com.yyh.media.constants.ServerApiConstant;
import com.yyh.media.service.IMediaServerRestful;
import com.yyh.media.utils.ConverterUtil;
import com.yyh.web.entity.MediaServer;
import com.yyh.web.mapper.MediaServerMapper;
import com.yyh.web.service.IMediaServerService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author: yyh
 * @date: 2021-12-17 15:41
 * @description: MediaServerServiceImpl
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class MediaServerServiceImpl  extends ServiceImpl<MediaServerMapper, MediaServer> implements IMediaServerService {

    private final RedisTemplate<String,String> redisTemplate;
    private final ObjectMapper mapper;
    private final IMediaServerRestful mediaServerRestful;
    private final SipServerProperties sipServerProperties;
    private final SsrcManagement ssrcManagement;
    @Value("${server.port:8080}")
    private int port;

    /**
     * ????????????????????????
     *
     * @param mediaServer mediaServer
     * @param config config
     * @param isUpdate    isUpdate
     */
    @Override
    public void initMediaServer(MediaServer mediaServer, ZlmServerConfig config, Boolean isUpdate) {
        if (isUpdate==null||!isUpdate){
            /// ??????
            this.save(mediaServer);
        }else {
            this.updateById(mediaServer);
        }
        /// ?????????
        try {
            config.setGeneralMediaServerIp(mediaServer.getIp());
            redisTemplate.opsForValue().set(MediaConstant.MEDIA_SERVER+config.getGeneralMediaServerId(),
                    mapper.writeValueAsString(config));
        } catch (JsonProcessingException e) {
            log.error("??????????????????????????????:{}->{}",config.getGeneralMediaServerId(),e.getMessage(),e);
        }
        /// ??????ssrc
        /// buildSsrc(mediaServer.getServerId());
        ssrcManagement.init(mediaServer.getServerId());
        /// ?????????????????????????????????hook
        String url = String.format(ServerApiConstant.SET_SERVER_CONFIG,mediaServer.getIp(),mediaServer.getHttpPort());
        Map<String,String> map = new HashMap<>(16);
        map.put(ServerApiConstant.SECRET,mediaServer.getSecret());
        map.put(ServerApiConstant.ON_SHELL_LOGIN_KEY,String.format(ServerApiConstant.ON_SHELL_LOGIN,mediaServer.getHookIp(),port));
        map.put(ServerApiConstant.ON_SERVER_KEEPALIVE_KEY,String.format(ServerApiConstant.ON_SERVER_KEEPALIVE,mediaServer.getHookIp(),port));
        map.put(ServerApiConstant.ON_SERVER_STARTED_KEY,String.format(ServerApiConstant.ON_SERVER_STARTED,mediaServer.getHookIp(),port));
        map.put(ServerApiConstant.ON_STREAM_CHANGED_KEY,String.format(ServerApiConstant.ON_STREAM_CHANGED,mediaServer.getHookIp(),port));
        map.put(ServerApiConstant.ON_STREAM_NONE_READER_KEY,String.format(ServerApiConstant.ON_STREAM_NONE_READER,mediaServer.getHookIp(),port));
        map.put(ServerApiConstant.HOOK_ENABLE,ServerApiConstant.HOOK_OPEN);
        map.put(ServerApiConstant.HTTP_CHARSET, CharsetUtil.UTF_8);
        mediaServerRestful.updateMediaServerConfig(url,map);
        log.info("??????????????????,??????hook");
    }

    /**
     * ?????????????????????
     *
     * @param device ??????id
     * @return ????????????????????????key
     */
    @Override
    public String distributeMediaServer(String device) {
        Random random = new Random();
        Set<String> keys = redisTemplate.keys(MediaConstant.MEDIA_SERVER + "*");
        if (keys==null||keys.size()==0){
            log.info("??????:{}???????????????????????????,?????????????????????????????????",device);
            return StrUtil.EMPTY;
        }
        List<String> listKeys = new ArrayList<>(keys);
        String key = listKeys.get(random.nextInt(listKeys.size()));
        redisTemplate.opsForHash().put(DeviceConstants.DEVICE + device, GlobalServerType.MEDIA_28181.toString(), key);
        log.info("??????:{},?????????????????????:{}",device,key.substring(key.lastIndexOf(":")+1));
        return key.trim();
    }

    /**
     * ???????????????????????????
     *
     * @param device ??????id
     */
    @Override
    public void undistributedMediaServer(String device) {
        String key =DeviceConstants.DEVICE + device;
        Boolean hasKey = redisTemplate.hasKey(key);
        if (hasKey!=null&&hasKey){
            redisTemplate.delete(key);
            log.info("??????:{},?????????????????????:{}",device,key);
        }
    }

    /**
     * ????????????????????????????????????
     *
     * @param device ??????id
     * @return Map<String,Object>
     */
    @Override
    public ZlmServerConfig queryMediaServerByDevice(String device) {
        String key = DeviceConstants.DEVICE + device;
        try {
            Boolean hasKey = redisTemplate.hasKey(key);
            if (hasKey!=null&&hasKey){
                Object o = redisTemplate.opsForHash().get(key, GlobalServerType.MEDIA_28181.toString());
                if (o!=null){
                    String k = o.toString().trim();
                    log.info("??????:{},???????????????:{}",device,k.substring(k.lastIndexOf(":")+1));
                    return queryMediaServerByKey(o.toString().trim());
                }else {
                    log.info("??????????????????:{}???????????????,??????????????????",device);
                    String k = distributeMediaServer(device);
                    if (StrUtil.isNotBlank(k)){
                        return queryMediaServerByKey(k);
                    }
                }
            }
        }catch (Exception ex){
            log.error("????????????:{}?????????????????????????????????:{}",device,ex.getMessage(),ex);
        }
        return null;
    }

    /**
     * ??????key???????????????????????????
     *
     * @param key key
     * @return ZlmServerConfig
     */
    @Override
    public ZlmServerConfig queryMediaServerByKey(String key) {
        String substring = key.substring(key.lastIndexOf(":") + 1);
        Boolean hasKey = redisTemplate.hasKey(MediaConstant.MEDIA_ONLINE_SERVER + substring);
        if (hasKey!=null&&hasKey){
            String s = redisTemplate.opsForValue().get(key);
            try {
                return mapper.readValue(s, ConverterType.ZLM_SERVER_CONFIG_TYPE);
            } catch (JsonProcessingException e) {
                log.error("?????????????????????:{}??????????????????:{}",key,e.getMessage(),e);
            }
        }else {
            log.warn("?????????????????????:{}",substring);
        }
        return null;
    }

    /**
     * ?????????????????????
     *
     * @param deviceId ??????id
     * @return MediaServer
     */
    @Override
    public MediaServer queryMediaServerByDeviceId(String deviceId) {
        String serverId = null;
        String id = StrUtil.EMPTY;
        String key = DeviceConstants.DEVICE + deviceId;
        try {
            Boolean hasKey = redisTemplate.hasKey(key);
            if (hasKey!=null&&hasKey){
                Object o = redisTemplate.opsForHash().get(key, GlobalServerType.MEDIA_28181.toString());
                if (o!=null){
                    String k = o.toString().trim();
                    serverId = k.substring(k.lastIndexOf(":")+1);
                    log.info("??????:{},???????????????:{}",deviceId,serverId);
                }else {
                    log.warn("??????????????????:{}???????????????,??????????????????",deviceId);
                    id = distributeMediaServer(deviceId);
                }
            }
            else {
                log.warn("??????????????????:{}???????????????,??????????????????",deviceId);
                id = distributeMediaServer(deviceId);
            }
            if (StrUtil.isNotBlank(id)){
                serverId = id.substring(id.lastIndexOf(":")+1);
            }
        }catch (Exception ex){
            log.error("????????????:{}?????????????????????????????????:{}",deviceId,ex.getMessage(),ex);
        }
        if (StrUtil.isNotBlank(serverId)){
            List<MediaServer> list = this.list(Wrappers.<MediaServer>lambdaQuery().eq(MediaServer::getServerId, serverId));
            if (list==null||list.size()==0){
                log.warn("????????????db????????????:{}",serverId);
                return null;
            }
            return list.get(0);
        }
        log.warn("??????????????????:{}???????????????,??????????????????",deviceId);
        return null;
    }

    /**
     * ?????????????????????
     *
     * @param serverId ????????????id
     * @return MediaServer
     */
    @Override
    public MediaServer queryMediaServerByServerId(String serverId) {
        List<MediaServer> list = this.list(Wrappers.<MediaServer>lambdaQuery().eq(MediaServer::getServerId, serverId));
        if (list==null){
            return null;
        }
        return list.get(0);
    }

    /**
     * ?????????????????????????????????
     *
     * @param ip     ???????????????ip
     * @param port   ???????????????port
     * @param secret ?????????????????????
     * @return MediaServer
     */
    @Override
    public CommonResult<MediaServer> checkMediaServer(String ip, Integer port, String secret) {
        MediaServer mediaServer = new MediaServer();
        CommonResult<MediaServer> result = new CommonResult<>();
        LambdaQueryWrapper<MediaServer> eq = Wrappers.<MediaServer>lambdaQuery().eq(MediaServer::getIp, ip).eq(MediaServer::getHttpPort, port);
        List<MediaServer> list = this.list(eq);
        if (list!=null&&list.size()>0){
            result.setCode(CommonResultConstants.SUCCESS);
            result.setMsg(String.format("??????????????????????????????:%s | %s:%d",list.get(0).getServerId(),ip,port));
            return result;
        }
        String url = String.format(ServerApiConstant.GET_SERVER_CONFIG,ip,port,secret);
        ArrayList<Map<String, Object>> arrayList = mediaServerRestful.queryMediaServerInfo(url);
        if (arrayList==null){
            result.setCode(CommonResultConstants.SUCCESS);
            result.setMsg(String.format("?????????????????????????????????: %s:%d",ip,port));
            return result;
        }
        ArrayList<ZlmServerConfig> zlmServerConfigs = mapper.convertValue(arrayList, ConverterType.ARRAY_LIST_TYPE);
        if (zlmServerConfigs==null||zlmServerConfigs.size()==0){
            result.setCode(CommonResultConstants.SUCCESS);
            result.setMsg(String.format("?????????????????????????????????: %s:%d",ip,port));
            return result;
        }
        ZlmServerConfig config = zlmServerConfigs.get(0);
        ConverterUtil.converterZlMediaServerConfig2MediaServer(config,mediaServer);
        mediaServer.setUpdateTime(LocalDateTime.now());
        mediaServer.setHookIp(sipServerProperties.getIp());
        return CommonResult.success(mediaServer);
    }

    /**
     * ?????????????????????
     *
     * @param mediaServer ?????????????????????
     * @return Boolean
     */
    @Override
    public Boolean saveMediaServer(MediaServer mediaServer) {
        ArrayList<ZlmServerConfig> configs;
        String url = String.format(
                ServerApiConstant.GET_SERVER_CONFIG,
                mediaServer.getIp(),
                mediaServer.getHttpPort(),
                mediaServer.getSecret());
        ArrayList<Map<String, Object>> data = mediaServerRestful.queryMediaServerInfo(url);
        if (data!=null){
            try {
                configs = mapper.convertValue(data, ConverterType.ARRAY_LIST_TYPE);
                if (configs!=null&&configs.size()>0){
                    for (ZlmServerConfig config: configs){
                        MediaServer server = new MediaServer();
                        ConverterUtil.converterZlMediaServerConfig2MediaServer(config, server);
                        server.setUpdateTime(LocalDateTime.now());
                        this.initMediaServer(server,config,false);
                    }
                    return true;
                }
            }catch (IllegalArgumentException ex){
                log.error("???????????????????????????????????????:{}",ex.getMessage(),ex);
            }
        }
        log.info("????????????????????????????????? | {}:{}",mediaServer.getIp(),mediaServer.getHttpPort());
        return false;
    }

    /**
     * ?????????????????????
     *
     * @param mediaServer mediaServer
     * @return Boolean
     */
    @Override
    public Boolean updateMediaServerById(MediaServer mediaServer) {
        mediaServer.setUpdateTime(LocalDateTime.now());
        return this.updateById(mediaServer);
    }

    /**
     * ??????ssrc
     * @param serverId serverId
     */
    private void buildSsrc(String serverId){
        SsrcConfig ssrcConfig = new SsrcConfig(serverId,null,sipServerProperties.getDomain());
        try {
            redisTemplate.opsForHash().put(
                    MediaConstant.SSRC_SERVER+serverId,
                    "mediaServerId",
                    serverId);
            redisTemplate.opsForHash().put(
                    MediaConstant.SSRC_SERVER+serverId,
                    "prefix",
                    ssrcConfig.getSsrcPrefix());
            redisTemplate.opsForHash().put(
                    MediaConstant.SSRC_SERVER+serverId,
                    "used",
                    mapper.writeValueAsString(ssrcConfig.getIsUsed()));
            redisTemplate.opsForHash().put(
                    MediaConstant.SSRC_SERVER+serverId,
                    "unused",
                    mapper.writeValueAsString(ssrcConfig.getNotUsed()));
        }catch (JsonProcessingException ex){
            log.error("??????ssrc????????????:{}",ex.getMessage(),ex);
        }
    }
}
