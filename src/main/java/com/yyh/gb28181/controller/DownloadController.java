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
 * @date: 2022-03-01 11:18
 * @description: DownloadController
 **/
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("api/download")
@Api(value = "download", tags = "媒体文件下载")
public class DownloadController {

    private final IPlayService playService;

    /**
     * 开始历史媒体下载
     * @param channelId 设备id
     * @param deviceId 通道id
     * @param bTime 开始时间
     * @param eTime 结束时间
     * @param downloadSpeed 下载速度
     * @return DeferredResult<CommonResult<StreamInfo>>
     */
    @ApiOperation("开始历史媒体下载")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deviceId", value = "设备ID", paramType = "path", dataTypeClass = String.class),
            @ApiImplicitParam(name = "channelId", value = "通道ID", paramType = "path", dataTypeClass = String.class),
            @ApiImplicitParam(name = "bTime", value = "开始时间", paramType = "query", dataTypeClass = LocalDateTime.class),
            @ApiImplicitParam(name = "eTime", value = "结束时间", paramType = "query", dataTypeClass = LocalDateTime.class),
            @ApiImplicitParam(name = "downloadSpeed", value = "下载速度,整数", paramType = "query", dataTypeClass = String.class),
    })
    @GetMapping("start/{deviceId}/{channelId}")
    public DeferredResult<CommonResult<StreamInfo>> download(@PathVariable String channelId,
                                                             @PathVariable String deviceId,
                                                             @RequestParam LocalDateTime bTime,
                                                             @RequestParam LocalDateTime eTime,
                                                             @RequestParam String downloadSpeed){
        return playService.download(deviceId,channelId,bTime,eTime,downloadSpeed,null,null);
    }
}
