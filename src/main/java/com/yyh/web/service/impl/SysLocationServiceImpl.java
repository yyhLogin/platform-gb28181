package com.yyh.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyh.web.entity.SysLocation;
import com.yyh.web.mapper.SysLocationMapper;
import com.yyh.web.service.ISysLocationService;
import org.springframework.stereotype.Service;

/**
 * @author: yyh
 * @date: 2022-02-15 15:29
 * @description: SysLocationServiceImpl
 **/
@Service
public class SysLocationServiceImpl extends ServiceImpl<SysLocationMapper, SysLocation> implements ISysLocationService {
}
