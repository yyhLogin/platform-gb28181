package com.yyh.gb28181.controller;

import com.yyh.common.constant.CommonResultConstants;
import com.yyh.common.exception.PlatformException;
import com.yyh.common.utils.CommonResult;
import com.yyh.gb28181.command.ISipCommander;
import com.yyh.web.entity.DeviceChannel;
import com.yyh.media.callback.DeferredResultHandle;
import com.yyh.web.entity.GbDevice;
import com.yyh.web.service.IGbDeviceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * @author: yyh
 * @date: 2022-03-08 14:06
 * @description: DeviceQueryController
 **/
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/device/query")
@Api(tags = "国标设备查询", value = "国标设备查询")
public class DeviceQueryController {

    private final IGbDeviceService gbDeviceService;

    private final ISipCommander sipCommander;

    private final DeferredResultHandle deferredResultHandler;

    /**
     * 设备报警查询
     * @param deviceId 设备id
     * @param startPriority 开始的等级
     * @param endPriority 结束的等级
     * @param alarmMethod 报警方式
     * @param alarmType 报警类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return DeferredResult<CommonResult<String>>
     */
    @GetMapping("/alarm/{deviceId}")
    @ApiOperation("设备报警查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deviceId", value = "设备id", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "startPriority", value = "报警起始级别", dataTypeClass = String.class),
            @ApiImplicitParam(name = "endPriority", value = "报警终止级别", dataTypeClass = String.class),
            @ApiImplicitParam(name = "alarmMethod", value = "报警方式条件", dataTypeClass = String.class),
            @ApiImplicitParam(name = "alarmType", value = "报警类型", dataTypeClass = String.class),
            @ApiImplicitParam(name = "startTime", value = "报警发生起始时间", dataTypeClass = LocalDateTime.class),
            @ApiImplicitParam(name = "endTime", value = "报警发生终止时间", dataTypeClass = LocalDateTime.class),
    })
    public DeferredResult<CommonResult<String>> alarmApi(@PathVariable String deviceId,
                                                         @RequestParam(required = false) String startPriority,
                                                         @RequestParam(required = false) String endPriority,
                                                         @RequestParam(required = false) String alarmMethod,
                                                         @RequestParam(required = false) String alarmType,
                                                         @RequestParam(required = false) LocalDateTime startTime,
                                                         @RequestParam(required = false) LocalDateTime endTime){
        //指定超时时间3S
        DeferredResult<CommonResult<String>> result = new DeferredResult<>(1000*3L);
        GbDevice byId = gbDeviceService.getById(deviceId);
        if (byId==null){
            CommonResult<String> commonResult = CommonResult.fail("设备不存在");
            result.setResult(commonResult);
            return result;
        }
        String key = DeferredResultHandle.CALLBACK_CMD_ALARM + deviceId;
        int sn  =  (int)((Math.random()*9+1)*100000);
        sipCommander.alarmInfoQuery(byId,startPriority,endPriority,alarmMethod,alarmType,startTime,endTime,sn,(event)->{
            CommonResult<String> commonResult = CommonResult.fail(String.format("设备报警查询失败，错误码： %s, %s",event.statusCode, event.msg));
            result.setResult(commonResult);
            deferredResultHandler.invokeResult(key,String.valueOf(sn),commonResult);
        });
        result.onTimeout(()->{
            CommonResult<String> commonResult = CommonResult.fail("请求超时");
            result.setResult(commonResult);
            deferredResultHandler.invokeResult(key,String.valueOf(sn),commonResult);
        });
        deferredResultHandler.put(key,String.valueOf(sn),result);
        return result;
    }

    /**
     * 设备状态查询请求API接口
     *
     * @param deviceId 设备id
     */
    @ApiOperation("设备状态查询")
    @ApiImplicitParam(name = "deviceId", value = "设备id", required = true, dataTypeClass = String.class)
    @GetMapping("/devices/{deviceId}/status")
    public DeferredResult<CommonResult<String>> deviceStatusApi(@PathVariable String deviceId){
        //指定超时时间3S
        DeferredResult<CommonResult<String>> result = new DeferredResult<>(1000*3L);
        GbDevice byId = gbDeviceService.getById(deviceId);
        if (byId==null){
            CommonResult<String> commonResult = CommonResult.fail("设备不存在");
            result.setResult(commonResult);
            return result;
        }
        String key = DeferredResultHandle.CALLBACK_CMD_DEVICE_STATUS + deviceId;
        String sn  =  String.valueOf((int)((Math.random()*9+1)*100000));
        sipCommander.deviceStatusQuery(byId,sn,(event)->{
            CommonResult<String> commonResult = CommonResult.fail(
                    String.format("获取设备状态失败，错误码： %s, %s",
                            event.statusCode,
                            event.msg));
            result.setResult(commonResult);
            deferredResultHandler.invokeResult(key,sn,commonResult);
        });
        result.onTimeout(()->{
            CommonResult<String> commonResult = CommonResult.fail("请求超时");
            result.setResult(commonResult);
            deferredResultHandler.invokeResult(key,sn,commonResult);
        });
        deferredResultHandler.put(key,sn,result);
        return result;
    }

    /**
     * 更新设备通道信息
     * @param deviceId 设备编号
     * @return DeferredResult<CommonResult<String>>
     */
    @GetMapping("/catalog/{deviceId}")
    @ApiOperation(value = "更新设备通道信息", notes = "需要延迟等待",httpMethod = "GET")
    @ApiImplicitParam(name = "deviceId", value = "设备id", required = true, dataTypeClass = String.class,paramType = "path")
    public DeferredResult<CommonResult<List<DeviceChannel>>> updateDeviceChannelApi(@PathVariable String deviceId){
        GbDevice device = gbDeviceService.getById(deviceId);
        if (device==null){
            throw new PlatformException(CommonResultConstants.FAIL,"设备不存在:"+deviceId);
        }
        DeferredResult<CommonResult<List<DeviceChannel>>> result = new DeferredResult<>(10*1000L);
        String key = DeferredResultHandle.CALLBACK_CMD_CATALOG + deviceId;
        String uuid = UUID.randomUUID().toString();
        result.onTimeout(()->{
            CommonResult<List<DeviceChannel>> commonResult = CommonResult.<List<DeviceChannel>>builder()
                    .code(CommonResultConstants.FAIL)
                    .msg(String.format("刷新设备通道信息超时:%s",deviceId))
                    .build();
            deferredResultHandler.invokeResult(key,uuid,commonResult);
        });
        if (deferredResultHandler.exist(key,null)){
            deferredResultHandler.put(key,uuid,result);
        }
        sipCommander.deviceCatalogQuery(device,eventResult -> {
            CommonResult<List<DeviceChannel>> commonResult = CommonResult.<List<DeviceChannel>>builder()
                    .code(CommonResultConstants.FAIL)
                    .msg(String.format("刷新设备通道信息错误:%s | 错误码:%s,错误信息:%s",deviceId,eventResult.statusCode,eventResult.msg))
                    .build();
            deferredResultHandler.invokeResult(key,uuid,commonResult);
        });
        deferredResultHandler.put(key,uuid,result);
        return result;
    }
}
