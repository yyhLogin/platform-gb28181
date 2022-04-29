package com.yyh.media.callback;

import org.springframework.web.context.request.async.DeferredResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: yyh
 * @date: 2022-04-11 15:12
 * @description: AbstractCmdHandle
 **/
public class AbstractCmdHandle<T> implements CmdHandle{

    private final Map<String,Map<String, DeferredResult<T>>> DEFERRED_MAP = new ConcurrentHashMap<>();

    public void put(String key,String sn, DeferredResult<T> deferredResult){
        boolean b = DEFERRED_MAP.containsKey(key);
        if (b){
            Map<String, DeferredResult<T>> stringDeferredResultMap = DEFERRED_MAP.get(key);
            stringDeferredResultMap.put(sn,deferredResult);
        }else {
            Map<String, DeferredResult<T>> map = new ConcurrentHashMap<>();
            map.put(sn,deferredResult);
            DEFERRED_MAP.put(key,map);
        }
    }

    /**
     * 处理单个handle
     *
     * @param key    key
     * @param sn     sn
     * @param object object
     */
    @Override
    public void handleResult(String key, String sn, Object object) {

    }

    /**
     * 处理所有的handle
     *
     * @param key    key
     * @param object object
     */
    @Override
    public void handleAllResult(String key, Object object) {

    }

    /**
     * 当前handle是否存在
     *
     * @param key key
     * @param sn  sn
     * @return boolean
     */
    @Override
    public boolean isExist(String key, String sn) {
        return false;
    }
}
