package com.yyh.media.entity;

import com.yyh.common.constant.CommonResultConstants;
import com.yyh.media.constants.MediaConstant;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.lang.Nullable;

import java.io.Serializable;

/**
 * @author: yyh
 * @date: 2021-12-21 17:40
 * @description: HookResult
 **/
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ToString
@ApiModel(value = "hook返回响应")
public class HookResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    @ApiModelProperty(value = "返回标记：成功标记=0，失败标记=其它")
    private int code;

    @Getter
    @Setter
    @ApiModelProperty(value = "返回信息")
    private String msg;


    @Getter
    @Setter
    @ApiModelProperty(value = "详细信息")
    private String result;

    @Getter
    @Setter
    @ApiModelProperty(value = "数据")
    private T data;

    public static <T> HookResult<T> success() {
        return restResult(null, MediaConstant.RESTFUL_SUCCESS, MediaConstant.RESTFUL_SUCCESS_MSG);
    }

    public static <T> HookResult<T> success(T data) {
        return restResult(data, MediaConstant.RESTFUL_SUCCESS, MediaConstant.RESTFUL_SUCCESS_MSG);
    }

    private static <T> HookResult<T> restResult(@Nullable T data,int code,String msg) {
        HookResult<T> apiResult = new HookResult<>();
        apiResult.setCode(code);
        apiResult.setData(data);
        apiResult.setMsg(msg);
        return apiResult;
    }
}