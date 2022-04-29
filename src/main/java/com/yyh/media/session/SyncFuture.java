package com.yyh.media.session;

import java.util.concurrent.*;

/**
 * @author: yyh
 * @date: 2022-01-19 11:00
 * @description: SyncFuture
 * 处理等待消息
 **/
public class SyncFuture<T> extends CompletableFuture<T> {

    public SyncFuture(T t){
        response = t;
    }

    /**
     * 因为请求和响应是一一对应的，因此初始化CountDownLatch值为1。
     */
    private CountDownLatch latch = new CountDownLatch(1);

    /**
     * 返回结果
     */
    private T response;

    /**
     * Future的请求时间，用于计算Future是否超时
     */
    private final long bTime = System.currentTimeMillis();

}
