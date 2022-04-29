package com.yyh.web.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yyh.common.utils.CommonResult;
import com.yyh.web.entity.DeviceChannel;
import com.yyh.web.dto.GbDeviceDTO;
import com.yyh.web.entity.GbDevice;
import com.yyh.web.service.IGbDeviceChannelService;
import com.yyh.web.service.IGbDeviceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author: yyh
 * @date: 2022-03-29 14:36
 * @description: GbDeviceController
 **/
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/device/gb")
@Api(value = "device", tags = "国标设备模块")
public class GbDeviceController {


    private final IGbDeviceService gbDeviceService;

    private final IGbDeviceChannelService deviceChannelService;

    /**
     * 分页查询国标设备
     * @param page 分页参数
     * @param device 查询参数
     * @return CommonResult<Page<GbDevice>>
     */
    @GetMapping("page")
    @ApiOperation(value = "分页获取国标设备列表", notes = "可以根据不同的信息查询",httpMethod = "GET")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "分页参数", dataTypeClass = Page.class),
            @ApiImplicitParam(name = "device", value = "查询参数", dataTypeClass = GbDeviceDTO.class)
    })
    public CommonResult<Page<GbDevice>> queryDeviceByPage(Page<GbDevice> page, GbDeviceDTO device){
        return CommonResult.success(gbDeviceService.queryDeviceByPage(page,device));
    }


    /**
     * 更新设备信息
     * @param deviceId 设备id
     * @param name 设备名称
     * @param charSet 通信字符集
     * @param sub 订阅周期
     * @return CommonResult<Boolean>
     */
    @PutMapping("{deviceId}")
    @ApiOperation(value = "更新设备信息", notes = "根据id更新设备信息",httpMethod = "PUT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deviceId", value = "设备id", dataTypeClass = String.class,paramType = "path"),
            @ApiImplicitParam(name = "name", value = "设备名称",dataTypeClass = String.class,paramType = "query"),
            @ApiImplicitParam(name = "charSet", value = "字符集", dataTypeClass = String.class,paramType = "query"),
            @ApiImplicitParam(name = "sub", value = "目录订阅周期(0-不订阅)", dataTypeClass = String.class,paramType = "query")
    })
    public CommonResult<Boolean> updateDeviceById(@PathVariable String deviceId,
                                                  @RequestParam(required = false)String name,
                                                  @RequestParam(required = false)String charSet,
                                                  @RequestParam(required = false)String sub){
        return CommonResult.success(gbDeviceService.updateDeviceById(deviceId,name,charSet,sub));
    }

    /**
     * 删除设备
     * @param deviceId 设备id
     * @return CommonResult<Boolean>
     */
    @DeleteMapping("{deviceId}")
    @ApiOperation(value = "删除设备", notes = "根据id删除设备",httpMethod = "DELETE")
    @ApiImplicitParam(name = "deviceId", value = "设备id", dataTypeClass = String.class,paramType = "path")
    public CommonResult<Boolean> removeDevice(@PathVariable String deviceId){
        return CommonResult.success(gbDeviceService.removeById(deviceId));
    }


    /**
     * 更新流传输模式
     * @param deviceId 设备编号
     * @param streamMode 流传输模式
     * @return CommonResult<Boolean>
     */
    @PutMapping("stream/{deviceId}")
    @ApiOperation(value = "更新设备流传输模式", notes = "更新设备流传输模式",httpMethod = "PUT")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deviceId", value = "设备id", dataTypeClass = String.class,paramType = "path"),
            @ApiImplicitParam(name = "streamMode", value = "流传输模式", dataTypeClass = String.class)
    })
    public CommonResult<Boolean> updateStreamMode(@PathVariable String deviceId,
                                                  @RequestParam String streamMode){
        return CommonResult.success( gbDeviceService.updateStreamMode(deviceId,streamMode));
    }


    /**
     * 查询设备的通道信息
     * @param deviceId deviceId
     * @return CommonResult<List<DeviceChannel>>
     */
    @GetMapping("channel/{deviceId}")
    @ApiOperation(value = "查询设备通道", notes = "根据设备id查询设备通道信息",httpMethod = "GET")
    @ApiImplicitParam(name = "deviceId", value = "设备id", dataTypeClass = String.class, paramType = "path")
    public CommonResult<List<DeviceChannel>> queryDeviceChannelByDeviceId(@PathVariable String deviceId){
        return CommonResult.success(deviceChannelService.list(Wrappers.<DeviceChannel>lambdaQuery()
                .eq(DeviceChannel::getDeviceId,deviceId)));
    }
}
