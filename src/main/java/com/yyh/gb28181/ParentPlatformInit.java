package com.yyh.gb28181;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yyh.gb28181.command.ISipCommander4Platform;
import com.yyh.gb28181.event.platform.OnPlatformEnum;
import com.yyh.gb28181.event.platform.OnPlatformEvent;
import com.yyh.web.entity.ParentPlatform;
import com.yyh.web.service.IPlatformService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: yyh
 * @date: 2022-04-08 14:27
 * @description: ParentPlatformInit 上级平台初始化
 **/
@Slf4j
@Component
@Order(10087)
public class ParentPlatformInit implements CommandLineRunner {

    private final IPlatformService platformService;

    private final ApplicationEventPublisher publisher;

    public ParentPlatformInit(IPlatformService platformService, ApplicationEventPublisher publisher) {
        this.platformService = platformService;
        this.publisher = publisher;
    }

    @Async
    @Override
    public void run(String... args) throws Exception {
        log.info(">>>>>>>>>>   Parent platform init start  >>>>>>>>>>");
        //检测有哪些上级平台
        List<ParentPlatform> list = platformService.list(Wrappers.<ParentPlatform>lambdaQuery()
                .eq(ParentPlatform::getEnable,true));
        if (list!=null&&list.size()>0){
            //注册平台
            for (ParentPlatform platform : list){
                publisher.publishEvent(new OnPlatformEvent("register", OnPlatformEnum.REGISTER,platform));
            }
        }
        log.info("<<<<<<<<<<   Parent platform init end    <<<<<<<<<<");
    }
}
