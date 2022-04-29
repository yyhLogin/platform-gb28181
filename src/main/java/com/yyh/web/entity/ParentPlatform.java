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

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
    private String id;

    @ApiModelProperty(value = "是否启用")
    private Boolean enable;

    @ApiModelProperty(value = "名称")
    @NotBlank(message = "名称不能为空")
    private String name;

    @ApiModelProperty(value = "SIP服务国标编码")
    @NotBlank(message = "SIP服务国标编码不能为空")
    private String serverGbId;

    @ApiModelProperty(value = "SIP服务国标域")
    @NotBlank(message = "SIP服务国标域不能为空")
    private String serverGbDomain;

    @ApiModelProperty(value = "SIP服务IP")
    @NotBlank(message = "SIP服务IP不能为空")
    private String serverIp;

    @ApiModelProperty(value = "SIP服务端口")
    @NotNull(message = "SIP服务端口不能为空")
    private Integer serverPort;

    @ApiModelProperty(value = "设备国标编号")
    @NotBlank(message = "设备国标编号不能为空")
    private String deviceGbId;

    @ApiModelProperty(value = "设备ip")
    @NotBlank(message = "设备ip不能为空")
    private String deviceIp;

    @ApiModelProperty(value = "设备端口")
    @NotNull(message = "设备端口不能为空")
    private Integer devicePort;

    @ApiModelProperty(value = "SIP认证用户名(默认使用设备国标编号)")
    @NotBlank(message = "SIP认证用户名不能为空")
    private String username;

    @ApiModelProperty(value = "SIP认证密码")
    @NotBlank(message = "SIP认证密码不能为空")
    private String password;

    @ApiModelProperty(value = "注册周期(秒)")
    @NotNull(message = "注册周期不能为空")
    private Integer expires;

    @ApiModelProperty(value = "心跳周期(秒)")
    @NotNull(message = "心跳周期不能为空")
    private Integer keepTimeout;

    @ApiModelProperty(value = "传输协议,UDP/TCP")
    @NotBlank(message = "传输协议不能为空")
    private String transport;

    @ApiModelProperty(value = "字符集")
    @NotBlank(message = "字符集不能为空")
    private String characterSet;

    @ApiModelProperty(value = "允许云台控制")
    private Boolean ptz;

    /**
     * TODO 预留, 暂不实现
     */
    @ApiModelProperty(value = "RTCP流保活")
    private Boolean rtcp;

    @ApiModelProperty(value = "在线状态")
    private Boolean status;

    /**
     * 共享所有的直播流
     */
    @ApiModelProperty(value = "共享所有的直播流")
    private Boolean shareAllLiveStream;

    /**
     * 默认目录Id,自动添加的通道多放在这个目录下
     */
    @ApiModelProperty(value = "默认目录Id,自动添加的通道多放在这个目录下")
    private String catalogId;

    /**
     * 点播未推流的设备时是否使用redis通知拉起
     */
    @ApiModelProperty(value = "点播未推流的设备时是否使用redis通知拉起")
    private boolean startOfflinePush;

    /**
     * 目录分组-每次向上级发送通道信息时单个包携带的通道数量，取值1,2,4,8
     */
    @ApiModelProperty(value = "目录分组-每次向上级发送通道信息时单个包携带的通道数量，取值1,2,4,8")
    private int catalogGroup;

    /**
     * 行政区划
     */
    @ApiModelProperty(value = "行政区划")
    private String administrativeDivision;

    @ApiModelProperty(value = "通道数量")
    @TableField(exist = false)
    private int channelCount;

    /**
     * 已被订阅目录信息
     */
    @ApiModelProperty(value = "已被订阅目录信息")
    @TableField(exist = false)
    private boolean catalogSubscribe;

    /**
     * 已被订阅报警信息
     */
    @ApiModelProperty(value = "已被订阅报警信息")
    @TableField(exist = false)
    private boolean alarmSubscribe;

    /**
     * 已被订阅移动位置信息
     */
    @ApiModelProperty(value = "已被订阅移动位置信息")
    @TableField(exist = false)
    private boolean mobilePositionSubscribe;


}
