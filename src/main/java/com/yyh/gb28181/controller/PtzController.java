package com.yyh.gb28181.controller;

import com.yyh.common.constant.CommonResultConstants;
import com.yyh.common.exception.PlatformException;
import com.yyh.common.utils.CommonResult;
import com.yyh.gb28181.command.ISipCommander;
import com.yyh.media.callback.DeferredResultHandle;
import com.yyh.gb28181.service.IPlayService;
import com.yyh.media.utils.SnowflakeUtil;
import com.yyh.web.entity.GbDevice;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Map;

/**
 * @author: yyh
 * @date: 2022-04-11 10:29
 * @description: PtzController 云台控制
 **/
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("api/ptz")
@Api(value = "ptz", tags = "云台控制")
public class PtzController {


    private final ISipCommander sipCommander;

    private final IPlayService playService;

    private final DeferredResultHandle deferredResultHandle;



    /***
     * 云台控制
     * @param deviceId 设备id
     * @param channelId 通道id
     * @param command	控制指令
     * @param horizonSpeed	水平移动速度
     * @param verticalSpeed	垂直移动速度
     * @param zoomSpeed	    缩放速度
     * @return String 控制结果
     */
    @ApiOperation("云台控制")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deviceId", value = "设备ID", dataTypeClass = String.class),
            @ApiImplicitParam(name = "channelId", value = "通道ID", dataTypeClass = String.class),
            @ApiImplicitParam(name = "command", value = "控制指令,允许值: left, right, up, down, upleft, upright, downleft, downright, zoomin, zoomout, stop", dataTypeClass = String.class),
            @ApiImplicitParam(name = "horizonSpeed", value = "水平速度", dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "verticalSpeed", value = "垂直速度", dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "zoomSpeed", value = "缩放速度", dataTypeClass = Integer.class),
    })
    @PostMapping("/control/{deviceId}/{channelId}")
    public CommonResult<Boolean> ptz(@PathVariable String deviceId,
                                    @PathVariable String channelId,
                                    String command,
                                    int horizonSpeed,
                                    int verticalSpeed,
                                    int zoomSpeed){
        if (log.isDebugEnabled()) {
            log.debug(String.format("设备云台控制 API调用，deviceId：%s ，channelId：%s ，command：%s ，horizonSpeed：%d ，verticalSpeed：%d ，zoomSpeed：%d",deviceId, channelId, command, horizonSpeed, verticalSpeed, zoomSpeed));
        }
        GbDevice device = playService.queryGbDevice(deviceId, channelId);
        if (device==null){
            return CommonResult.fail(String.format("设备不存在: %s | %s ",deviceId,channelId));
        }
        int cmdCode = 0;
        switch (command){
            case "left":
                cmdCode = 2;
                break;
            case "right":
                cmdCode = 1;
                break;
            case "up":
                cmdCode = 8;
                break;
            case "down":
                cmdCode = 4;
                break;
            case "upleft":
                cmdCode = 10;
                break;
            case "upright":
                cmdCode = 9;
                break;
            case "downleft":
                cmdCode = 6;
                break;
            case "downright":
                cmdCode = 5;
                break;
            case "zoomin":
                cmdCode = 16;
                break;
            case "zoomout":
                cmdCode = 32;
                break;
            case "stop":
                cmdCode = 0;
                break;
            default:
                break;
        }
        boolean b = sipCommander.frontEndCmd(device, channelId, cmdCode, horizonSpeed, verticalSpeed, zoomSpeed);
        return CommonResult.success(b,"云台控制");
    }

    @ApiOperation("通用前端控制命令")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deviceId", value = "设备ID", dataTypeClass = String.class),
            @ApiImplicitParam(name = "channelId", value = "通道ID", dataTypeClass = String.class),
            @ApiImplicitParam(name = "cmdCode", value = "指令码", dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "parameter1", value = "数据一", dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "parameter2", value = "数据二", dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "comBindCode2", value = "组合码二", dataTypeClass = Integer.class),
    })
    @PostMapping("/front_end_command/{deviceId}/{channelId}")
    public CommonResult<Boolean> frontEndCommand(@PathVariable String deviceId,
                                                 @PathVariable String channelId,
                                                 int cmdCode,
                                                 int parameter1,
                                                 int parameter2,
                                                 int comBindCode2){
        if (log.isDebugEnabled()) {
            log.debug(String.format("设备云台控制 API调用，deviceId：%s ，channelId：%s ，cmdCode：%d parameter1：%d parameter2：%d",deviceId, channelId, cmdCode, parameter1, parameter2));
        }
        GbDevice device = playService.queryGbDevice(deviceId, channelId);
        if (device==null){
            return CommonResult.fail(String.format("设备不存在: %s | %s ",deviceId,channelId));
        }
        boolean b = sipCommander.frontEndCmd(device, channelId, cmdCode, parameter1, parameter2, comBindCode2);
        return CommonResult.success(b,"云台控制");
    }

    @ApiOperation("预置位查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deviceId", value = "设备ID", dataTypeClass = String.class),
            @ApiImplicitParam(name = "channelId", value = "通道ID", dataTypeClass = String.class),
    })
    @GetMapping("/preset/query/{deviceId}/{channelId}")
    public DeferredResult<CommonResult<Map<String,Object>>> presetQueryApi(@PathVariable String deviceId,
                                                                           @PathVariable String channelId){

        if (log.isDebugEnabled()) {
            log.debug("设备预置位查询API调用");
        }
        GbDevice device = playService.queryGbDevice(deviceId, channelId);
        if (device==null){
           throw new PlatformException(CommonResultConstants.FAIL,String.format("设备不存在: %s | %s ",deviceId,channelId));
        }
        long snowflake = SnowflakeUtil.getSnowflake();
        String key =  DeferredResultHandle.CALLBACK_CMD_PRESET_QUERY + (StringUtils.hasLength(channelId) ? channelId : deviceId);
        DeferredResult<CommonResult<Map<String,Object>>> result = new DeferredResult<>(3*1000L);
        result.onTimeout(()->{
            CommonResult<String> commonResult = new CommonResult<>();
            commonResult.setCode(CommonResultConstants.FAIL);
            commonResult.setMsg(String.format("获取设备预置位超时:%s|%s",deviceId,channelId));
            deferredResultHandle.invokeResult(key,String.valueOf(snowflake),commonResult);
        });
        deferredResultHandle.put(key,String.valueOf(snowflake),result);
        sipCommander.presetQuery(device,channelId,snowflake,(event)->{
            CommonResult<String> commonResult = new CommonResult<>();
            commonResult.setCode(CommonResultConstants.FAIL);
            commonResult.setMsg(String.format("获取设备预置位失败:%s|%s",deviceId,channelId));
            deferredResultHandle.invokeAllResult(key,commonResult);
        });
        return result;
    }
}
