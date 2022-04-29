package com.yyh.web.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author: yyh
 * @date: 2022-04-25 09:37
 * @description: PlatformChannelVO
 **/
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="通道信息", description="")
public class PlatformChannelVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    private String id;

    /**
     * 通道id
     */
    private String channelId;


    /**
     * 设备id
     */
    private String deviceId;

    /**
     * 通道名
     */
    private String name;

    /**
     * 生产厂商
     */
    private String manufacturer;

    /**
     * wan地址
     */
    private String  hostAddress;

    /**
     * 子节点数
     */
    private Integer  subCount;

    /**
     * 平台Id
     */
    private String  platformId;

    /**
     * 目录Id
     */
    private String  catalogId;
}
