package com.yyh.web.controller;

import com.yyh.web.service.ITestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author: yyh
 * @date: 2021-11-22 11:43
 * @description: TestController
 **/
@RestController
@RequestMapping("test")
public class TestController {

    @Resource
    private ITestService testService;

    @GetMapping()
    public ResponseEntity<String> test(@RequestParam String name){
        String s = testService.sayHello();
        return ResponseEntity.ok(s);
    }
}
