package com.yyh.gb28181.handle.response.service;

import com.yyh.gb28181.command.ISipCommander4Platform;
import com.yyh.gb28181.entity.ParentPlatformCache;
import com.yyh.gb28181.service.IRedisCacheStorage;
import com.yyh.web.entity.ParentPlatform;
import com.yyh.web.service.IPlatformService;
import gov.nist.javax.sip.ResponseEventExt;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sip.*;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.WWWAuthenticateHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.text.ParseException;

/**
 * @author: yyh
 * @date: 2022-01-04 16:32
 * @description: SipResponseProcessorService
 **/
@Slf4j
@Service
public class SipResponseProcessorService{


    private final IRedisCacheStorage redisCacheStorage;

    private final ISipCommander4Platform sipCommander4Platform;

    private final IPlatformService platformService;

    public SipResponseProcessorService(IRedisCacheStorage redisCacheStorage,
                                       ISipCommander4Platform sipCommander4Platform,
                                       IPlatformService platformService) {
        this.redisCacheStorage = redisCacheStorage;
        this.sipCommander4Platform = sipCommander4Platform;
        this.platformService = platformService;
    }

    /**
     * 处理invite响应
     * @param evt responseEvent
     */
    public void invite(ResponseEvent evt) {

        try {
            Response response = evt.getResponse();
            int statusCode = response.getStatusCode();
            // trying不会回复
            if (statusCode == Response.TRYING) {

            }
            // 成功响应
            // 下发ack
            if (statusCode == Response.OK) {
                ResponseEventExt event = (ResponseEventExt)evt;
                ClientTransaction clientTransaction = event.getClientTransaction();
                Dialog dialog = clientTransaction.getDialog();
                CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
                Request reqAck = dialog.createAck(cseq.getSeqNumber());
                SipURI requestUri = (SipURI) reqAck.getRequestURI();
                try {
                    requestUri.setHost(event.getRemoteIpAddress());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                requestUri.setPort(event.getRemotePort());
                reqAck.setRequestURI(requestUri);
                log.info("向 " + event.getRemoteIpAddress() + ":" + event.getRemotePort() + "回复ack");
                SipURI sipUri = (SipURI)dialog.getRemoteParty().getURI();
                String deviceId = requestUri.getUser();
                String channelId = sipUri.getUser();
                dialog.sendAck(reqAck);
            }
        } catch (InvalidArgumentException | SipException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理向平台注册
     * @param responseEvent responseEvent
     */
    public void register(ResponseEvent responseEvent) {
        Response response = responseEvent.getResponse();
        CallIdHeader callIdHeader = (CallIdHeader) response.getHeader(CallIdHeader.NAME);
        String callId = callIdHeader.getCallId();

        String platformGbId = redisCacheStorage.queryPlatformRegisterInfo(callId);
        if (platformGbId == null) {
            log.warn(String.format("未找到callId： %s 的注册/注销平台id", callId ));
            return;
        }

        ParentPlatformCache parentPlatformCatch = redisCacheStorage.queryPlatformCacheInfo(platformGbId);
        if (parentPlatformCatch == null) {
            log.warn(String.format("收到 %s 的注册/注销%S请求, 但是平台缓存信息未查询到!!!", platformGbId, response.getStatusCode()));
            return;
        }
        String register = "注册";
        String logout = "注销";
        String action = parentPlatformCatch.getParentPlatform().getExpires() ==0 ? logout : register;
        log.info(String.format("收到 %s %s的%S响应", platformGbId, action, response.getStatusCode() ));
        ParentPlatform parentPlatform = parentPlatformCatch.getParentPlatform();
        if (parentPlatform == null) {
            log.warn(String.format("收到 %s %s的%S请求, 但是平台信息未查询到!!!", platformGbId, action, response.getStatusCode()));
            return;
        }

        if (response.getStatusCode() == Response.UNAUTHORIZED) {
            WWWAuthenticateHeader www = (WWWAuthenticateHeader)response.getHeader(WWWAuthenticateHeader.NAME);
            sipCommander4Platform.register(parentPlatform, callId, www, null, null, true);
        }else if (response.getStatusCode() == Response.OK){
            // 注册/注销成功
            log.info(String.format("%s %s成功", platformGbId, action));
            redisCacheStorage.delPlatformRegisterInfo(callId);
            redisCacheStorage.delPlatformCacheInfo(platformGbId);
            // 取回Expires设置，避免注销过程中被置为0
            ParentPlatform parentPlatformTmp = platformService.queryParentPlatByServerGbId(platformGbId);
            parentPlatformTmp.setStatus(register.equals(action));
            redisCacheStorage.updatePlatformRegister(parentPlatformTmp);
            redisCacheStorage.updatePlatformKeepalive(parentPlatformTmp);
            parentPlatformCatch.setParentPlatform(parentPlatformTmp);
            redisCacheStorage.updatePlatformCacheInfo(parentPlatformCatch);
            platformService.updateParentPlatformStatus(platformGbId, register.equals(action));
//            if (logout.equals(action)) {
//                subscribeHolder.removeCatalogSubscribe(platformGbId);
//                subscribeHolder.removeMobilePositionSubscribe(platformGbId);
//            }
        }

    }
}
