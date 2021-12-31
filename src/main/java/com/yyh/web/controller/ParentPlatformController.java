package com.yyh.web.controller;

import com.yyh.web.entity.ParentPlatform;
import com.yyh.web.service.IPlatformService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author: yyh
 * @date: 2021-12-09 18:28
 * @description: ParentPlatformController
 **/
@Slf4j
@RestController
@RequestMapping("api/platform")
@Api(value = "platform", tags = "上级平台")
public class ParentPlatformController {


    @Resource
    private IPlatformService platformService;

    @GetMapping
    public ResponseEntity<String> save(){
        ParentPlatform parentPlatform = new ParentPlatform();
        parentPlatform.setDeviceGbId("111");
        parentPlatform.setServerGbId("111");
        platformService.save(parentPlatform);
        return ResponseEntity.ok("测试");
    }

    @GetMapping("1")
    public ResponseEntity<List<ParentPlatform>> query(){
        return ResponseEntity.ok(platformService.list());
    }

}
