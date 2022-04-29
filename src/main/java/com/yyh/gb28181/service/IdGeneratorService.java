package com.yyh.gb28181.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * @author: yyh
 * @date: 2022-04-19 17:45
 * @description: IdGeneratorService 发号
 **/
@Service
@RequiredArgsConstructor
public class IdGeneratorService {
    private final RedisService redisService;

    /**
     * 生成id（每日重置自增序列）
     * 格式：日期 + 6位自增数
     * 如：20210804000001
     * @param key key
     * @param length length
     * @return
     */
    public String generateId(String key, Integer length) {
        long num = redisService.increment(key, getEndTime());
        String id = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + String.format("%0" + length + "d", num);
        return id;
    }

    /**
     * 获取当天的结束时间
     */
    public Instant getEndTime() {
        LocalDateTime endTime = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        return endTime.toInstant(ZoneOffset.ofHours(8));
    }
}

