package com.yyh.config;

import org.hibernate.validator.HibernateValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.validation.Validation;
import javax.validation.Validator;

/**
 * @author: yyh
 * @date: 2022-04-18 16:03
 * @description: ValidateConfig
 **/
@Configuration
public class ValidateConfig {
    @Bean
    public Validator validator(){
        return Validation.byProvider(HibernateValidator.class )
                .configure()
                //开启快速失败
                .failFast( true )
                .buildValidatorFactory()
                .getValidator();
    }
}
