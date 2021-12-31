package com.yyh.media;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Stopwatch;
import com.yyh.config.ConverterType;
import com.yyh.media.config.MediaProperties;
import com.yyh.media.config.RtpProperties;
import com.yyh.media.config.ZlmServerConfig;
import com.yyh.media.constants.MediaConstant;
import com.yyh.media.constants.ServerApiConstant;
import com.yyh.media.service.IMediaServerRestful;
import com.yyh.media.subscribe.HookType;
import com.yyh.media.subscribe.ZlmHttpHookSubscribe;
import com.yyh.media.utils.ConverterUtil;
import com.yyh.web.entity.MediaServer;
import com.yyh.web.service.IMediaServerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author: yyh
 * @date: 2021-12-13 09:54
 * @description: ZlMediaServerInit
 **/
@Slf4j
@Component
@Order(10086)
public class ZlmMediaServerInit implements ApplicationRunner {




    private int count = 1;

    @Value("${server.port:8080}")
    private int port;

    @Resource
    private IMediaServerRestful mediaServerRestful;

    @Resource
    private MediaProperties mediaProperties;

    @Resource
    private RtpProperties rtpProperties;

    @Resource
    private ObjectMapper mapper;

    @Resource
    private IMediaServerService mediaServerService;

    @Resource
    private RedisTemplate<String,String> redisTemplate;

    @Resource
    private ZlmHttpHookSubscribe subscribe;

    /**
     * 初始化流媒体服务器
     * @param args args
     * @throws Exception Exception
     * 1.先从数据库初始化
     * 2.从配置文件初始化
     *  配置文件初始化时先看是否已经初始化到数据库
     */
    @Async
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("***********开始初始化流媒体服务器ZLMediaKit***********");
        log.info("项目运行端口:{}",port);
        Stopwatch watch = Stopwatch.createStarted();
        /// 清空redis缓存中之前存在的服务器
        Set<String> keys = redisTemplate.keys(MediaConstant.MEDIA_SERVER + "*");
        if (keys!=null&&keys.size()>0){
            redisTemplate.delete(keys);
        }
        initDbMediaServer();
        initConfigMediaServer();
        log.info("***********结束初始化流媒体服务器ZLMediaKit:耗时:{}ms***********",watch.elapsed(TimeUnit.MILLISECONDS));
    }

    /**
     * 初始化媒体服务器(从配置文件初始化)
     */
    private void initConfigMediaServer(){
        List<MediaServer> list = mediaServerService.list(
                Wrappers.<MediaServer>lambdaQuery()
                        .eq(MediaServer::getIp, mediaProperties.getIp())
                        .eq(MediaServer::getHttpPort, mediaProperties.getPort()));
        if (list.size()==0){
            ArrayList<ZlmServerConfig> configs = init(mediaProperties.getIp(), mediaProperties.getPort(), mediaProperties.getSecret());
            for (ZlmServerConfig config: configs){
                MediaServer server = new MediaServer();
                ConverterUtil.converterZlMediaServerConfig2MediaServer(config,server);
                server.setHookIp(mediaProperties.getHookIp());
                server.setIp(mediaProperties.getIp());
                server.setSdpIp(mediaProperties.getSdpIp());
                server.setStreamIp(mediaProperties.getStreamIp());
                server.setRtpEnable(rtpProperties.getEnable()? MediaConstant.IS_TRUE:MediaConstant.IS_FALSE);
                server.setRtpPortRange(rtpProperties.getPortRange());
                server.setSendRtpPortRange(rtpProperties.getSendPortRange());
                server.setRecordAssistPort(mediaProperties.getRecordAssistPort());
                server.setCreateTime(LocalDateTime.now());
                server.setAutoConfig(MediaConstant.IS_TRUE);
                server.setDefaultServer(MediaConstant.IS_TRUE);
                mediaServerService.initMediaServer(server,config,false);
            }
            log.info("");
        }
    }

    /**
     * 初始化媒体服务器(从数据库初始化)
     */
    private void initDbMediaServer(){
        List<MediaServer> list = mediaServerService.list();
        if (list.size()==0){
            return;
        }
        for (MediaServer server:list){
            ArrayList<ZlmServerConfig> configs = init(server.getIp(),server.getHttpPort(),server.getSecret());
            if (configs!=null&&configs.size()>0){
                for (ZlmServerConfig config: configs){
                    ConverterUtil.converterZlMediaServerConfig2MediaServer(config, server);
                    server.setUpdateTime(LocalDateTime.now());
                    mediaServerService.initMediaServer(server,config,true);
                }
            }
        }
    }

    /**
     * 初始化媒体服务器
     * @param ip 媒体服务器ip
     * @param port 媒体服务器port
     * @param secret 媒体服务器秘钥
     * @return ArrayList<ZlMediaServerConfig>
     */
    private ArrayList<ZlmServerConfig> init(String ip,int port,String secret) {
        ArrayList<ZlmServerConfig> configs = null;
        //配置文件中的mediaServer未初始化到数据库
        String url = String.format(ServerApiConstant.GET_SERVER_CONFIG,ip,port,secret);
        int maxCount = 4;
        boolean isInit = false;
        while(!isInit &&count< maxCount) {
            log.info("第{}次初始化媒体服务器:{}", count, url);
            ArrayList<Map<String, Object>> data = mediaServerRestful.queryMediaServerInfo(url);
            if (data!=null){
                try {
                    configs = mapper.convertValue(data, ConverterType.ArrayList_TYPE);
                    isInit = true;
                } catch (Exception ex) {
                    log.error("初始化媒体服务器出现异常:{}.....2s后重试", ex.getMessage(), ex);
                    try {
                        TimeUnit.MILLISECONDS.sleep(2000);
                    } catch (InterruptedException e) {
                        log.error("等待重新初始化媒体服务器出现异常:{}",e.getMessage(),e);
                    }
                } finally {
                    count++;
                }
            }else {
                count++;
                log.error("初始化媒体服务器失败.....2s后重试");
                try {
                    TimeUnit.MILLISECONDS.sleep(2000);
                } catch (InterruptedException e) {
                    log.error("等待重新初始化媒体服务器出现异常:{}",e.getMessage(),e);
                }
            }
        }
        return configs;
    }
}
