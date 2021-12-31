package com.yyh.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * @author: yyh
 * @date: 2021-12-03 14:53
 * @description: SysDevice
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="系统设备对象", description="")
@TableName("sys_device")
public class SysDevice {

    private static final long serialVersionUID = 1L;

    @TableId(value = "device_id", type = IdType.ASSIGN_UUID)
    @ApiModelProperty(value = "设备id")
    private String deviceId;

    @ApiModelProperty(value = "设备唯一编码(可能是imei等)")
    private String deviceCode;

    @ApiModelProperty(value = "系统使用的设备唯一标识")
    private String deviceSignid;

    @ApiModelProperty(value = "设备类型")
    private Integer deviceType;

    @ApiModelProperty(value = "设备所属组织")
    @NotBlank(message = "设备组织不能为空")
    private String deviceOrg;

    @ApiModelProperty(value = "设备所属组织名称")
    private String deviceOrgName;

    @ApiModelProperty(value = "设备注册时间")
    private LocalDateTime deviceRegTime;

    @ApiModelProperty(value = "设备参数")
    private String deviceOption;

    @ApiModelProperty(value = "设备状态")
    private Integer deviceStatus;

    @ApiModelProperty(value = "备注")
    private String deviceMemo;

    @ApiModelProperty(value = "设备删除状态")
    private Boolean delFlag;

    @ApiModelProperty(value = "删除时间")
    private LocalDateTime deviceDelTime;

    @ApiModelProperty(value = "删除人员")
    private String deviceDelUsername;
}
