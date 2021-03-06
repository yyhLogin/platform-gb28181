package com.yyh.config;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author: yyh
 * @date: 2021-12-10 11:25
 * @description: AsyncThreadPoolAutoConfiguration
 **/
@Slf4j
@Configuration
@EnableAsync
public class AsyncThreadPoolAutoConfiguration implements AsyncConfigurer {

    /**
     * 获取当前系统的CPU 数目
     */
    int cpuNums = Runtime.getRuntime().availableProcessors();
    /**
     * 线程池的最大线程数
     */
    private final int maxPoolSize = cpuNums * 20;
    private final int corePoolSize = cpuNums * 5;

    @Override
    public Executor getAsyncExecutor() {
        /// 定义线程池
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        /// 核心线程数
        taskExecutor.setCorePoolSize(corePoolSize);
        /// 线程池最大线程数,默认：40000
        taskExecutor.setMaxPoolSize(maxPoolSize);
        /// 线程队列最大线程数,默认：80000
        taskExecutor.setQueueCapacity(100);
        /// 线程名称前缀
        taskExecutor.setThreadNamePrefix("async-thread-");
        /// 线程池中线程最大空闲时间，默认：60，单位：秒
        taskExecutor.setKeepAliveSeconds(60);
        /// 核心线程是否允许超时，默认:false
        taskExecutor.setAllowCoreThreadTimeOut(false);
        /// IOC容器关闭时是否阻塞等待剩余的任务执行完成，默认:false（必须设置setAwaitTerminationSeconds）
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        /// 阻塞IOC容器关闭的时间，默认：10秒（必须设置setWaitForTasksToCompleteOnShutdown）
        taskExecutor.setAwaitTerminationSeconds(10);
        /*
          拒绝策略，默认是AbortPolicy
          AbortPolicy：丢弃任务并抛出RejectedExecutionException异常
          DiscardPolicy：丢弃任务但不抛出异常
          DiscardOldestPolicy：丢弃最旧的处理程序，然后重试，如果执行器关闭，这时丢弃任务
          CallerRunsPolicy：执行器执行任务失败，则在策略回调方法中执行任务，如果执行器关闭，这时丢弃任务
         */
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        /// 初始化
        taskExecutor.initialize();
        log.info("Async-ThreadPool-Init -> CorePool:{},MaxPool:{}", corePoolSize, maxPoolSize);
        return taskExecutor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, objects) -> {
            String msg = StrUtil.EMPTY;
            if (ArrayUtil.isNotEmpty(objects) && objects.length > 0) {
                msg = StrUtil.join(msg, "参数是：");
                for (Object object : objects) {
                    msg = StrUtil.join(msg, object, StrUtil.EMPTY);
                }
            }
            msg = StrUtil.join(msg, throwable);
            log.error(msg, method.getDeclaringClass());
        };
    }
}
