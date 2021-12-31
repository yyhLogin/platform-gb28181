package com.yyh.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyh.web.entity.SysDevice;
import com.yyh.web.mapper.SysDeviceMapper;
import com.yyh.web.service.ISysDeviceService;
import org.springframework.stereotype.Service;

/**
 * @author: yyh
 * @date: 2021-12-03 16:30
 * @description: SysDeviceServiceImpl
 **/
@Service
public class SysDeviceServiceImpl extends ServiceImpl<SysDeviceMapper, SysDevice> implements ISysDeviceService {
}
