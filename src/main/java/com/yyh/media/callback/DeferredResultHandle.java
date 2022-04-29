package com.yyh.media.callback;

import com.yyh.common.utils.CommonResult;
import com.yyh.media.entity.StreamInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: yyh
 * @date: 2022-01-05 18:04
 * @description: DeferredResultHandle
 **/
@Component
public class DeferredResultHandle {

    public static final String CALLBACK_CMD_PLAY = "CALLBACK_PLAY";
    public static final String CALLBACK_CMD_PLAYBACK = "CALLBACK_PLAYBACK";
    public static final String CALLBACK_CMD_DOWNLOAD = "CALLBACK_DOWNLOAD";
    public static final String CALLBACK_CMD_RECORD_INFO = "CALLBACK_RECORD_INFO";
    public static final String CALLBACK_CMD_STOP = "CALLBACK_PLAY_STOP";
    public static final String CALLBACK_CMD_ALARM = "CALLBACK_ALARM";
    public static final String CALLBACK_CMD_DEVICE_STATUS = "CALLBACK_DEVICE_STATUS";
    public static final String CALLBACK_CMD_CONFIG_DOWNLOAD = "CALLBACK_CONFIG_DOWNLOAD";
    public static final String CALLBACK_CMD_DEVICE_CONFIG = "CALLBACK_CMD_DEVICE_CONFIG";
    public static final String CALLBACK_CMD_DEVICE_CONTROL = "CALLBACK_CMD_DEVICE_CONTROL";
    public static final String CALLBACK_CMD_CATALOG = "CALLBACK_CMD_CATALOG";
    public static final String CALLBACK_CMD_PRESET_QUERY = "CALLBACK_CMD_PRESET_QUERY";

    private final Map<String, Map<String, DeferredResult>> map = new ConcurrentHashMap<>();


    public void put(String key, String id, DeferredResult result) {
        Map<String, DeferredResult> deferredResultMap = map.get(key);
        if (deferredResultMap == null) {
            deferredResultMap = new ConcurrentHashMap<>();
            map.put(key, deferredResultMap);
        }
        deferredResultMap.put(id, result);
    }

    public DeferredResult get(String key, String id) {
        Map<String, DeferredResult> deferredResultMap = map.get(key);
        if (deferredResultMap == null) {
            return null;
        }
        return deferredResultMap.get(id);
    }

    public boolean exist(String key, String id){
        if (key == null) {
            return false;
        }
        Map<String, DeferredResult> deferredResultMap = map.get(key);
        if (id == null) {
            return deferredResultMap != null;
        }else {
            return deferredResultMap != null && deferredResultMap.get(id) != null;
        }
    }

    /**
     * 释放单个请求
     * @param msg
     */
    public void invokeResult(RequestMessage msg) {
        Map<String, DeferredResult> deferredResultMap = map.get(msg.getKey());
        if (deferredResultMap == null) {
            return;
        }
        DeferredResult result = deferredResultMap.get(msg.getId());
        if (result == null) {
            return;
        }
        result.setResult(msg);
        deferredResultMap.remove(msg.getId());
        if (deferredResultMap.size() == 0) {
            map.remove(msg.getKey());
        }
    }

    /**
     * 释放单个请求
     * @param key key
     * @param id id
     * @param data data
     */
    public void invokeResult(String key,String id, CommonResult data){
        Map<String, DeferredResult> deferredResultMap = map.get(key);
        if (deferredResultMap == null) {
            return;
        }
        DeferredResult result = deferredResultMap.get(id);
        if (result == null) {
            return;
        }
        result.setResult(data);
        deferredResultMap.remove(id);
        if (deferredResultMap.size() == 0) {
            map.remove(key);
        }
    }

    /**
     * 释放所有请求
     * @param key key
     * @param data data
     */
    public void invokeAllResult(String key, CommonResult data){
        Map<String, DeferredResult> deferredResultMap = map.get(key);
        if (deferredResultMap == null) {
            return;
        }
        Set<String> ids = deferredResultMap.keySet();
        for (String id : ids) {
            DeferredResult result = deferredResultMap.get(id);
            if (result == null) {
                return;
            }
            result.setResult(data);
        }
        map.remove(key);
    }
}
