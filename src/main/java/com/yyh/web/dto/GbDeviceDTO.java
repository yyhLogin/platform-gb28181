package com.yyh.web.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author: yyh
 * @date: 2022-03-29 14:58
 * @description: GbDeviceDTO
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="国标设备查询对象", description="")
public class GbDeviceDTO implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 在线
     * 0不在线，1在线
     */
    @ApiModelProperty(value = "在线状态")
    private String online;

    /**
     * 字符集, 支持 utf-8 与 gb2312
     */
    @ApiModelProperty(value = "字符集")
    private String charset ;

    /**
     * 目录订阅周期，0为不订阅
     */
    @ApiModelProperty(value = "目录订阅周期，0为不订阅")
    private Integer subscribeCycleForCatalog ;
}
