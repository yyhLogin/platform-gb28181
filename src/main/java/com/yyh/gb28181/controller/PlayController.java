package com.yyh.gb28181.controller;

import com.yyh.common.exception.PlatformException;
import com.yyh.common.utils.CommonResult;
import com.yyh.media.entity.StreamInfo;
import com.yyh.media.service.IMediaServerRestful;
import com.yyh.gb28181.service.IPlayService;
import com.yyh.web.service.IGbDeviceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;


/**
 * @author: yyh
 * @date: 2021-12-23 10:01
 * @description: PlayController
 **/
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("api/play")
@Api(value = "play", tags = "实时点播设备")
public class PlayController {

    private final IGbDeviceService deviceService;
    private final IPlayService playService;
    private final IMediaServerRestful restful;


    /**
     * 视频预览
     * @param deviceId 设备id
     * @param channelId 通道id
     * @return DeferredResult<CommonResult<StreamInfo>>
     */
    @ApiOperation("视频预览")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deviceId", value = "设备ID", dataTypeClass = String.class),
            @ApiImplicitParam(name = "channelId", value = "通道ID", dataTypeClass = String.class),
    })
    @GetMapping("/start/{deviceId}/{channelId}")
    public DeferredResult<CommonResult<StreamInfo>> play(@PathVariable String deviceId,
                                                         @PathVariable String channelId) {
        return playService.play(deviceId,channelId,null,null);
    }

    /**
     * 停止预览
     * @param deviceId 设备id
     * @param channelId 通道id
     * @return CommonResult<Boolean>
     */
    @ApiOperation("停止预览")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deviceId", value = "设备ID", dataTypeClass = String.class),
            @ApiImplicitParam(name = "channelId", value = "通道ID", dataTypeClass = String.class),
    })
    @GetMapping("/stop/{deviceId}/{channelId}")
    public DeferredResult<CommonResult<String>> stop(@PathVariable String deviceId,
                                                      @PathVariable String channelId){
        return playService.stop(deviceId,channelId);
    }

    @GetMapping("test/{deviceId}")
    public DeferredResult<CommonResult<String>> play(@PathVariable String deviceId){
        DeferredResult<CommonResult<String>> result = new DeferredResult<>(10000L);
        result.onTimeout(()->{
            log.info("请求超时了");
        });
        if ("1".equals(deviceId)){
            throw new PlatformException(-1,"请求出错了");
        }
        return result;
    }
}
