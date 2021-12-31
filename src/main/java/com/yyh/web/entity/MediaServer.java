package com.yyh.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author: yyh
 * @date: 2021-12-17 15:20
 * @description: MediaServer
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="媒体服务器", description="")
@TableName("gb_media_server")
public class MediaServer {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "id")
    private String id;

    @ApiModelProperty(value = "服务id")
    private String serverId;

    @ApiModelProperty(value = "主机ip")
    private String ip;

    @ApiModelProperty(value = "hook ip")
    private String hookIp;

    @ApiModelProperty(value = "sdp ip")
    private String sdpIp;

    @ApiModelProperty(value = "stream ip")
    private String streamIp;

    @ApiModelProperty(value = "http port")
    private int httpPort;

    @ApiModelProperty(value = "http ssl port")
    private int httpSslPort;

    @ApiModelProperty(value = "rtmp port")
    private int rtmpPort;

    @ApiModelProperty(value = "rtmp ssl port")
    private int rtmpSslPort;

    @ApiModelProperty(value = "rtp proxy port")
    private int rtpProxyPort;

    @ApiModelProperty(value = "rtsp port")
    private int rtspPort;

    @ApiModelProperty(value = "rtsp ssl port")
    private int rtspSslPort;

    @ApiModelProperty(value = "自动配置")
    private String autoConfig;

    @ApiModelProperty(value = "认证秘钥")
    private String secret;

    @ApiModelProperty(value = "无人播放失效时间")
    private String streamNoneReaderDelayMs;

    @ApiModelProperty(value = "rtp 启用与否")
    private String rtpEnable;

    @ApiModelProperty(value = "rtp 端口: 默认30000-30500")
    private String rtpPortRange;

    @ApiModelProperty(value = "send rtp 端口: 默认30000-30500")
    private String sendRtpPortRange;

    @ApiModelProperty(value = "record assist port 端口")
    private int recordAssistPort;

    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "默认server")
    private String defaultServer;

    @ApiModelProperty(value = "媒体服务的保活时间")
    private Integer hookAliveInterval;

    @TableField(exist = false)
    @ApiModelProperty(value = "最后心跳时间")
    private String lastKeepaliveTime;

    @TableField(exist = false)
    @ApiModelProperty(value = "当前端口")
    private int currentPort;
}
