package com.yyh.media.subscribe;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: yyh
 * @date: 2021-12-21 18:22
 * @description: ZLMHttpHookSubscribe
 **/
@Slf4j
@Component
public class ZlmHttpHookSubscribe{

    private Map<HookType, Map<Map<String,Object>, HookEvent>> allSubscribes;

    @PostConstruct
    public void init(){
        allSubscribes = new ConcurrentHashMap<>(16);
    }

    /**
     * 添加订阅
     * @param type 订阅类
     * @param response 参数
     * @param event 回调事件
     */
    public void addSubscribe(HookType type,Map<String,Object> response,HookEvent event){
        Map<Map<String, Object>, HookEvent> mapHookEventMap = allSubscribes.computeIfAbsent(type, k -> new HashMap<>(8));
        mapHookEventMap.put(response, event);
    }

    public HookEvent getSubscribe(HookType type, Map<String,Object> hookResponse) {
        HookEvent event= null;
        Map<Map<String,Object>, HookEvent> eventMap = allSubscribes.get(type);
        if (eventMap == null) {
            return null;
        }
        for (Map<String,Object> map : eventMap.keySet()) {
            Boolean result = null;
            for (String s : map.keySet()) {
                if (result == null) {
                    result = MapUtil.getStr(map,s).equals(MapUtil.getStr(hookResponse,s));
                }else {
                    if (MapUtil.getStr(map,s) == null) {
                        continue;
                    }
                    result = result && MapUtil.getStr(map,s).equals(MapUtil.getStr(hookResponse,s));
                }
            }
            if (null != result && result) {
                event = eventMap.get(map);
            }
        }
        return event;
    }

    /**
     * 获取某个类型的所有的订阅
     * @param type type
     * @return List<HookEvent>
     */
    public List<HookEvent> getSubscribes(HookType type) {
        // ZLMHttpHookSubscribe.Event event= null;
        Map<Map<String,Object>, HookEvent> eventMap = allSubscribes.get(type);
        if (eventMap == null) {
            return null;
        }
        List<HookEvent> result = new ArrayList<>();
        for (Map<String,Object> key : eventMap.keySet()) {
            result.add(eventMap.get(key));
        }
        return result;
    }
}
