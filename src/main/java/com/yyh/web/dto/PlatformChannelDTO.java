package com.yyh.web.dto;

import com.yyh.web.vo.PlatformChannelVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * @author: yyh
 * @date: 2022-04-25 11:46
 * @description: PlatformChannelDTO
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="国标通道添加参数", description="")
public class PlatformChannelDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "平台国标id")
    @NotBlank(message = "平台国标id不能为空")
    private String platformId;
    @ApiModelProperty(value = "目录id")
    @NotBlank(message = "目录id不能为空")
    private String catalogId;
    @ApiModelProperty(value = "通道信息")
    @NotNull(message = "通道信息不能为空")
    private List<PlatformChannelVO> channels;
}
