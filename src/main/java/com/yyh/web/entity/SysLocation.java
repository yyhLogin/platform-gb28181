package com.yyh.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author: yyh
 * @date: 2022-02-15 14:40
 * @description: SysLocation
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="位置信息", description="")
@TableName("sys_location")
public class SysLocation implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID,value = "id")
    private String id;

    /**
     * 标识位
     */
    private Integer alarmFlag;

    /**
     * 设备唯一标识(设备编号或者imei)
     */
    private String imei;

    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 经度
     */
    private Double longitude;

    /**
     * 精度
     */
    private Float accuracy;

    /**
     * 速度 KM/H
     */
    private Float speed;

    /**
     * 定位时间(UTC时间)
     */
    private LocalDateTime time;

    /**
     * 海拔
     */
    private Float altitude;

    /**
     * 方位
     */
    private Float bearing;

    /**
     * 坐标类型
     */
    private String coordinateType;

    /**
     * 当前上传gps时间
     */
    private LocalDateTime uploadTime;
}
