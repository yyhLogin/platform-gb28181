package com.yyh.gb28181.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class RedisServiceTest {

    @Resource
    private RedisService redisService;

    @Test
    public void increment(){
        long yyh = redisService.increment("yyh");
        log.info("key|{}",yyh);
    }
}