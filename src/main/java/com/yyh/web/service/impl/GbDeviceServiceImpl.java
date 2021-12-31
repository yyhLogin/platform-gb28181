package com.yyh.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyh.web.entity.GbDevice;
import com.yyh.web.mapper.GbDeviceMapper;
import com.yyh.web.service.IGbDeviceService;
import org.springframework.stereotype.Service;

/**
 * @author: yyh
 * @date: 2021-12-06 16:49
 * @description: GbDeviceServiceImpl
 **/
@Service
public class GbDeviceServiceImpl extends ServiceImpl<GbDeviceMapper, GbDevice> implements IGbDeviceService {
}
