package com.yyh.config;

import com.yyh.media.config.ZlmServerConfig;
import com.yyh.media.entity.HookResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

/**
 * @author: yyh
 * @date: 2021-12-21 16:07
 * @description: RestTemplateUtil
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class RestTemplateUtil {

    private final RestTemplate restTemplate;

    public <T> ResponseEntity<T> exchange(String url,
                                          HttpMethod method,
                                          @Nullable HttpEntity<?> requestEntity,
                                          Class<T> responseType,
                                          Object... uriVariables){
        try {
            return restTemplate.exchange(url,method,requestEntity,responseType,uriVariables);
        }catch (Exception ex){
            Pattern p = compile("(\\d+\\.\\d+\\.\\d+\\.\\d+)\\:(\\d+)");
            Matcher m = p.matcher(url);
            while (m.find()){
                log.error("http请求异常:{}->{}",url,ex.getMessage(),ex);
            }
        }
        return null;
    }


}
