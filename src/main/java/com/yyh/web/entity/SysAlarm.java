package com.yyh.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author: yyh
 * @date: 2022-03-04 15:18
 * @description: SysAlarm
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="SysAlarm对象", description="")
@TableName("sys_alarm")
public class SysAlarm implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "alarm_id", type = IdType.ASSIGN_UUID)
    @ApiModelProperty(value = "主键")
    private String alarmId;

    @ApiModelProperty(value = "描述")
    private String alarmDescription;

    @ApiModelProperty(value = "报警时间")
    private LocalDateTime alarmTime;

    @ApiModelProperty(value = "报警经度")
    private Double longitude;

    @ApiModelProperty(value = "报警纬度")
    private Double latitude;

    @ApiModelProperty(value = "报警方式")
    private String alarmMethod;

    @ApiModelProperty(value = "报警类型")
    private String alarmType;

    @ApiModelProperty(value = "报警级别")
    private String alarmLevel;

    @ApiModelProperty(value = "报警设备")
    private String deviceId;

    @ApiModelProperty(value = "报警入库时间")
    private LocalDateTime uploadTime;

    @ApiModelProperty(value = "报警状态(0-未处理,1-已处理)")
    private Integer alarmStatus;

    @ApiModelProperty(value = "报警处理人")
    private String alarmUser;

    @ApiModelProperty(value = "报警处理时间")
    private LocalDateTime handleTime;

    @ApiModelProperty(value = "报警删除人用户名")
    private String delId;

    @ApiModelProperty(value = "报警删除时间")
    private LocalDateTime delTime;

    @ApiModelProperty(value = "是否删除")
    private Boolean delFlag;


}
