package com.yyh.gb28181.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yyh.common.utils.CommonResult;
import com.yyh.media.entity.AlarmQuery;
import com.yyh.web.entity.SysAlarm;
import com.yyh.web.service.ISysAlarmService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * @author: yyh
 * @date: 2022-03-03 15:53
 * @description: AlarmController
 **/
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("api/alarm")
@Api(value = "alarm", tags = "报警信息管理")
public class AlarmController {


    private final ISysAlarmService alarmService;


    /**
     * 查询报警信息
     * @param deviceId 设备id
     * @param page 分页参数
     * @param query 查询参数
     * @return CommonResult<Page<SysAlarm>>
     */
    @GetMapping("page/{deviceId}")
    @ApiOperation("分页查询设备的历史报警信息")
    @ApiImplicitParam(name = "deviceId", value = "设备ID", paramType = "path", dataTypeClass = String.class)
    public CommonResult<Page<SysAlarm>> queryAlarmByPage(@PathVariable String deviceId,
                                                         Page<SysAlarm> page,
                                                         AlarmQuery query){
        return CommonResult.success(alarmService.queryAlarmByPage(deviceId,page,query));
    }




}
