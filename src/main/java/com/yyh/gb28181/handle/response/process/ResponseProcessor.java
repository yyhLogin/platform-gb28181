package com.yyh.gb28181.handle.response.process;

import com.yyh.gb28181.annotation.SipMapping;
import com.yyh.gb28181.annotation.SipProcess;
import com.yyh.gb28181.constant.SipRequestConstant;
import com.yyh.gb28181.handle.request.service.SipRequestProcessorService;
import com.yyh.gb28181.handle.response.service.SipResponseProcessorService;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.message.Request;

/**
 * @author: yyh
 * @date: 2022-01-04 16:30
 * @description: ResponseProcessor
 **/
@Slf4j
@SipMapping(SipRequestConstant.RESPONSE)
public class ResponseProcessor {



    @Resource
    private SipResponseProcessorService sipService;

    /**
     * 注册
     * @param responseEvent responseEvent
     */
    @SipProcess(method = Request.INVITE)
    public void invite(ResponseEvent responseEvent){
        if (responseEvent==null){
            return;
        }
        sipService.invite(responseEvent);
    }


    /**
     * 平台注册
     * @param responseEvent responseEvent
     */
    @SipProcess(method = Request.REGISTER)
    public void register(ResponseEvent responseEvent){
        if (responseEvent==null){
            return;
        }
        sipService.register(responseEvent);
    }




}
