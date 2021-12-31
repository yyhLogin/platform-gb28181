package com.yyh.config;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.OkHttp3ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author: yyh
 * @date: 2021-12-10 17:37
 * @description: RestTemplateConfig
 * 启用okHttp3替代默认的http请求
 * 修改默认的字符为UTF-8
 **/
@Configuration
public class RestTemplateConfig {
    @Bean
    public RestTemplate restTemplate(){
        OkHttpClient builder = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(100,5, TimeUnit.MINUTES))
                .build();
///        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
///        factory.setConnectTimeout(15000);
///        factory.setReadTimeout(5000);
        OkHttp3ClientHttpRequestFactory okHttp3ClientHttpRequestFactory = new OkHttp3ClientHttpRequestFactory(builder);
        okHttp3ClientHttpRequestFactory.setConnectTimeout(10000);
        okHttp3ClientHttpRequestFactory.setReadTimeout(5000);
        RestTemplate restTemplate = new RestTemplate(okHttp3ClientHttpRequestFactory);
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        //重新设置StringHttpMessageConverter字符集为UTF-8，解决中文乱码问题
        for (HttpMessageConverter<?> item : messageConverters){
            if (StringHttpMessageConverter.class.equals(item.getClass())){
                ((StringHttpMessageConverter) item).setDefaultCharset(StandardCharsets.UTF_8);
                break;
            }
        }
        return restTemplate;
    }
}
