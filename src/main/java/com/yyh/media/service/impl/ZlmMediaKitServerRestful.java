package com.yyh.media.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yyh.config.ConverterType;
import com.yyh.config.RestTemplateUtil;
import com.yyh.media.constants.MediaConstant;
import com.yyh.media.constants.ServerApiConstant;
import com.yyh.media.entity.HookResult;
import com.yyh.media.service.IMediaServerRestful;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * @author: yyh
 * @date: 2021-12-16 17:03
 * @description: ZlMediaKitService
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class ZlmMediaKitServerRestful implements IMediaServerRestful {

    private final RestTemplateUtil restTemplate;

    private final ObjectMapper mapper;



    /**
     * 获取媒体服务器信息
     *
     * @param url 查询接口
     * @return Map<String, Object>
     */
    @Override
    public ArrayList<Map<String, Object>> queryMediaServerInfo(String url) {
        ArrayList<Map<String, Object>> map = null;
        ResponseEntity<String> res = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
        try {
            if (res!=null&&res.getStatusCode()== HttpStatus.OK){
                String body = res.getBody();
                if (StrUtil.isNotBlank(body)){
                    var hookRes = mapper.readValue(body,HookResult.class);
                    int code = hookRes.getCode();
                    Object data = hookRes.getData();
                    if (MediaConstant.RESTFUL_SUCCESS == code){
                        map = mapper.convertValue(data, ConverterType.ArrayList_MAP_TYPE);
                    }
                }
            }
        }catch (Exception ex){
            log.info("获取媒体服务器信息发送url出现异常:{}->{}",url,ex.getMessage(),ex);
        }
        return map;
    }

    /**
     * 更新媒体服务器配置
     *
     * @param url 请求url
     * @param map map
     * @return Boolean
     */
    @Override
    public Boolean updateMediaServerConfig(String url,Map<String, String> map) {
        HttpEntity<Map<String,String>> entity = new HttpEntity<>(map);
        var res = restTemplate.exchange(url, HttpMethod.POST, entity, HookResult.class);
        if (res!=null&&res.getStatusCode()==HttpStatus.OK){
            Object body = res.getBody();
            log.info("");
        }
        return null;
    }

    /**
     * 创建GB28181 RTP接收端口，如果该端口接收数据超时，则会自动被回收(不用调用closeRtpServer接口)
     *
     * @param url       api url
     * @param secret    api操作密钥(配置文件配置)，如果操作ip是127.0.0.1，则不需要此参数
     * @param port      接收端口，0则为随机端口
     * @param enableTcp 启用UDP监听的同时是否监听TCP端口
     * @param streamId  该端口绑定的流ID，该端口只能创建这一个流(而不是根据ssrc创建多个)
     * @return Integer
     */
    @Override
    public Map<String,Object> openRtpServer(String url, String secret, Integer port, String enableTcp, String streamId) {
        Map<String,Object> map = new HashMap<>(4);
        map.put(ServerApiConstant.SECRET,secret);
        map.put("port",port);
        map.put("enable_tcp",enableTcp);
        map.put("stream_id",streamId);
        HttpEntity<Map<String,Object>> entity = new HttpEntity<>(map);
        var res = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
        if (res!=null&&res.getStatusCode()==HttpStatus.OK){
            try {
                return mapper.readValue(res.getBody(), ConverterType.MAP_TYPE);
            } catch (JsonProcessingException e) {
                log.error("JsonProcessingException:{}",e.getMessage(),e);
            }
        }
        return null;
    }
}
