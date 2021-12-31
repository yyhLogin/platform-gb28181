package com.yyh.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author: yyh
 * @date: 2021-12-30 15:00
 * @description: SipConfigProperties
 **/
@Getter
@Setter
@ToString
@Component
@ConfigurationProperties(prefix = "global", ignoreInvalidFields = true)
public class GlobalConfigProperties {

    /**
     * 保存移动位置历史轨迹：true:保留历史数据，false:仅保留最后的位置(默认)
     */
    private Boolean savePositionHistory = Boolean.FALSE;

    /**
     * [可选] 自动点播， 使用固定流地址进行播放时，如果未点播则自动进行点播, 需要rtp.enable=true
     */
    private Boolean autoApplyPlay = Boolean.FALSE;

    /**
     * [可选] 部分设备需要扩展SDP，需要打开此设置
     */
    private Boolean seniorSdp = Boolean.FALSE;

    /**
     * 点播等待超时时间,单位：毫秒
     */
    private Long playTimeout = 18000L;

    /**
     * 等待音视频编码信息再返回， true： 可以根据编码选择合适的播放器，false： 可以更快点播
     */
    private Boolean waitTrack = Boolean.FALSE;

    /**
     * 是否开启接口鉴权
     */
    private Boolean interfaceAuthentication = Boolean.TRUE;

    /**
     * 推流直播是否录制
     */
    private Boolean recordPushLive = Boolean.FALSE;

    /**
     * 是否将日志存储进数据库
     */
    private Boolean logInDatabase = Boolean.TRUE;
}
