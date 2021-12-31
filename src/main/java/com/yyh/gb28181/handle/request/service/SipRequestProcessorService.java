package com.yyh.gb28181.handle.request.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yyh.gb28181.config.SipServerProperties;
import com.yyh.gb28181.constant.SipRequestConstant;
import com.yyh.gb28181.constant.VideoManagerConstant;
import com.yyh.gb28181.event.online.OnlineEvent;
import com.yyh.gb28181.handle.request.SipRequestProcessorParent;
import com.yyh.gb28181.utils.DigestServerAuthenticationHelper;
import com.yyh.gb28181.utils.Sip28181Date;
import com.yyh.web.entity.SysDevice;
import com.yyh.web.entity.GbDevice;
import com.yyh.web.service.IGbDeviceService;
import com.yyh.web.service.IPlatformService;
import com.yyh.web.service.ISysDeviceService;
import gov.nist.javax.sip.RequestEventExt;
import gov.nist.javax.sip.address.AddressImpl;
import gov.nist.javax.sip.address.SipUri;
import gov.nist.javax.sip.header.Expires;
import gov.nist.javax.sip.header.SIPDateHeader;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.sip.*;
import javax.sip.address.Address;
import javax.sip.header.*;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.time.LocalDateTime;
import java.util.*;

import static com.yyh.gb28181.utils.XmlUtil.getText;

/**
 * @author: yyh
 * @date: 2021-11-24 15:18
 * @description: SipRequestProcessorParent
 **/
@Service
public class SipRequestProcessorService extends SipRequestProcessorParent {

    private final static Logger logger = LoggerFactory.getLogger(SipRequestProcessorService.class);

    @Resource
    private SipServerProperties sipConfig;

    @Resource
    private ISysDeviceService deviceService;

    @Resource
    private IGbDeviceService gb28181Service;

    @Resource
    private IPlatformService platformService;

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    @Resource
    private RedisTemplate<String,String> redisTemplate;

    @Resource
    private ObjectMapper mapper;

    public void register(RequestEvent requestEvent){
        try {
            RequestEventExt evt = (RequestEventExt)requestEvent;
            String requestAddress = evt.getRemoteIpAddress() + ":" + evt.getRemotePort();
            Request request = evt.getRequest();
            Response response = null;
            boolean passwordCorrect = false;
            // 注册标志  0：未携带授权头或者密码错误  1：注册成功   2：注销成功
            int registerFlag = 0;
            FromHeader fromHeader = (FromHeader) request.getHeader(FromHeader.NAME);
            AddressImpl address = (AddressImpl) fromHeader.getAddress();
            SipUri uri = (SipUri) address.getURI();
            String deviceId = uri.getUser();
            logger.info("[{}:{}] 收到注册请求，开始处理 | id:{}",evt.getRemoteIpAddress(),evt.getRemotePort(),deviceId);
            SysDevice one = deviceService.getOne(Wrappers.<SysDevice>lambdaQuery().eq(SysDevice::getDeviceSignid, deviceId));
            GbDevice gb = new GbDevice();
            if (one==null){
                logger.info("[{}:{}]当前设备未授权,需要授权 | id:{}",evt.getRemoteIpAddress(),evt.getRemotePort(),deviceId);
                response = getMessageFactory().createResponse(Response.FORBIDDEN, request);
                response.setReasonPhrase("the device is not registered,please contact the administrator");
                ServerTransaction serverTransaction = getServerTransaction(evt);
                serverTransaction.sendResponse(response);
                if (serverTransaction.getDialog() != null) {
                    serverTransaction.getDialog().delete();
                }
                return;
            }
            AuthorizationHeader authored = (AuthorizationHeader) request.getHeader(AuthorizationHeader.NAME);
            // 校验密码是否正确
            if (authored != null) {
                passwordCorrect = new DigestServerAuthenticationHelper().doAuthenticatePlainTextPassword(request,
                        sipConfig.getPassword());
            }
            if (!StringUtils.hasText(sipConfig.getPassword())){
                passwordCorrect = true;
            }
            // 未携带授权头或者密码错误 回复401
            if (authored == null ) {
                logger.info("[{}] 未携带授权头 回复401", requestAddress);
                response = getMessageFactory().createResponse(Response.UNAUTHORIZED, request);
                new DigestServerAuthenticationHelper().generateChallenge(getHeaderFactory(), response, sipConfig.getDomain());
            }else {
                if (!passwordCorrect){
                    // 注册失败
                    response = getMessageFactory().createResponse(Response.FORBIDDEN, request);
                    response.setReasonPhrase("wrong password");
                    logger.info("[{}] 密码/SIP服务器ID错误, 回复403", requestAddress);
                }else {
                    // 携带授权头并且密码正确
                    response = getMessageFactory().createResponse(Response.OK, request);
                    // 添加date头
                    SIPDateHeader dateHeader = new SIPDateHeader();
                    // 使用自己修改的
                    Sip28181Date sipDate = new Sip28181Date(Calendar.getInstance(Locale.ENGLISH).getTimeInMillis());
                    dateHeader.setDate(sipDate);
                    response.addHeader(dateHeader);

                    ExpiresHeader expiresHeader = (ExpiresHeader) request.getHeader(Expires.NAME);
                    if (expiresHeader == null) {
                        response = getMessageFactory().createResponse(Response.BAD_REQUEST, request);
                        ServerTransaction serverTransaction = getServerTransaction(evt);
                        serverTransaction.sendResponse(response);
                        if (serverTransaction.getDialog() != null) {
                            serverTransaction.getDialog().delete();
                        }
                        return;
                    }
                    // 添加Contact头
                    response.addHeader(request.getHeader(ContactHeader.NAME));
                    // 添加Expires头
                    response.addHeader(request.getExpires());

                    // 获取到通信地址等信息
                    ViaHeader viaHeader = (ViaHeader) request.getHeader(ViaHeader.NAME);
                    String received = viaHeader.getReceived();
                    int rPort = viaHeader.getRPort();
                    // 解析本地地址替代
                    if (StrUtil.isEmpty(received) || rPort == -1) {
                        received = viaHeader.getHost();
                        rPort = viaHeader.getPort();
                    }
                    gb.setGbId(deviceId);
                    gb.setIp(received);
                    gb.setPort(rPort);
                    gb.setHostAddress(received.concat(":").concat(String.valueOf(rPort)));
                    // 注销成功
                    if (expiresHeader.getExpires() == 0) {
                        registerFlag = 2;
                    }
                    // 注册成功
                    else {
                        gb.setExpires(expiresHeader.getExpires());
                        registerFlag = 1;
                        // 判断TCP还是UDP
                        boolean isTcp = false;
                        ViaHeader reqViaHeader = (ViaHeader) request.getHeader(ViaHeader.NAME);
                        String transport = reqViaHeader.getTransport();
                        if (transport.equals(SipRequestConstant.TCP)) {
                            isTcp = true;
                        }
                        gb.setTransport(isTcp ? SipRequestConstant.TCP : SipRequestConstant.UDP);
                    }
                }
            }
            ServerTransaction serverTransaction = getServerTransaction(evt);
            serverTransaction.sendResponse(response);
            if (serverTransaction.getDialog() != null) {
                serverTransaction.getDialog().delete();
            }
            // 注册成功
            // 保存到redis
            // 下发catelog查询目录
            if (registerFlag == 1 ) {
                OnlineEvent onlineEvent = new OnlineEvent(this);
                onlineEvent.setDevice(gb);
                onlineEvent.setFrom(VideoManagerConstant.EVENT_ONLINE_REGISTER);
                applicationEventPublisher.publishEvent(onlineEvent);
                logger.info("[{}] 注册成功! deviceId:" + deviceId, requestAddress);
            } else if (registerFlag == 2) {
                OnlineEvent onlineEvent = new OnlineEvent(this);
                onlineEvent.setId(deviceId);
                onlineEvent.setFrom(VideoManagerConstant.EVENT_ONLINE_OUTLINE);
                applicationEventPublisher.publishEvent(onlineEvent);
                logger.info("[{}] 注销成功! deviceId:" + deviceId, requestAddress);
            }
        }catch (Exception e){
            logger.error("处理注册消息出现异常:{}",e.getMessage(),e);
        }
    }

    /**
     * 心跳信息
     * @param requestEvent requestEvent
     */
    public void messageKeepalive(RequestEvent requestEvent) {
        RequestEventExt evt = (RequestEventExt)requestEvent;
        Request request = requestEvent.getRequest();
        FromHeader header = (FromHeader) request.getHeader(FromHeader.NAME);
        Address address = header.getAddress();
        SipUri uri = (SipUri) address.getURI();
        String deviceId = uri.getUser();
        try {
            OnlineEvent onlineEvent = new OnlineEvent(this);
            onlineEvent.setId(deviceId);
            onlineEvent.setFrom(VideoManagerConstant.EVENT_ONLINE_KEEPALIVE);
            applicationEventPublisher.publishEvent(onlineEvent);
            responseAck(evt, Response.OK);
        }catch (Exception ex){
            logger.error("处理心跳消息出现异常:{}",deviceId+ " | "+ex.getMessage(),ex);
        }

    }

    /**
     * 设备信息
     *
     *
     * @param requestEvent requestEvent
     */
    public void messageResponseDeviceInfo(RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        RequestEventExt evt = (RequestEventExt)requestEvent;
        FromHeader header = (FromHeader) request.getHeader(FromHeader.NAME);
        Address address = header.getAddress();
        SipUri uri = (SipUri) address.getURI();
        String deviceId = uri.getUser();
        GbDevice byId = gb28181Service.getById(deviceId);
        try{
            String charset = byId==null?VideoManagerConstant.DEVICE_DEFAULT_CHARSET:byId.getCharset();
            Element rootElement = getRootElement(evt,charset);
            String requestName = rootElement.getName();
            logger.debug("{}",requestEvent.getRequest());
            if (SipRequestConstant.RESPONSE.equalsIgnoreCase(requestName)){
                if (byId==null){
                    logger.info("设备不存在:{}",deviceId);
                    responseAck(evt, Response.NOT_FOUND, "device id not found");
                    return;
                }
                byId.setName(getText(rootElement, "DeviceName"));
                byId.setManufacturer(getText(rootElement, "Manufacturer"));
                byId.setModel(getText(rootElement, "Model"));
                byId.setFirmware(getText(rootElement, "Firmware"));
                if (StrUtil.isBlank(byId.getStreamMode())) {
                    byId.setStreamMode(SipRequestConstant.UDP);
                }
                byId.setUpdateTime(LocalDateTime.now());
                gb28181Service.updateById(byId);
                responseAck(evt, Response.OK);
            }else {
                logger.info("接收到设备信息,根节点为:{}",requestName);
            }
        }catch (Exception ex){
            logger.error("DeviceInfo handle has error {} | {}",deviceId,ex.getMessage(),ex);
        }
    }
}




