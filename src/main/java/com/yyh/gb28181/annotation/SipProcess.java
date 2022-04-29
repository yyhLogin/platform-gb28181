package com.yyh.gb28181.annotation;

import java.lang.annotation.*;

/**
 * @author: yyh
 * @date: 2021-11-24 15:20
 * @description: SipRequestProcess
 **/
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SipProcess {

    /**
     * sip请求处理方法
     * @return String
     */
    String method() default "";
}
