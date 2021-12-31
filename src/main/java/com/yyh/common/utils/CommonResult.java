package com.yyh.common.utils;

import com.yyh.common.constant.CommonResultConstants;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author: yyh
 * @date: 2021-12-03 16:22
 * @description: CommonResult
 **/
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ToString
@ApiModel(value = "响应信息主体")
public class CommonResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    @Getter
    @Setter
    @ApiModelProperty(value = "返回标记：成功标记=200，失败标记=0")
    private int code;

    @Getter
    @Setter
    @ApiModelProperty(value = "返回信息")
    private String msg;


    @Getter
    @Setter
    @ApiModelProperty(value = "数据")
    private T data;

    public static <T> CommonResult<T> success() {
        return restResult(null, CommonResultConstants.SUCCESS, null);
    }

    public static <T> CommonResult<T> success(T data) {
        return restResult(data, CommonResultConstants.SUCCESS, null);
    }

    public static <T> CommonResult<T> success(T data, String msg) {
        return restResult(data, CommonResultConstants.SUCCESS, msg);
    }
    public static <T> CommonResult<T> success(int code,T data, String msg) {
        return restResult(data, code, msg);
    }

    public static <T> CommonResult<T> fail() {
        return restResult(null, CommonResultConstants.FAIL, null);
    }

    public static <T> CommonResult<T> fail(String msg) {
        return restResult(null, CommonResultConstants.FAIL, msg);
    }

    public static <T> CommonResult<T> fail(T data) {
        return restResult(data, CommonResultConstants.FAIL, null);
    }

    public static <T> CommonResult<T> fail(T data, String msg) {
        return restResult(data, CommonResultConstants.FAIL, msg);
    }
    public static <T> CommonResult<T> fail(Integer code,T data, String msg) {
        return restResult(data, code, msg);
    }

    private static <T> CommonResult<T> restResult(T data, int code, String msg) {
        CommonResult<T> apiResult = new CommonResult<>();
        apiResult.setCode(code);
        apiResult.setData(data);
        apiResult.setMsg(msg);
        return apiResult;
    }
}
