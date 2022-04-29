package com.yyh.common.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author: yyh
 * @date: 2022-01-11 15:01
 * @description: PlatformException
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class PlatformException extends RuntimeException implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer code;

    private Object data;


    public PlatformException(Integer code,String msg, Object object) {
        super(msg);
        this.code = code;
        this.data = object;
    }

    public PlatformException(Integer code,String msg) {
        super(msg);
        this.code = code;
    }

}
