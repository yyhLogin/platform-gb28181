package com.yyh.gb28181.handle;

import org.springframework.web.method.HandlerMethod;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author: yyh
 * @date: 2021-11-26 11:18
 * @description: SipMappingRegistry
 **/
public class SipMappingRegistry {

    private final Set<String> supportedMsgTypes = new HashSet<>();
    private final Map<String, HandlerMethod> mapping = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();


    public SipMappingRegistry(){

    }

    public Map<String, HandlerMethod> getHandlerMethodMapping() {
        return Collections.unmodifiableMap(mapping);
    }

    public void register(String type,HandlerMethod method){
    }
}
