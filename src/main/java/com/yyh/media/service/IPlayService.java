package com.yyh.media.service;

import com.yyh.common.utils.CommonResult;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * @author: yyh
 * @date: 2021-12-23 11:00
 * @description: IPlayService
 **/
public interface IPlayService {

    /**
     * 实时预览视频
     * @param deviceId 设备id
     * @param channelId 通道id
     * @return DeferredResult<CommonResult<String>>
     */
    DeferredResult<CommonResult<String>> play(String deviceId, String channelId);
}
