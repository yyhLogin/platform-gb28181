package com.yyh.media.controller;

import com.yyh.common.constant.CommonResultConstants;
import com.yyh.common.utils.CommonResult;
import com.yyh.media.service.IPlayService;
import com.yyh.web.entity.GbDevice;
import com.yyh.web.service.IGbDeviceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.Resource;

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

    @ApiOperation("视频预览")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deviceId", value = "设备ID", dataTypeClass = String.class),
            @ApiImplicitParam(name = "channelId", value = "通道ID", dataTypeClass = String.class),
    })
    @GetMapping("/start/{deviceId}/{channelId}")
    public DeferredResult<CommonResult<String>> play(@PathVariable String deviceId,
                                                     @PathVariable String channelId) {
        return playService.play(deviceId,channelId);
    }
}
