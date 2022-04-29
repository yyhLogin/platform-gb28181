package com.yyh.media.callback;

import com.yyh.common.utils.CommonResult;
import com.yyh.media.entity.StreamInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Map;

/**
 * @author: yyh
 * @date: 2022-01-06 09:42
 * @description: CallbackPlayDeferredHandle
 **/
@Slf4j
@Component
public class CallbackPlayDeferredHandle extends DeferredHandle<StreamInfo,CommonResult<StreamInfo>,DeferredResult<CommonResult<StreamInfo>>> {

    /**
     * 释放handle
     *
     * @param key  key
     * @param id   id
     * @param data data
     */
    @Override
    public void invokeHandle(String key, String id, CommonResult<StreamInfo> data) {
        Map<String, DeferredResult<CommonResult<StreamInfo>>> map = MAP.get(key);
        if (map==null){
            return;
        }
        log.info("MAP size ->{}",MAP.size());
        for (String itemKey : map.keySet()){
            DeferredResult<CommonResult<StreamInfo>> result = map.get(itemKey);
            result.setResult(data);
            map.remove(itemKey);
        }
        MAP.remove(key);
        log.info("MAP size ->{}",MAP.size());
    }
}
