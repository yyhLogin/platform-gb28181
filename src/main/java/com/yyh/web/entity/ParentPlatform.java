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

import java.io.Serializable;

/**
 * @author: yyh
 * @date: 2021-12-09 16:37
 * @description: ParentPlatform
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="国标上级平台", description="")
@TableName("gb_parent_platform")
public class ParentPlatform implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "是否启用")
    private String enable;

    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "SIP服务国标编码")
    private String serverGbId;

    @ApiModelProperty(value = "SIP服务国标域")
    private String serverGbDomain;

    @ApiModelProperty(value = "SIP服务IP")
    private String serverIp;

    @ApiModelProperty(value = "SIP服务端口")
    private int serverPort;

    @ApiModelProperty(value = "设备国标编号")
    private String deviceGbId;

    @ApiModelProperty(value = "设备ip")
    private String deviceIp;

    @ApiModelProperty(value = "设备端口")
    private String devicePort;

    @ApiModelProperty(value = "SIP认证用户名(默认使用设备国标编号)")
    private String username;

    @ApiModelProperty(value = "SIP认证密码")
    private String password;

    @ApiModelProperty(value = "注册周期 (秒)")
    private String expires;

    @ApiModelProperty(value = "心跳周期(秒)")
    private String keepTimeout;

    @ApiModelProperty(value = "传输协议,UDP/TCP")
    private String transport;

    @ApiModelProperty(value = "字符集")
    private String characterSet;

    @ApiModelProperty(value = "允许云台控制")
    private String ptz;

    /**
     * TODO 预留, 暂不实现
     */
    @ApiModelProperty(value = "RTCP流保活")
    private String rtcp;

    @ApiModelProperty(value = "在线状态")
    private String status;

    @ApiModelProperty(value = "通道数量")
    @TableField(exist = false)
    private int channelCount;
}
