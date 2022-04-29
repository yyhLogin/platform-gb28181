package com.yyh.media.callback;


import com.yyh.common.utils.CommonResult;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: yyh
 * @date: 2022-01-06 09:31
 * @description: DeferredHandle
 **/
public abstract  class DeferredHandle<H, T extends CommonResult<H>, V extends DeferredResult<T>> {

    public final Map<String, Map<String,V>> MAP = new ConcurrentHashMap<>();

    /**
     * 添加handle
     * @param key key
     * @param id id
     * @param data data
     */
    public synchronized void put(String key,String id,V data){
        Map<String, V> result = MAP.get(key);
        if (result == null) {
            result = new ConcurrentHashMap<>(8);
            MAP.put(key, result);
        }
        result.put(id, data);
    }

    /**
     * 判断是否存在key
     * @param key key
     * @return @return
     */
    public synchronized boolean hasKey(String key){
        Map<String, V> result = MAP.get(key);
        return result != null;
    }

    /**
     * 释放handle
     * @param key key
     * @param id id
     * @param data data
     */
    protected abstract void invokeHandle(String key,String id,T data);
}
