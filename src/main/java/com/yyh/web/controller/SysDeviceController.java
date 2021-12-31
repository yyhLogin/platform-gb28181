package com.yyh.web.controller;

import com.yyh.common.utils.CommonResult;
import com.yyh.web.entity.SysDevice;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: yyh
 * @date: 2021-12-03 16:17
 * @description: SysDeviceController
 **/
@Slf4j
@RestController
@RequestMapping("api/device")
@Api(value = "device", tags = "设备模块")
public class SysDeviceController {

    /**
     * 分页查询数据
     * @return CommonResult<SysDevice>
     */
    @GetMapping
    @ApiOperation(value = "获取用户在线状态", notes = "获取设备列表",httpMethod = "GET")
    public CommonResult<SysDevice> queryDeviceByPage(){
        return null;
    }
}
