package com.yyh.media.callback;

import com.yyh.common.utils.CommonResult;
import com.yyh.media.entity.RecordInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Map;

/**
 * @author: yyh
 * @date: 2022-02-16 15:29
 * @description: RecordInfoDeferredHandle
 **/
@Slf4j
@Component
public class RecordInfoDeferredHandle extends DeferredHandle<RecordInfo, CommonResult<RecordInfo>, DeferredResult<CommonResult<RecordInfo>>> {
    /**
     * 释放handle
     *
     * @param key  key
     * @param id   id
     * @param data data
     */
    @Override
    public void invokeHandle(String key, String id, CommonResult<RecordInfo> data) {
        Map<String, DeferredResult<CommonResult<RecordInfo>>> stringDeferredResultMap = MAP.get(key);
        DeferredResult<CommonResult<RecordInfo>> commonResultDeferredResult = stringDeferredResultMap.get(id);
        commonResultDeferredResult.setResult(data);
        stringDeferredResultMap.remove(id);
        MAP.remove(key);
    }
}
