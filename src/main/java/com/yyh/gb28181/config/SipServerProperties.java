package com.yyh.gb28181.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author: yyh
 * @date: 2021-11-23 17:21
 * @description: SipConfig
 **/
@Getter
@Setter
@ToString
@Component
@ConfigurationProperties(prefix = "sip", ignoreInvalidFields = true)
public class SipServerProperties {

    /**
     * 是否启用sip(默认开启)
     */
    private Boolean enabled;
    /**
     * sip服务器ip
     */
    private String ip;
    /**
     * 默认使用 0.0.0.0
     */
    private String monitorIp = "0.0.0.0";
    /**
     * sip服务器端口(默认使用5060)
     */
    private Integer port = 5060;

    /**
     * sip 服务器域
     */
    private String domain;

    /**
     * sip服务器id
     */
    private String id;

    /**
     * sip服务器连接密码
     */
    private String password;

    /**
     * 转动速度
     */
    private Integer ptzSpeed = 50;

    /**
     * 心跳超时时间
     */
    private Integer keepaliveTimeOut = 255;

    /**
     * 注册过期时间
     */
    private Integer registerTimeInterval = 60;
}
