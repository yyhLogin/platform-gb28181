package com.yyh.web.service.impl;

import com.yyh.web.service.ITestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author: yyh
 * @date: 2021-11-22 11:46
 * @description: TestServiceImpl
 **/
@Slf4j
@Service
public class TestServiceImpl implements ITestService {
    /**
     * 说话
     *
     * @return String
     */
    @Override
    public String sayHello() {
        log.info("sayHello | hello");
        return "hello";
    }
}
