package com.yyh.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author: yyh
 * @date: 2021-12-28 17:36
 * @description: Swagger3Config
 **/
@EnableOpenApi
@Configuration
public class Swagger3Config {


    @Bean
    public Docket createRestApi(){
        return new Docket(DocumentationType.OAS_30)
                .pathMapping("/")
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.yyh"))
                .paths(PathSelectors.any())
                .build().apiInfo(new ApiInfoBuilder()
                        .title("GB28181媒体服务器文档")
                        .description("详细信息")
                        .version("1.0")
                        .contact(new Contact(
                                "yyh",
                                "www.baidu.com",
                                "846487248@qq.com"))
                        .build()
                );
    }
}
