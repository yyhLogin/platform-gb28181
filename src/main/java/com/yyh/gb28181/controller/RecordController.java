package com.yyh.gb28181.controller;

import com.yyh.common.utils.CommonResult;
import com.yyh.gb28181.command.ISipCommander;
import com.yyh.media.callback.DeferredResultHandle;
import com.yyh.media.callback.RecordInfoDeferredHandle;
import com.yyh.media.entity.RecordInfo;
import com.yyh.gb28181.service.IPlayService;
import com.yyh.web.entity.GbDevice;
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
 * @date: 2022-02-16 11:02
 * @description: RecordController
 **/
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("api/record")
@Api(value = "record", tags = "录像查询")
public class RecordController {

    private final IPlayService playService;

    private final ISipCommander sipCommander;

    private final RecordInfoDeferredHandle recordInfoDeferredHandle;

    /**
     * 录像文件查询
     * @param deviceId 设备id
     * @param channelId 通道id
     * @param bTime 开始时间
     * @param eTime 结束时间
     * @return DeferredResult<CommonResult<RecordInfo>>
     */
    @ApiOperation("录像查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deviceId", value = "设备ID", dataTypeClass = String.class),
            @ApiImplicitParam(name = "channelId", value = "通道ID", dataTypeClass = String.class),
            @ApiImplicitParam(name = "bTime", value = "开始时间", dataTypeClass = LocalDateTime.class),
            @ApiImplicitParam(name = "eTime", value = "结束时间", dataTypeClass = LocalDateTime.class),
    })
    @GetMapping("/query/{deviceId}/{channelId}")
    public DeferredResult<CommonResult<RecordInfo>> query(@PathVariable String deviceId,
                                                          @PathVariable String channelId,
                                                          @RequestParam LocalDateTime bTime,
                                                          @RequestParam LocalDateTime eTime){
        // 指定超时时间 20秒
        DeferredResult<CommonResult<RecordInfo>> result = new DeferredResult<>(20*1000L);
        GbDevice gbDevice = playService.queryGbDevice(deviceId, channelId);
        int sn  =  (int)((Math.random()*9+1)*100000);
        String key = DeferredResultHandle.CALLBACK_CMD_RECORD_INFO + deviceId + sn;
        sipCommander.recordInfoQuery(gbDevice,deviceId,channelId,bTime,eTime,sn,(eventResult)->{
            log.error("查询设备录像文件出现异常:{}->{}",deviceId,channelId);
            CommonResult<RecordInfo> commonResult = CommonResult.fail("查询设备录像文件出现异常");
            recordInfoDeferredHandle.invokeHandle(key,String.valueOf(sn),commonResult);
        });
        recordInfoDeferredHandle.put(key,String.valueOf(sn),result);
        result.onTimeout(()->{
            CommonResult<RecordInfo> commonResult = CommonResult.fail("获取视频文件超时");
            recordInfoDeferredHandle.invokeHandle(key,String.valueOf(sn),commonResult);
        });
        return result;
    }
}
