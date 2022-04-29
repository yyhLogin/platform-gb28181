package com.yyh.gb28181.controller;

import com.yyh.common.utils.CommonResult;
import com.yyh.media.entity.StreamInfo;
import com.yyh.gb28181.service.IPlayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.time.LocalDateTime;

/**
 * @author: yyh
 * @date: 2022-02-18 10:50
 * @description: PlaybackController
 **/
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("api/playback")
@Api(value = "playback", tags = "录像回放")
public class PlaybackController {


    private final IPlayService playService;

    /**
     * 录像回放
     * @param channelId 设备id
     * @param deviceId 通道id
     * @param bTime 开始时间
     * @param eTime 结束时间
     * @return DeferredResult<CommonResult<StreamInfo>>
     */
    @ApiOperation("录像回放")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deviceId", value = "设备ID", dataTypeClass = String.class),
            @ApiImplicitParam(name = "channelId", value = "通道ID", dataTypeClass = String.class),
            @ApiImplicitParam(name = "bTime", value = "开始时间", dataTypeClass = LocalDateTime.class),
            @ApiImplicitParam(name = "eTime", value = "结束时间", dataTypeClass = LocalDateTime.class),
    })
    @GetMapping("/start/{deviceId}/{channelId}")
    public DeferredResult<CommonResult<StreamInfo>> playback(@PathVariable String channelId,
                                                             @PathVariable String deviceId,
                                                             @RequestParam LocalDateTime bTime,
                                                             @RequestParam LocalDateTime eTime){
        return playService.playback(deviceId,channelId,bTime,eTime,null,null);
    }


    /**
     * 停止录像回放
     * @param channelId 设备id
     * @param deviceId 通道id
     * @return DeferredResult<CommonResult<StreamInfo>>
     */
    @ApiOperation("停止录像回放")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deviceId", value = "设备ID", dataTypeClass = String.class),
            @ApiImplicitParam(name = "channelId", value = "通道ID", dataTypeClass = String.class),
    })
    @GetMapping("/stop/{deviceId}/{channelId}")
    public DeferredResult<CommonResult<String>> stopPlayback(@PathVariable String channelId,
                                                             @PathVariable String deviceId){
        return playService.stopPlayback(deviceId,channelId);
    }

}
