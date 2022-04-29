package com.yyh.gb28181.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yyh.common.utils.CommonResult;
import com.yyh.config.ConverterType;
import com.yyh.gb28181.handle.request.service.SipRequestProcessorService;
import com.yyh.media.callback.DeferredResultHandle;
import com.yyh.media.callback.RecordInfoDeferredHandle;
import com.yyh.media.entity.RecordInfo;
import com.yyh.media.entity.RecordItem;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author: yyh
 * @date: 2022-02-17 11:14
 * @description: CheckForAllRecordsThread
 **/
public class CheckForAllRecordsThread extends Thread {

    private final String key;

    private final RecordInfo recordInfo;

    private final String sn;

    private RedisTemplate<String,String> redis;

    private Logger logger;

    private ObjectMapper mapper;

    private RecordInfoDeferredHandle recordInfoDeferredHandle;

    public CheckForAllRecordsThread(String key, RecordInfo recordInfo,String sn) {
        this.key = key;
        this.recordInfo = recordInfo;
        this.sn = sn;
    }

    @SneakyThrows
    @Override
    public void run() {
        for (long stop = System.nanoTime() + TimeUnit.SECONDS.toNanos(10); stop > System.nanoTime();) {
            Set<String> keys = redis.keys(this.key + "_*");
            if (keys==null){
                return;
            }
            List<RecordItem> totalRecordList = new ArrayList<RecordItem>();
            for (String key : keys){
                String s = redis.opsForValue().get(key);
                ArrayList<RecordItem> recordItems = mapper.readValue(s, ConverterType.ARRAYLIST_RECORD_ITEM_TYPE);
                totalRecordList.addAll(recordItems);
            }
            if (totalRecordList.size() < this.recordInfo.getSumNum()) {
                logger.info("已获取" + totalRecordList.size() + "项录像数据，共" + this.recordInfo.getSumNum() + "项");
            } else {
                logger.info("录像数据已全部获取，共 {} 项", this.recordInfo.getSumNum());
                this.recordInfo.setRecordList(totalRecordList);
                for (String key : keys) {
                    redis.delete(key);
                }
                break;
            }
        }
        // 自然顺序排序, 元素进行升序排列
        this.recordInfo.getRecordList().sort(Comparator.naturalOrder());
        CommonResult<RecordInfo> result = CommonResult.success(recordInfo);
        String handleKey = DeferredResultHandle.CALLBACK_CMD_RECORD_INFO + recordInfo.getDeviceId() + sn;
        recordInfoDeferredHandle.invokeHandle(handleKey,sn,result);
        logger.info("处理完成，返回结果");
        SipRequestProcessorService.threadNameList.remove(this.key);
    }

    public void setRedis(RedisTemplate<String,String> redis) {
        this.redis = redis;
    }

    public void setRecordInfoDeferredHandle(RecordInfoDeferredHandle recordInfoDeferredHandle) {
        this.recordInfoDeferredHandle = recordInfoDeferredHandle;
    }

    public void setMapper(ObjectMapper mapper){
        this.mapper = mapper;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

}
