package com.yyh.web.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyh.media.entity.AlarmQuery;
import com.yyh.web.entity.SysAlarm;
import com.yyh.web.mapper.SysAlarmMapper;
import com.yyh.web.service.ISysAlarmService;
import org.springframework.stereotype.Service;

/**
 * @author: yyh
 * @date: 2022-03-07 17:20
 * @description: SysAlarmServiceImpl
 **/
@Service
public class SysAlarmServiceImpl extends ServiceImpl<SysAlarmMapper, SysAlarm> implements ISysAlarmService {

    /**
     * 分页查询设备的历史报警信息
     *
     * @param deviceId 设备id
     * @param page     分页参数
     * @param query    查询参数
     * @return Page<SysAlarm>
     */
    @Override
    public Page<SysAlarm> queryAlarmByPage(String deviceId, Page<SysAlarm> page, AlarmQuery query) {
        LambdaQueryWrapper<SysAlarm> eq = Wrappers.<SysAlarm>lambdaQuery().eq(SysAlarm::getDeviceId, deviceId);
        if (query==null){
            return this.page(page,eq);
        }
        eq.eq(StrUtil.isNotBlank(query.getAlarmPriority()),SysAlarm::getAlarmLevel,query.getAlarmPriority())
                .eq(StrUtil.isNotBlank(query.getAlarmMethod()),SysAlarm::getAlarmMethod,query.getAlarmMethod())
                .eq(StrUtil.isNotBlank(query.getAlarmType()),SysAlarm::getAlarmType,query.getAlarmType());

        if (query.getStartTime()!=null&&query.getEndTime()!=null){
            eq.between(SysAlarm::getAlarmTime,query.getStartTime(),query.getEndTime());
        }
        return this.page(page,eq);
    }
}
