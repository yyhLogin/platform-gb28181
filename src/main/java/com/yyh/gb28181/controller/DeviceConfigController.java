package com.yyh.gb28181.controller;

import cn.hutool.core.util.StrUtil;
import com.yyh.common.constant.CommonResultConstants;
import com.yyh.common.exception.PlatformException;
import com.yyh.common.utils.CommonResult;
import com.yyh.gb28181.command.impl.SipCommander;
import com.yyh.media.callback.DeferredResultHandle;
import com.yyh.web.entity.GbDevice;
import com.yyh.web.service.IGbDeviceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.UUID;

/**
 * @author: yyh
 * @date: 2022-03-23 10:35
 * @description: DeviceConfigController 设备配置
 **/
@Slf4j
@RestController
@RequestMapping("/api/device/config")
@Api(tags = "国标设备设置", value = "国标设备设置")
public class DeviceConfigController {


    private final IGbDeviceService gbDeviceService;
    private final SipCommander sipCommander;
    private final DeferredResultHandle deferredResultHandle;

    public DeviceConfigController(IGbDeviceService gbDeviceService, SipCommander sipCommander, DeferredResultHandle deferredResultHandle) {
        this.gbDeviceService = gbDeviceService;
        this.sipCommander = sipCommander;
        this.deferredResultHandle = deferredResultHandle;
    }

    /**
     * 设备配置查询请求API接口
     * @param deviceId 设备ID
     * @param configType 配置类型
     * @param channelId 通道ID
     * @return
     */
    @ApiOperation("设备配置查询请求")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deviceId", value ="设备ID" ,dataTypeClass = String.class),
            @ApiImplicitParam(name = "channelId", value ="通道ID" ,dataTypeClass = String.class),
            @ApiImplicitParam(name = "configType", value ="配置类型" ,dataTypeClass = String.class),
    })
    @GetMapping("/query/{deviceId}/{configType}")
    public DeferredResult<CommonResult<String>> configDownloadApi(@PathVariable String deviceId,
                                                                    @PathVariable String configType,
                                                                    @RequestParam(required = false) String channelId) {
        if (log.isDebugEnabled()) {
            log.debug("设备状态查询API调用");
        }
        String key = DeferredResultHandle.CALLBACK_CMD_CONFIG_DOWNLOAD + (StrUtil.isBlank(channelId) ? deviceId : channelId);
        ///String key = DeferredResultHandle.CALLBACK_CMD_CONFIG_DOWNLOAD + deviceId + (StrUtil.isBlank(channelId)?"":channelId);
        GbDevice device = gbDeviceService.getById(deviceId);
        String uuid = UUID.randomUUID().toString();
        if (device==null){
            throw new PlatformException(CommonResultConstants.FAIL,"设备不存在");
        }
        sipCommander.deviceConfigQuery(device, channelId, configType, event -> {
            CommonResult<String> commonResult = CommonResult.<String>builder()
                    .code(CommonResultConstants.FAIL)
                    .msg(String.format("获取设备配置失败，错误码： %s, %s", event.statusCode, event.msg))
                    .build();
            deferredResultHandle.invokeResult(key,uuid,commonResult);
        });
        DeferredResult<CommonResult<String>> result = new DeferredResult<> (3 * 1000L);
        result.onTimeout(()->{
            log.warn(String.format("获取设备配置超时:%s",deviceId));
            CommonResult<String> commonResult = CommonResult.<String>builder()
                    .code(CommonResultConstants.FAIL)
                    .msg("Timeout. Device did not response to this command.")
                    .build();
            deferredResultHandle.invokeResult(key,uuid,commonResult);
        });
        deferredResultHandle.put(key, uuid, result);
        return result;
    }

    /**
     * 看守位控制命令API接口
     * @param deviceId 设备ID
     * @param channelId 通道ID
     * @param name 名称
     * @param expiration 到期时间
     * @param heartBeatInterval 心跳间隔
     * @param heartBeatCount 心跳计数
     * @return DeferredResult<CommonResult<String>>
     */
    @ApiOperation("基本配置设置命令")
    @GetMapping("/basicParam/{deviceId}")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deviceId", value ="设备ID" ,dataTypeClass = String.class),
            @ApiImplicitParam(name = "channelId", value ="通道ID",required = true,dataTypeClass = String.class ),
            @ApiImplicitParam(name = "name", value ="名称" ,dataTypeClass = String.class),
            @ApiImplicitParam(name = "expiration", value ="到期时间" ,dataTypeClass = String.class),
            @ApiImplicitParam(name = "heartBeatInterval", value ="心跳间隔" ,dataTypeClass = String.class),
            @ApiImplicitParam(name = "heartBeatCount", value ="心跳计数" ,dataTypeClass = String.class),
    })
    public DeferredResult<CommonResult<String>> basicConfigSetting(@PathVariable String deviceId,
                                                                   String channelId,
                                                                   @RequestParam(required = false) String name,
                                                                   @RequestParam(required = false) String expiration,
                                                                   @RequestParam(required = false) String heartBeatInterval,
                                                                   @RequestParam(required = false) String heartBeatCount){
        if (log.isDebugEnabled()) {
            log.debug("报警复位API调用");
        }
        GbDevice device = gbDeviceService.getById(deviceId);
        if (device==null){
            throw new PlatformException(CommonResultConstants.FAIL,"设备不存在");
        }
        //String uuid = UUID.randomUUID().toString();
        String key = DeferredResultHandle.CALLBACK_CMD_DEVICE_CONFIG + deviceId + channelId;
        int sn = (int)((Math.random() * 9 + 1) * 100000);
        sipCommander.deviceBasicConfigCmd(device, channelId,sn, name, expiration, heartBeatInterval, heartBeatCount, event -> {
            CommonResult<String> commonResult = CommonResult.<String>builder()
                    .code(CommonResultConstants.FAIL)
                    .msg(String.format("设备配置操作失败，错误码： %s, %s", event.statusCode, event.msg))
                    .build();
            deferredResultHandle.invokeResult(key,String.valueOf(sn),commonResult);
        });
        DeferredResult<CommonResult<String>> result = new DeferredResult<>(3 * 1000L);
        result.onTimeout(() -> {
            log.warn(String.format("设备配置操作超时, 设备未返回应答指令%s",deviceId));
            // 释放rtp server
            CommonResult<String> commonResult = CommonResult.<String>builder()
                    .code(CommonResultConstants.FAIL)
                    .msg(String.format("设备配置操作超时, 设备未返回应答指令: %s",deviceId))
                    .build();
            deferredResultHandle.invokeResult(key,String.valueOf(sn),commonResult);
        });
        deferredResultHandle.put(key, String.valueOf(sn), result);
        return result;
    }

}
