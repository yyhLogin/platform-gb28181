package com.yyh.gb28181.handle.request.processor;

import com.yyh.gb28181.annotation.SipRequestMapping;
import com.yyh.gb28181.annotation.SipRequestProcess;
import com.yyh.gb28181.constant.SipRequestConstant;
import com.yyh.gb28181.handle.request.service.SipRequestProcessorService;
import com.yyh.gb28181.handle.request.service.message.SipMessageRequestProcessor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.sip.RequestEvent;

/**
 * @author: yyh
 * @date: 2021-11-24 15:16
 * @description: RegisterRequestProcessor
 **/
@Slf4j
@SipRequestMapping
public class RequestProcessor{

    @Resource
    private SipRequestProcessorService sipService;

    @Resource
    private SipMessageRequestProcessor messageService;

    /**
     * 注册
     * @param requestEvent requestEvent
     */
    @SipRequestProcess(method = SipRequestConstant.REGISTER)
    public void register(RequestEvent requestEvent){
        if (requestEvent==null){
            return;
        }
        sipService.register(requestEvent);
    }

    /**
     * keepalive 心跳
     * @param requestEvent requestEvent
     */
    @SipRequestProcess(method = SipRequestConstant.MESSAGE_NOTIFY_KEEPALIVE)
    public void messageKeepalive(RequestEvent requestEvent){
        sipService.messageKeepalive(requestEvent);
    }

    /**
     * device info 设备信息
     * @param requestEvent requestEvent
     */
    @SipRequestProcess(method = SipRequestConstant.MESSAGE_RESPONSE_DEVICEINFO)
    public void messageDeviceInfo(RequestEvent requestEvent){
        sipService.messageResponseDeviceInfo(requestEvent);
    }

    /**
     * Catalog 设备通道信息
     * @param requestEvent requestEvent
     */
    @SipRequestProcess(method = SipRequestConstant.MESSAGE_RESPONSE_CATALOG)
    public void messageCatalog(RequestEvent requestEvent){
        messageService.messageResponseCatalog(requestEvent);
    }
}
