package com.yyh.gb28181.command;

import com.google.common.util.concurrent.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.util.concurrent.*;

/**
 * @author: yyh
 * @date: 2021-12-07 11:15
 * @description: Test
 **/
@Slf4j
public class Test {
    final static ThreadPoolExecutor executor = new ThreadPoolExecutor(
                5,
                        10,
                        60,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                new ThreadFactoryBuilder()
                        .setNameFormat("future-td-%d")
                        .setDaemon(true)
                        .build(),
                new ThreadPoolExecutor.AbortPolicy()
                        );


    private final static ListeningExecutorService service = MoreExecutors.listeningDecorator(executor);

    public static void submit(){
        ListenableFuture<String> submit = service.submit(() -> {
            Thread.sleep(3000L);
            log.info("我搞掂了");
            return "执行完成";
        });
        submit.addListener(()->{
            log.info("监测事情办完了");
        },MoreExecutors.directExecutor());
        Futures.addCallback(submit, new FutureCallback<>() {
            @Override
            public void onSuccess(String result) {
                log.info("执行成功");
            }

            @Override
            public void onFailure(Throwable t) {
                log.info("执行失败");
            }
        },Executors.newSingleThreadExecutor());
        log.info("看看我什么时候执行");
        try {
            String o = submit.get(4, TimeUnit.SECONDS);
            log.info("等待结果:{}",o);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("异步操作出现异常:{}",e.getMessage());
            submit.cancel(true);
        }
        log.info("看看我什么时候执行结束");
    }
    public static void main(String[] args) throws Exception {
        int value = HttpStatus.OK.value();
        log.info("");
        //submit();
//        CompletableFuture<Boolean> future1 = new CompletableFuture<>();
//        try {
//            CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
//                try {
//                    Thread.sleep(5000L);
//                    //future1.complete(false);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                log.info("hello world");
//                return false;
//            }, executor);
//            future.whenCompleteAsync((r,e)->{
//                boolean cancelled = future.isCancelled();
//                boolean cancelled1 = future1.isCancelled();
//                log.info("执行完成111 | {}",r);
//            },executor);
//            //future.complete(true);
//            //future.cancel(true);
//            Boolean aBoolean = future1.get(3, TimeUnit.SECONDS);
//            log.info("执行完成 | {}",aBoolean);
//            Thread.sleep(5000);
//        } catch (Exception ex){
//            log.info("TimeoutException | {}",ex.getMessage());
//            boolean cancelled = future1.isCancelled();
//            future1.cancel(true);
//            cancelled = future1.isCancelled();
//            log.info("");
//        }
    }
}
