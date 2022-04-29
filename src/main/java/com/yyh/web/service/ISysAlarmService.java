package com.yyh.web.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yyh.media.entity.AlarmQuery;
import com.yyh.web.entity.SysAlarm;

/**
 * @author: yyh
 * @date: 2022-03-07 17:20
 * @description: ISysAlarmService
 **/
public interface ISysAlarmService extends IService<SysAlarm> {


    /**
     * 分页查询设备的历史报警信息
     * @param deviceId 设备id
     * @param page 分页参数
     * @param query 查询参数
     * @return Page<SysAlarm>
     */
    Page<SysAlarm> queryAlarmByPage(String deviceId, Page<SysAlarm> page, AlarmQuery query);
}
