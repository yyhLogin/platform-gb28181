package com.yyh.gb28181.callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: yyh
 * @date: 2021-12-08 16:27
 * @description: SipSubscribe
 **/
@Component
public class SipSubscribe {
    private final Logger logger = LoggerFactory.getLogger(SipSubscribe.class);

    private final Map<String, SipCallback> errorSubscribes = new ConcurrentHashMap<>();

    private final Map<String, SipCallback> okSubscribes = new ConcurrentHashMap<>();

    private final Map<String, Date> timeSubscribes = new ConcurrentHashMap<>();

    /**
     * 每小时执行一次， 每个整点
     */
    @Scheduled(cron="0 0 * * * ?")
    public void execute(){
        logger.info("[定时任务] 清理过期的订阅信息");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR, calendar.get(Calendar.HOUR) - 1);
        for (String key : timeSubscribes.keySet()) {
            if (timeSubscribes.get(key).before(calendar.getTime())){
                logger.info("[定时任务] 清理过期的订阅信息： {}", key);
                errorSubscribes.remove(key);
                okSubscribes.remove(key);
                timeSubscribes.remove(key);
            }
        }
    }

    public void addErrorSubscribe(String key, SipCallback callback) {
        errorSubscribes.put(key, callback);
        timeSubscribes.put(key, new Date());
    }

    public void addOkSubscribe(String key, SipCallback callback) {
        okSubscribes.put(key, callback);
        timeSubscribes.put(key, new Date());
    }

    public SipCallback getErrorSubscribe(String key) {
        return errorSubscribes.get(key);
    }

    public void removeErrorSubscribe(String key) {
        errorSubscribes.remove(key);
        timeSubscribes.remove(key);
    }

    public SipCallback getOkSubscribe(String key) {
        return okSubscribes.get(key);
    }

    public void removeOkSubscribe(String key) {
        okSubscribes.remove(key);
        timeSubscribes.remove(key);
    }
    public int getErrorSubscribesSize(){
        return errorSubscribes.size();
    }
    public int getOkSubscribesSize(){
        return okSubscribes.size();
    }

}
