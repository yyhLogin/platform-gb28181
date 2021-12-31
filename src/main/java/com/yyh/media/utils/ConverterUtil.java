package com.yyh.media.utils;

import com.yyh.media.config.ZlmServerConfig;
import com.yyh.web.entity.MediaServer;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * @author: yyh
 * @date: 2021-12-22 17:24
 * @description: ConverterUtil
 **/
@Slf4j
public class ConverterUtil {
    /**
     * ZlMediaServerConfig->MediaServer
     * @param config ZlMediaServerConfig
     * @param server MediaServer
     * @return MediaServer
     */
    public static void converterZlMediaServerConfig2MediaServer(ZlmServerConfig config, MediaServer server){
        try {
            server.setHttpPort(config.getHttpPort());
            server.setHttpSslPort(config.getHttpSslPort());
            server.setUpdateTime(LocalDateTime.now());
            server.setRtmpPort(config.getRtmpPort());
            server.setRtmpSslPort(config.getRtmpSslPort());
            server.setRtspPort(config.getRtspPort());
            server.setRtspSslPort(config.getRtspSslPort());
            server.setRtpProxyPort(config.getRtpProxyPort());
            server.setServerId(config.getGeneralMediaServerId());
            server.setSecret(config.getApiSecret());
            server.setStreamNoneReaderDelayMs(config.getGeneralStreamNoneReaderDelayMs());
            server.setHookAliveInterval((int) Double.parseDouble(config.getHookAliveInterval()));
        }catch (Exception ex){
            log.info("转换媒体服务信息出现异常:{}",ex.getMessage(),ex);
        }
    }
}
