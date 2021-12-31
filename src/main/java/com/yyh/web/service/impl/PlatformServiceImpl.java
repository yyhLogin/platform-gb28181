package com.yyh.web.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyh.web.entity.ParentPlatform;
import com.yyh.web.mapper.PlatformMapper;
import com.yyh.web.service.IPlatformService;
import org.springframework.stereotype.Service;

/**
 * @author: yyh
 * @date: 2021-12-09 18:32
 * @description: PlatformServiceImpl
 **/
@Service
public class PlatformServiceImpl extends ServiceImpl<PlatformMapper, ParentPlatform> implements IPlatformService {
}
