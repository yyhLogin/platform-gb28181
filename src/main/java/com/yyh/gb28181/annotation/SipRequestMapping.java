package com.yyh.gb28181.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author: yyh
 * @date: 2021-11-24 14:38
 * @description: SipRequestMapping sip请求注解
 **/
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface SipRequestMapping {
}
