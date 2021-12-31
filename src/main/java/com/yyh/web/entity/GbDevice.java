package com.yyh.web.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.annotations.Update;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author: yyh
 * @date: 2021-12-06 14:04
 * @description: GbDevice
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="系统设备对象", description="")
@TableName("gb_device")
public class GbDevice implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 设备Id
     */
    @TableId(value = "gb_id", type = IdType.INPUT)
    @ApiModelProperty(value = "国标id")
    private String gbId;

    /**
     * 设备名
     */
    @ApiModelProperty(value = "设备名")
    private String name;

    /**
     * 生产厂商
     */
    @ApiModelProperty(value = "生产厂商")
    private String manufacturer;

    /**
     * 型号
     */
    @ApiModelProperty(value = "型号")
    private String model;

    /**
     * 固件版本
     */
    @ApiModelProperty(value = "固件版本")
    private String firmware;

    /**
     * 传输协议
     * UDP/TCP
     */
    @ApiModelProperty(value = "传输协议UDP/TCP")
    private String transport;

    /**
     * 数据流传输模式
     * UDP:udp传输
     * TCP-ACTIVE：tcp主动模式
     * TCP-PASSIVE：tcp被动模式
     */
    @ApiModelProperty(value = "数据流传输模式")
    private String streamMode;

    /**
     * wan地址_ip
     */
    @ApiModelProperty(value = "wan地址_ip")
    private String  ip;

    /**
     * wan地址_port
     */
    @ApiModelProperty(value = "wan地址_port")
    private Integer port;

    /**
     * wan地址
     */
    @ApiModelProperty(value = "wan地址")
    private String  hostAddress;

    /**
     * 在线
     * 0不在线，1在线
     */
    @ApiModelProperty(value = "在线状态")
    private String online;


    /**
     * 注册时间
     */
    @ApiModelProperty(value = "注册时间")
    private LocalDateTime registerTime;


    /**
     * 心跳时间
     */
    @ApiModelProperty(value = "心跳时间")
    private LocalDateTime keepaliveTime;


    /**
     * 注册有效期
     */
    @ApiModelProperty(value = "注册有效期")
    private Integer expires;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;


    /**
     * 字符集, 支持 utf-8 与 gb2312
     */
    @ApiModelProperty(value = "字符集")
    private String charset ;

    /**
     * 目录订阅周期，0为不订阅
     */
    @ApiModelProperty(value = "目录订阅周期，0为不订阅")
    private int subscribeCycleForCatalog ;

//    /**
//     * 媒体服务器id
//     */
//    private String mediaServerId;
//
//    /**
//     * 是否第一次注册
//     */
//    private Boolean firstRegister;
}
