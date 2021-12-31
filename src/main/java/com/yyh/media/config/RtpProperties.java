package com.yyh.media.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author: yyh
 * @date: 2021-12-20 15:55
 * @description: RtpProperties
 * 启用多端口模式, 多端口模式使用端口区分每路流，兼容性更好。 单端口使用流的ssrc区分， 点播超时建议使用多端口测试
 **/
@Getter
@Setter
@ToString
@Component
@ConfigurationProperties(prefix = "media.rtp", ignoreInvalidFields = true)
public class RtpProperties {

    /**
     * 是否启用rtp多端口
     */
    private Boolean enable;

    /**
     * 在此范围内选择端口用于媒体流传输
     * 30000,30500 # 端口范围
     */
    private String portRange;

    /**
     * 国标级联在此范围内选择端口发送媒体流
     * 30000,30500 # 端口范围
     */
    private String sendPortRange;
}
