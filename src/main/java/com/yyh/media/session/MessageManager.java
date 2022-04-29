package com.yyh.media.session;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.*;

/**
 * @author: yyh
 * @date: 2022-01-19 10:59
 * @description: MessageManager
 **/
//@Component
@Slf4j
public class MessageManager {

    private final Map<String,SyncFuture<?>> map = new ConcurrentHashMap<>();
    private final Map<String,CompletableFuture<?>> future_map = new ConcurrentHashMap<>();

//    public CompletableFuture<?> create(String key){
//        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(()->{
//
//        });
//    }
}
