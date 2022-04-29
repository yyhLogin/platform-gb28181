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
import java.io.Serializable;

/**
 * @author: yyh
 * @date: 2022-04-24 17:31
 * @description: PlatformChannel
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value="国标级联通道", description="")
@TableName("gb_platform_channel")
public class PlatformChannel implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "id")
    private String id;

    @ApiModelProperty(value = "设备通道id")
    @NotBlank(message = "设备通道id不能为空")
    private String deviceChannelId;

    @ApiModelProperty(value = "上级平台id")
    @NotBlank(message = "上级平台id不能为空")
    private String platformId;

    @ApiModelProperty(value = "目录id")
    private String catalogId;
}
