package com.yyh.gb28181.session;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: yyh
 * @date: 2022-01-18 15:11
 * @description: VideoStreamSessionManager
 * 媒体流会话管理
 **/
@Component
public class VideoStreamSessionManager {


    /**
     * session集合
     */
    private final Map<String,SsrcTransaction> map = new ConcurrentHashMap<>();

    public synchronized boolean put(String streamId,SsrcTransaction ssrcTransaction){
        if (map.containsKey(streamId)){
            return false;
        }
        map.put(streamId, ssrcTransaction);
        return true;
    }

    public SsrcTransaction get(String streamId){
        return map.get(streamId);
    }

    public synchronized void remove(String streamId){
        map.remove(streamId);
    }
}

