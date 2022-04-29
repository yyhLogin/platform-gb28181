package com.yyh.media.subscribe;



import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: yyh
 * @date: 2022-01-07 14:19
 * @description: AbstractHookSubscribe 订阅hook事件
 **/
public abstract class AbstractHookSubscribe<T> {


    public final Map<String,HookEvent> HOOK_MAP = new ConcurrentHashMap<>();
    private final Map<String,CompletableFuture<?>> FUTURE = new ConcurrentHashMap<>();

    /**
     * 添加订阅
     * @param key key
     * @param event event
     */
    public void subscribe(String key,HookEvent event){
        HOOK_MAP.put(key,event);
    }

    /**
     * 执行订阅
     * @param key key
     * @param map map
     */
    protected abstract void invokeSubscribe(String key,Map<String,Object> map);
}
