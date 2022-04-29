package com.yyh.gb28181.controller;

import cn.hutool.core.util.StrUtil;
import com.yyh.common.constant.CommonResultConstants;
import com.yyh.common.exception.PlatformException;
import com.yyh.common.utils.CommonResult;
import com.yyh.gb28181.command.impl.SipCommander;
import com.yyh.web.entity.GbDevice;
import com.yyh.web.service.IGbDeviceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author: yyh
 * @date: 2022-03-02 16:59
 * @description: MobilePositionController
 **/
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("api/position")
@Api(value = "position", tags = "位置信息管理")
public class MobilePositionController {

    private final IGbDeviceService gbDeviceService;
    private final SipCommander sipCommander;

    /**
     * 订阅设备位置信息
     * @param deviceId 设备id
     * @param expires 订阅超时时间(值=0时为取消订阅)
     * @param interval 上报时间间隔
     * @return CommonResult<String>
     */
    @ApiOperation("订阅位置信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deviceId", value = "设备ID", required = true, paramType = "path",dataTypeClass = String.class),
            @ApiImplicitParam(name = "expires", value = "订阅超时时间",required = true, dataTypeClass = Integer.class),
            @ApiImplicitParam(name = "interval", value = "上报时间间隔", dataTypeClass = Integer.class),
    })
    @GetMapping("/subscribe/{deviceId}")
    public CommonResult<Boolean> positionSubscribe(@PathVariable String deviceId,
                                                  @RequestParam Integer expires,
                                                  @RequestParam(required = false) Integer interval){
        if (interval == null){
            interval = 5;
        }
        GbDevice byId = gbDeviceService.getById(deviceId);
        if (byId==null){
            throw new PlatformException(CommonResultConstants.FAIL,"设备不存在");
        }
        Boolean aBoolean = sipCommander.mobilePositionSubscribe(byId, expires, interval);
        if (aBoolean){
            return CommonResult.success(true);
        }else {
            return CommonResult.fail(false);
        }
    }






}
