package com.yyh.media.config;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author: yyh
 * @date: 2021-12-10 15:39
 * @description: MediaProper 媒体服务器地址配置,当前适配zlMediaKit媒体服务器
 **/
@Getter
@Setter
@ToString
@Component
@ConfigurationProperties(prefix = "media", ignoreInvalidFields = true)
public class MediaProperties {

    /**
     * 媒体服务器 ip[必填]
     */
    private String ip;

    /**
     * hook ip[必填]
     */
    private String hookIp;

    /**
     * 媒体服务器 端口[必填]
     */
    private Integer port;

    /**
     * 连接秘钥[可选]
     */
    private String secret;

    /**
     * 录像管理端口
     * 录像辅助服务,部署此服务可以实现zlm录像的管理与下载, 0 表示不使用
     */
    private Integer recordAssistPort;

    /**
     * sdpIp
     */
    private String sdpIp;

    /**
     * streamIp
     */
    private String streamIp;

    public String getSdpIp() {
        if (StrUtil.isNotBlank(sdpIp)){
            return sdpIp;
        }else {
            return ip;
        }
    }

    public String getStreamIp() {
        if (StrUtil.isNotBlank(streamIp)){
            return streamIp;
        }else {
            return ip;
        }
    }
}
