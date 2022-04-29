package com.yyh.gb28181.controller;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.UUID;

/**
 * @author: yyh
 * @date: 2022-03-24 14:04
 * @description: DeviceControlController
 **/
@Slf4j
@RestController
@RequestMapping("/api/device/control")
@Api(tags = "国标设备控制",value = "国标设备控制")
public class DeviceControlController {

    private final IGbDeviceService gbDeviceService;
    private final SipCommander sipCommander;
    private final DeferredResultHandle deferredResultHandle;

    public DeviceControlController(IGbDeviceService gbDeviceService, SipCommander sipCommander, DeferredResultHandle deferredResultHandle) {
        this.gbDeviceService = gbDeviceService;
        this.sipCommander = sipCommander;
        this.deferredResultHandle = deferredResultHandle;
    }


    /**
     * 远程启动控制命令API接口
     *
     * @param deviceId 设备ID
     */
    @ApiOperation("远程启动控制命令")
    @ApiImplicitParam(name = "deviceId", value ="设备ID", required = true, dataTypeClass = String.class)
    @GetMapping("/teleboot/{deviceId}")
    public CommonResult<Boolean> teleBootApi(@PathVariable String deviceId){
        GbDevice device = gbDeviceService.getById(deviceId);
        if (device==null){
            throw new PlatformException(CommonResultConstants.FAIL,"设备不存在");
        }
        boolean b = sipCommander.teleBootCmd(device);
        if (b){
            return CommonResult.success(true);
        }else {
            log.warn("设备远程启动API调用失败!:{}",deviceId);
            return CommonResult.fail(false);
        }
    }

    /**
     * 录像控制命令API接口
     *
     * @param deviceId 设备ID
     * @param recordCmdStr  Record：手动录像，StopRecord：停止手动录像
     * @param channelId     通道编码（可选）
     */
    @ApiOperation("录像控制命令")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deviceId", value ="设备ID", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "channelId", value ="通道编码" ,dataTypeClass = String.class),
            @ApiImplicitParam(name = "recordCmdStr", value ="命令， 可选值：Record（手动录像），StopRecord（停止手动录像）",
                    required = true ,dataTypeClass = String.class),
    })
    @GetMapping("/record/{deviceId}/{recordCmdStr}")
    public DeferredResult<CommonResult<String>> recordApi(@PathVariable String deviceId,
                                                          @PathVariable String recordCmdStr,
                                                          String channelId){
        if (log.isDebugEnabled()) {
            log.debug("开始/停止录像API调用");
        }
        GbDevice device = gbDeviceService.getById(deviceId);
        if (device==null){
            throw new PlatformException(CommonResultConstants.FAIL,"设备不存在");
        }
        String key = DeferredResultHandle.CALLBACK_CMD_DEVICE_CONTROL +  deviceId + channelId;
        int sn = (int) ((Math.random() * 9 + 1) * 100000);
        DeferredResult<CommonResult<String>> result = new DeferredResult<>(3*1000L);
        result.onTimeout(()->{
            CommonResult<String> commonResult = CommonResult.<String>builder()
                    .code(CommonResultConstants.FAIL)
                    .data(String.format("开始/停止录像操作超时, 设备未返回应答指令:%s",deviceId))
                    .build();
            deferredResultHandle.invokeResult(key,String.valueOf(sn),commonResult);
        });
//        if (deferredResultHandle.exist(key,null)){
//            deferredResultHandle.put(key,String.valueOf(sn),result);
//            return result;
//        }
        sipCommander.recordCmd(device,channelId,recordCmdStr,sn,event->{
            CommonResult<String> commonResult = CommonResult.<String>builder()
                    .code(CommonResultConstants.FAIL)
                    .data(String.format("开始/停止录像操作失败:%s，错误码:%s, %s",deviceId,event.statusCode,event.msg))
                    .build();
            deferredResultHandle.invokeResult(key,String.valueOf(sn),commonResult);
        });
        deferredResultHandle.put(key,String.valueOf(sn),result);
        return result;
    }

    /**
     * 布防/撤防命令
     * @param deviceId 设备id
     * @param channelId 通道id
     * @param guardCmdStr 布防撤防命令
     * @return DeferredResult<CommonResult<String>>
     */
    @ApiOperation("布防/撤防命令")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deviceId", value = "设备ID", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "channelId", value ="通道编码" ,dataTypeClass = String.class),
            @ApiImplicitParam(name = "guardCmdStr", value ="命令， 可选值：SetGuard（布防），ResetGuard（撤防）", required = true,
                    dataTypeClass = String.class)
    })
    @GetMapping("/guard/{deviceId}/{guardCmdStr}")
    public DeferredResult<CommonResult<String>> guardApi(@PathVariable String deviceId,
                                                         String channelId,
                                                         @PathVariable String guardCmdStr){
        if (log.isDebugEnabled()) {
            log.debug("布防/撤防API调用");
        }
        GbDevice device = gbDeviceService.getById(deviceId);
        if (device==null){
            throw new PlatformException(CommonResultConstants.FAIL,"设备不存在");
        }
        String key = DeferredResultHandle.CALLBACK_CMD_DEVICE_CONTROL + deviceId + channelId;
        int sn = (int) ((Math.random() * 9 + 1) * 100000);
        DeferredResult<CommonResult<String>> result = new DeferredResult<>(3*1000L);
        result.onTimeout(()->{
            CommonResult<String> commonResult = CommonResult.<String>builder()
                    .code(CommonResultConstants.FAIL)
                    .data(String.format("布防/撤防操作超时, 设备未返回应答指令 | %s",deviceId))
                    .build();
            deferredResultHandle.invokeResult(key,String.valueOf(sn),commonResult);
        });
        sipCommander.guardCmd(device,guardCmdStr,sn,event->{
            CommonResult<String> commonResult = CommonResult.<String>builder()
                    .code(CommonResultConstants.FAIL)
                    .data(String.format("布防/撤防操作失败:%s，错误码： %s, %s",deviceId,event.statusCode,event.msg))
                    .build();
            deferredResultHandle.invokeResult(key,String.valueOf(sn),commonResult);
        });
        deferredResultHandle.put(key,String.valueOf(sn),result);
        return result;
    }


    /**
     * 强制关键帧API接口
     * @param deviceId 设备id
     * @param channelId 通道id
     * @return CommonResult<String>
     */
    @ApiOperation("强制关键帧")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deviceId", value = "设备ID", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "channelId", value ="通道ID", required = true, dataTypeClass = String.class),
    })
    @GetMapping("/iFrame/{deviceId}")
    public CommonResult<String> iFrame(@PathVariable String deviceId,
                                       @RequestParam(required = false) String channelId){

        if (log.isDebugEnabled()) {
            log.debug("强制关键帧API调用");
        }
        GbDevice device = gbDeviceService.getById(deviceId);
        if (device==null){
            throw new PlatformException(CommonResultConstants.FAIL,"设备不存在");
        }
        boolean b = sipCommander.iFrameCmd(device, channelId, null);
        if (b){
            return CommonResult.success("强制关键帧API调用成功");
        }else {
            log.warn("设备远程启动API调用失败!:{}",deviceId);
            return CommonResult.fail("强制关键帧API调用失败");
        }
    }

    /**
     * 看守位控制命令API接口
     *
     * @param deviceId 设备ID
     * @param enabled       看守位使能1:开启,0:关闭
     * @param resetTime     自动归位时间间隔（可选）
     * @param presetIndex   调用预置位编号（可选）
     * @param channelId     通道编码（可选）
     */
    @ApiOperation("看守位控制")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deviceId", value = "设备ID", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "channelId", value ="通道编码" ,dataTypeClass = String.class),
            @ApiImplicitParam(name = "enabled", value = "是否开启看守位 1:开启,0:关闭", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "resetTime", value = "自动归位时间间隔", dataTypeClass = String.class),
            @ApiImplicitParam(name = "presetIndex", value = "调用预置位编号", dataTypeClass = String.class),
            @ApiImplicitParam(name = "channelId", value ="通道ID", dataTypeClass = String.class),
    })
    @GetMapping("/home_position/{deviceId}/{enabled}")
    public DeferredResult<CommonResult<String>> homePositionApi(@PathVariable String deviceId,
                                                                @PathVariable String enabled,
                                                                @RequestParam(required = false) String resetTime,
                                                                @RequestParam(required = false) String presetIndex,
                                                                String channelId){
        if (log.isDebugEnabled()) {
            log.debug("报警复位API调用");
        }
        GbDevice device = gbDeviceService.getById(deviceId);
        if (device==null){
            throw new PlatformException(CommonResultConstants.FAIL,"设备不存在");
        }
        String key = DeferredResultHandle.CALLBACK_CMD_DEVICE_CONTROL + (!StringUtils.hasLength(channelId) ? deviceId : channelId);
        int sn = (int) ((Math.random() * 9 + 1) * 100000);
        DeferredResult<CommonResult<String>> result = new DeferredResult<>(3*1000L);
        result.onTimeout(()->{
            CommonResult<String> commonResult = CommonResult.<String>builder()
                    .code(CommonResultConstants.FAIL)
                    .msg(String.format("看守位控制操作超时, 设备未返回应答指令:%s",deviceId))
                    .build();
            deferredResultHandle.invokeResult(key,String.valueOf(sn),commonResult);
        });
        sipCommander.homePositionCmd(device,channelId,enabled, resetTime, presetIndex, event ->{
            CommonResult<String> commonResult = CommonResult.<String>builder()
                    .code(CommonResultConstants.FAIL)
                    .msg(String.format("看守位控制操作失败:%s|错误码： %s, %s",deviceId,event.statusCode,event.msg))
                    .build();
            deferredResultHandle.invokeResult(key,String.valueOf(sn),commonResult);
        });
        deferredResultHandle.put(key,String.valueOf(sn),result);
        return result;
    }


    /**
     * 拉框放大
     * @param deviceId 设备id
     * @param channelId 通道id
     * @param length 播放窗口长度像素值
     * @param width 播放窗口宽度像素值
     * @param midpointx 拉框中心的横轴坐标像素值
     * @param midpointy 拉框中心的纵轴坐标像素值
     * @param lengthx 拉框长度像素值
     * @param lengthy 拉框宽度像素值
     * @return
     */
    @ApiOperation("拉框放大")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deviceId", value = "设备ID", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "channelId", value = "通道ID", dataTypeClass = String.class),
            @ApiImplicitParam(name = "length", value = "播放窗口长度像素值", required = true, dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "width", value = "播放窗口宽度像素值", required = true, dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "midpointx", value = "拉框中心的横轴坐标像素值", required = true, dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "midpointy", value = "拉框中心的纵轴坐标像素值", required = true, dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "lengthx", value = "拉框长度像素值", required = true, dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "lengthy", value = "拉框宽度像素值", required = true, dataTypeClass = Integer.class),
    })
    @GetMapping("drag_zoom/zoom_in")
    public CommonResult<Boolean> dragZoomIn(@RequestParam String deviceId,
                                            @RequestParam(required = false) String channelId,
                                            @RequestParam int length,
                                            @RequestParam int width,
                                            @RequestParam int midpointx,
                                            @RequestParam int midpointy,
                                            @RequestParam int lengthx,
                                            @RequestParam int lengthy){
        if (log.isDebugEnabled()) {
            log.debug(String.format("设备拉框放大 API调用，deviceId：%s ，channelId：%s ，length：%d ，width：%d ，midpointx：%d ，midpointy：%d ，lengthx：%d ，lengthy：%d",deviceId, channelId, length, width, midpointx, midpointy,lengthx, lengthy));
        }
        GbDevice device = gbDeviceService.getById(deviceId);
        if (device==null){
            throw new PlatformException(CommonResultConstants.FAIL,"设备不存在");
        }
        String cmdXml = "<DragZoomIn>\r\n" +
                "<Length>" + length + "</Length>\r\n" +
                "<Width>" + width + "</Width>\r\n" +
                "<MidPointX>" + midpointx + "</MidPointX>\r\n" +
                "<MidPointY>" + midpointy + "</MidPointY>\r\n" +
                "<LengthX>" + lengthx + "</LengthX>\r\n" +
                "<LengthY>" + lengthy + "</LengthY>\r\n" +
                "</DragZoomIn>\r\n";
        boolean b = sipCommander.dragZoomCmd(device,channelId, cmdXml);
        return CommonResult.success(b);
    }


    /**
     * 拉框缩小
     * @param deviceId 设备id
     * @param channelId 通道id
     * @param length 播放窗口长度像素值
     * @param width 播放窗口宽度像素值
     * @param midpointx 拉框中心的横轴坐标像素值
     * @param midpointy 拉框中心的纵轴坐标像素值
     * @param lengthx 拉框长度像素值
     * @param lengthy 拉框宽度像素值
     * @return
     */
    @ApiOperation("拉框缩小")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deviceId", value = "设备ID", required = true, dataTypeClass = String.class),
            @ApiImplicitParam(name = "channelId", value = "通道ID", dataTypeClass = String.class),
            @ApiImplicitParam(name = "length", value = "播放窗口长度像素值", required = true, dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "width", value = "播放窗口宽度像素值", required = true, dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "midpointx", value = "拉框中心的横轴坐标像素值", required = true, dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "midpointy", value = "拉框中心的纵轴坐标像素值", required = true, dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "lengthx", value = "拉框长度像素值", required = true, dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "lengthy", value = "拉框宽度像素值", required = true, dataTypeClass = Integer.class),
    })
    @GetMapping("/drag_zoom/zoom_out")
    public CommonResult<Boolean> dragZoomOut(@RequestParam String deviceId,
                                             @RequestParam(required = false) String channelId,
                                             @RequestParam int length,
                                             @RequestParam int width,
                                             @RequestParam int midpointx,
                                             @RequestParam int midpointy,
                                             @RequestParam int lengthx,
                                             @RequestParam int lengthy){

        if (log.isDebugEnabled()) {
            log.debug(String.format("设备拉框缩小 API调用，deviceId：%s ，channelId：%s ，length：%d ，width：%d ，midpointx：%d ，midpointy：%d ，lengthx：%d ，lengthy：%d",deviceId, channelId, length, width, midpointx, midpointy,lengthx, lengthy));
        }
        GbDevice device = gbDeviceService.getById(deviceId);
        if (device==null){
            throw new PlatformException(CommonResultConstants.FAIL,"设备不存在");
        }
        String cmdXml = "<DragZoomOut>\r\n" +
                "<Length>" + length + "</Length>\r\n" +
                "<Width>" + width + "</Width>\r\n" +
                "<MidPointX>" + midpointx + "</MidPointX>\r\n" +
                "<MidPointY>" + midpointy + "</MidPointY>\r\n" +
                "<LengthX>" + lengthx + "</LengthX>\r\n" +
                "<LengthY>" + lengthy + "</LengthY>\r\n" +
                "</DragZoomOut>\r\n";
        boolean b = sipCommander.dragZoomCmd(device, channelId, cmdXml);
        return CommonResult.success(b);
    }
}
