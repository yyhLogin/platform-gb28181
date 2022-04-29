package com.yyh.gb28181.handle.request.service;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yyh.common.constant.CommonResultConstants;
import com.yyh.common.utils.CommonResult;
import com.yyh.gb28181.command.ISipCommander4Platform;
import com.yyh.gb28181.command.impl.SipCommander;
import com.yyh.gb28181.config.SipServerProperties;
import com.yyh.gb28181.constant.SipRequestConstant;
import com.yyh.gb28181.constant.VideoManagerConstant;
import com.yyh.gb28181.utils.*;
import com.yyh.media.callback.DeferredResultHandle;
import com.yyh.media.callback.RecordInfoDeferredHandle;
import com.yyh.media.entity.RecordInfo;
import com.yyh.media.entity.RecordItem;
import com.yyh.web.entity.*;
import com.yyh.gb28181.event.online.OnlineEvent;
import com.yyh.gb28181.handle.request.SipRequestProcessorParent;
import com.yyh.web.service.*;
import gov.nist.javax.sdp.TimeDescriptionImpl;
import gov.nist.javax.sdp.fields.TimeField;
import gov.nist.javax.sip.RequestEventExt;
import gov.nist.javax.sip.address.AddressImpl;
import gov.nist.javax.sip.address.SipUri;
import gov.nist.javax.sip.header.Expires;
import gov.nist.javax.sip.header.SIPDateHeader;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;

import javax.annotation.Resource;
import javax.sdp.*;
import javax.sip.*;
import javax.sip.address.Address;
import javax.sip.address.SipURI;
import javax.sip.header.*;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.yyh.gb28181.utils.XmlUtil.getText;

/**
 * @author: yyh
 * @date: 2021-11-24 15:18
 * @description: SipRequestProcessorParent
 **/
@Service
public class SipRequestProcessorService extends SipRequestProcessorParent {

    private final static Logger logger = LoggerFactory.getLogger(SipRequestProcessorService.class);

    public static volatile List<String> threadNameList = new ArrayList<>();
    private final static String CACHE_RECORDINFO_KEY = "CACHE_RECORDINFO_";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    @Resource
    private SipServerProperties sipConfig;

    @Resource
    private ISysDeviceService deviceService;

    @Resource
    private IGbDeviceService gb28181Service;

    @Resource
    private IPlatformService platformService;

    @Resource
    private IGbDeviceChannelService channelService;

    @Resource
    private ISysLocationService sysLocationService;

    @Resource
    private ApplicationEventPublisher applicationEventPublisher;

    @Resource
    private RedisTemplate<String,String> redisTemplate;

    @Resource
    private ObjectMapper mapper;

    @Resource
    private RecordInfoDeferredHandle recordInfoDeferredHandle;

    @Resource
    private SipCommander sipCommander;

    @Resource
    private ISysAlarmService sysAlarmService;

    @Resource
    private DeferredResultHandle deferredResultHandle;

    @Resource
    private ISipCommander4Platform sipCommander4Platform;

    @Resource
    private IPlatformChannelService platformChannelService;

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
            GbDevice gb = new GbDevice();
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

    /**
     * 录像文件查询
     * @param requestEvent requestEvent
     */
    public void messageResponseRecordInfo(RequestEvent requestEvent) {
        logger.info("message->{}",requestEvent.getRequest());
        Request request = requestEvent.getRequest();
        RequestEventExt evt = (RequestEventExt)requestEvent;
        FromHeader header = (FromHeader) request.getHeader(FromHeader.NAME);
        Address address = header.getAddress();
        SipUri uri = (SipUri) address.getURI();
        String deviceId = uri.getUser();
        GbDevice byId = gb28181Service.getById(deviceId);
        // 回复200 OK
        try {
            responseAck(evt, Response.OK);

            Element rootElement = getRootElement(evt, byId.getCharset());
            String uuid = UUID.randomUUID().toString().replace("-", "");
            RecordInfo recordInfo = new RecordInfo();
            String sn = getText(rootElement, "SN");
            String key = DeferredResultHandle.CALLBACK_CMD_RECORD_INFO + byId.getGbId() + sn;
            recordInfo.setDeviceId(byId.getGbId());
            recordInfo.setSn(sn);
            recordInfo.setName(getText(rootElement, "Name"));
            if (getText(rootElement, "SumNum") == null || getText(rootElement, "SumNum") == "") {
                recordInfo.setSumNum(0);
            } else {
                recordInfo.setSumNum(Integer.parseInt(getText(rootElement, "SumNum")));
            }
            Element recordListElement = rootElement.element("RecordList");
            if (recordListElement == null || recordInfo.getSumNum() == 0) {
                logger.info("无录像数据");
                CommonResult<RecordInfo> commonResult = CommonResult.success();
                recordInfoDeferredHandle.invokeHandle(key,sn,commonResult);
            } else {
                Iterator<Element> recordListIterator = recordListElement.elementIterator();
                List<RecordItem> recordList = new ArrayList<RecordItem>();
                if (recordListIterator != null) {
                    RecordItem record = new RecordItem();
                    logger.info("处理录像列表数据...");
                    // 遍历DeviceList
                    while (recordListIterator.hasNext()) {
                        Element itemRecord = recordListIterator.next();
                        Element recordElement = itemRecord.element("DeviceID");
                        if (recordElement == null) {
                            logger.info("记录为空，下一个...");
                            continue;
                        }
                        record = new RecordItem();
                        record.setDeviceId(getText(itemRecord, "DeviceID"));
                        record.setName(getText(itemRecord, "Name"));
                        record.setFilePath(getText(itemRecord, "FilePath"));
                        record.setAddress(getText(itemRecord, "Address"));
                        record.setStartTime(formatter.format(LocalDateTime.parse(getText(itemRecord, "StartTime"),dtf)));
                        record.setEndTime(formatter.format(LocalDateTime.parse(getText(itemRecord, "EndTime"),dtf)));
                        record.setSecrecy(itemRecord.element("Secrecy") == null ? 0
                                : Integer.parseInt(getText(itemRecord, "Secrecy")));
                        record.setType(getText(itemRecord, "Type"));
                        record.setRecorderId(getText(itemRecord, "RecorderID"));
                        recordList.add(record);
                    }
                    recordInfo.setRecordList(recordList);
                }

                // 改用单独线程统计已获取录像文件数量，避免多包并行分别统计不完整的问题
                String cacheKey = CACHE_RECORDINFO_KEY + byId.getGbId() + sn;
                redisTemplate.opsForValue().set(cacheKey + "_" + uuid, mapper.writeValueAsString(recordList), 90, TimeUnit.SECONDS);
                if (!threadNameList.contains(cacheKey)) {
                    threadNameList.add(cacheKey);
                    CheckForAllRecordsThread chk = new CheckForAllRecordsThread(cacheKey, recordInfo,sn);
                    chk.setName(cacheKey);
                    chk.setRecordInfoDeferredHandle(recordInfoDeferredHandle);
                    chk.setRedis(redisTemplate);
                    chk.setMapper(mapper);
                    chk.setLogger(logger);
                    chk.start();
                    if (logger.isDebugEnabled()) {
                        logger.debug("Start Thread " + cacheKey + ".");
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Thread " + cacheKey + " already started.");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 位置信息上报
     * @param requestEvent requestEvent
     */
    public void messageMobilePosition(RequestEvent requestEvent){
        Request request = requestEvent.getRequest();
        FromHeader header = (FromHeader) request.getHeader(FromHeader.NAME);
        RequestEventExt evt = (RequestEventExt)requestEvent;
        Address address = header.getAddress();
        SipUri uri = (SipUri) address.getURI();
        String deviceId = uri.getUser();
        GbDevice byId = gb28181Service.getById(deviceId);
        if (byId==null){
            logger.warn("设备不存在:{}",deviceId);
            return;
        }
        try {
            String charset = StrUtil.isNotBlank(byId.getCharset()) ? VideoManagerConstant.DEVICE_DEFAULT_CHARSET:byId.getCharset();
            Element rootElement = getRootElement(evt,charset);
            ///String requestName = rootElement.getName();
            logger.debug("{}",requestEvent.getRequest());
            SysLocation location = buildSysLocation(rootElement, byId);
            sysLocationService.save(location);
            redisTemplate.opsForValue().set("last_location:"+location.getImei(),mapper.writeValueAsString(location));
            responseAck(evt, Response.OK);
        }catch (Exception ex){
            logger.error("MobilePosition handle has error {} | {}",deviceId,ex.getMessage(),ex);
        }
    }

    /**
     * 视频回放完成
     * @param requestEvent requestEvent
     */
    public void messageNotifyMediaStatus(RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        //logger.info("request -> {}",request);
        FromHeader header = (FromHeader) request.getHeader(FromHeader.NAME);
        RequestEventExt evt = (RequestEventExt)requestEvent;
        Address address = header.getAddress();
        SipUri uri = (SipUri) address.getURI();
        String host = evt.getRemoteIpAddress();
        int port = evt.getRemotePort();
        String user = uri.getUser();
        String deviceId = null;
        String channelId = null;
        GbDevice byId = gb28181Service.getDeviceByHostAndPort(host, port);
        if (!byId.getGbId().equals(user)){
            deviceId = byId.getGbId();
            channelId = user;
        }else {
            channelId = byId.getGbId();
            // TODO 可能会出现设备编号和通道编号不一致的现象
        }
        try {
            String charset = StrUtil.isBlank(byId.getCharset())?VideoManagerConstant.DEVICE_DEFAULT_CHARSET:byId.getCharset();
            Element rootElement = getRootElement(evt,charset);
            responseAck(evt, Response.OK);
            String notifyType =getText(rootElement, "NotifyType");
            if (VideoManagerConstant.PLAYBACK_STOP_STATUS.equals(notifyType)){
                logger.info("媒体播放完毕，通知关流");
                sipCommander.streamByeCmd(byId.getGbId(), channelId);
            }
        }catch (Exception ex){
            logger.error("MediaStatus handle has error {} | {}",deviceId,ex.getMessage(),ex);
        }
    }

    /**
     * 处理订阅上报的位置信息
     * @param requestEvent requestEvent
     */
    public void notifyMobilePosition(RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        RequestEventExt evt = (RequestEventExt)requestEvent;
        FromHeader header = (FromHeader) request.getHeader(FromHeader.NAME);
        Address address = header.getAddress();
        SipUri uri = (SipUri) address.getURI();
        String deviceId = uri.getUser();
        try {
            GbDevice device = gb28181Service.getById(deviceId);
            if (device==null){
                logger.warn("设备不存在:{}",deviceId);
                return;
            }
            Element rootElement = getRootElement(requestEvent, device.getCharset());
            SysLocation location = buildSysLocation(rootElement, device);
            sysLocationService.save(location);
            redisTemplate.opsForValue().set("last_location:"+location.getImei(),mapper.writeValueAsString(location));
            responseAck(evt, Response.OK);
        }catch (DocumentException | JsonProcessingException | SipException | InvalidArgumentException | ParseException ex){
            logger.error("notify MobilePosition handle has error {} | {} ",deviceId,ex.getMessage(),ex);
        }
    }

    /**
     * 处理报警通知
     * @param requestEvent requestEvent
     */
    public void notifyAlarm(RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        RequestEventExt evt = (RequestEventExt)requestEvent;
        FromHeader header = (FromHeader) request.getHeader(FromHeader.NAME);
        Address address = header.getAddress();
        SipUri uri = (SipUri) address.getURI();
        String deviceId = uri.getUser();
        try {
            GbDevice device = gb28181Service.getById(deviceId);
            if (device==null){
                logger.warn("设备不存在:{}",deviceId);
                return;
            }
            Element rootElement = getRootElement(requestEvent, device.getCharset());
            SysAlarm sysAlarm = new SysAlarm();
            sysAlarm.setDeviceId(device.getGbId());
            String alarmPriority = getText(rootElement, "AlarmPriority");
            sysAlarm.setAlarmLevel(alarmPriority);
            String alarmDescription = getText(rootElement, "AlarmDescription");
            if (StrUtil.isNotBlank(alarmDescription)){
                sysAlarm.setAlarmDescription(alarmDescription);
            }
            String alarmMethod = getText(rootElement, "AlarmMethod");
            sysAlarm.setAlarmMethod(alarmMethod);
            String alarmTime = getText(rootElement, "AlarmTime");
            sysAlarm.setAlarmTime(LocalDateTime.parse(alarmTime,dtf));
            String longitude = getText(rootElement, "Longitude");
            if (NumberUtil.isDouble(longitude)){
                sysAlarm.setLongitude(Double.parseDouble(longitude));
            }
            String latitude = getText(rootElement, "Latitude");
            if (NumberUtil.isDouble(latitude)){
                sysAlarm.setLatitude(Double.parseDouble(latitude));
            }
            sysAlarmService.save(sysAlarm);
        }catch (DocumentException ex){
            logger.error("notify alarm handle has error {} | {} ",deviceId,ex.getMessage(),ex);
        }
    }

    /**
     * 报警信息查询
     * @param requestEvent requestEvent
     */
    public void messageResponseAlarm(RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        RequestEventExt evt = (RequestEventExt)requestEvent;
        FromHeader header = (FromHeader) request.getHeader(FromHeader.NAME);
        Address address = header.getAddress();
        SipUri uri = (SipUri) address.getURI();
        String deviceId = uri.getUser();
        try {
            GbDevice device = gb28181Service.getById(deviceId);
            if (device==null){
                logger.warn("设备不存在:{}",deviceId);
                return;
            }
            Element rootElement = getRootElement(requestEvent, device.getCharset());
            Element deviceIdElement = rootElement.element("DeviceID");
            String sn = getText(rootElement, "SN");
            String channelId = deviceIdElement.getText();
            String key = DeferredResultHandle.CALLBACK_CMD_ALARM + device.getGbId() + channelId;
            JSONObject json = new JSONObject();
            XmlUtil.node2Json(rootElement, json);
            CommonResult<String> commonResult = CommonResult.success(json.toJSONString());
            deferredResultHandle.invokeResult(key,sn,commonResult);
        }catch (DocumentException ex) {
            logger.error("message alarm handle has error {} | {} ",deviceId,ex.getMessage(),ex);
        }
    }

    /**
     * 设备配置查询响应
     * @param requestEvent requestEvent
     */
    public void messageResponseConfigDownload(RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        ///logger.info("request ->{}",request);
        FromHeader header = (FromHeader) request.getHeader(FromHeader.NAME);
        Address address = header.getAddress();
        SipUri uri = (SipUri) address.getURI();
        String deviceId = uri.getUser();
        try {
            GbDevice device = gb28181Service.getById(deviceId);
            if (device==null){
                logger.warn("设备不存在:{}",deviceId);
                return;
            }
            Element rootElement = getRootElement(requestEvent, device.getCharset());
            Element deviceIdElement = rootElement.element("DeviceID");
            String key = DeferredResultHandle.CALLBACK_CMD_CONFIG_DOWNLOAD + device.getGbId();
            JSONObject json = new JSONObject();
            XmlUtil.node2Json(rootElement, json);
            CommonResult<String> commonResult = CommonResult.success(json.toJSONString());
            deferredResultHandle.invokeAllResult(key,commonResult);
        }catch (DocumentException ex){
            logger.error("message config download handle has error {} | {} ",deviceId,ex.getMessage(),ex);
        }
    }

    /**
     * 设备配置设定响应
     * @param requestEvent requestEvent
     */
    public void messageResponseDeviceConfig(RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        ///logger.info("request ->{}",request);
        FromHeader header = (FromHeader) request.getHeader(FromHeader.NAME);
        Address address = header.getAddress();
        SipUri uri = (SipUri) address.getURI();
        String deviceId = uri.getUser();
        try {
            GbDevice device = gb28181Service.getById(deviceId);
            if (device==null){
                logger.warn("设备不存在:{}",deviceId);
                return;
            }
            Element rootElement = getRootElement(requestEvent, device.getCharset());
            String sn = getText(rootElement, "SN");
            String channelId = getText(rootElement, "DeviceID");
            String key = DeferredResultHandle.CALLBACK_CMD_DEVICE_CONFIG + device.getGbId()+channelId;
            JSONObject json = new JSONObject();
            XmlUtil.node2Json(rootElement, json);
            CommonResult<String> commonResult = CommonResult.success(json.toJSONString());
            deferredResultHandle.invokeResult(key,sn,commonResult);
        }catch (DocumentException ex){
            logger.error("message config download handle has error {} | {} ",deviceId,ex.getMessage(),ex);
        }
    }

    /**
     * 设备控制响应
     * @param requestEvent requestEvent
     */
    public void messageResponseDeviceControl(RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        RequestEventExt evt = (RequestEventExt)requestEvent;
        logger.info("request ->{}",request);
        FromHeader header = (FromHeader) request.getHeader(FromHeader.NAME);
        Address address = header.getAddress();
        SipUri uri = (SipUri) address.getURI();
        String deviceId = uri.getUser();
        // 此处是对本平台发出DeviceControl指令的应答
        try {
            responseAck(evt, Response.OK);
            GbDevice device = gb28181Service.getById(deviceId);
            if (device==null){
                logger.warn("设备不存在:{}",deviceId);
                return;
            }
            Element rootElement = getRootElement(requestEvent, device.getCharset());
            JSONObject json = new JSONObject();
            String channelId = getText(rootElement, "DeviceID");
            String sn = getText(rootElement, "SN");
            XmlUtil.node2Json(rootElement, json);
            if (logger.isDebugEnabled()) {
                logger.debug(json.toJSONString());
            }
            String key = DeferredResultHandle.CALLBACK_CMD_DEVICE_CONTROL +  device.getGbId() + channelId;
            CommonResult<String> commonResult = CommonResult.<String>builder()
                    .code(CommonResultConstants.SUCCESS)
                    .data(json.toJSONString())
                    .build();
            deferredResultHandle.invokeResult(key,sn,commonResult);
        } catch (SipException | InvalidArgumentException | ParseException | DocumentException e) {
            logger.error("message config device control has error {} | {} ",deviceId,e.getMessage(),e);
        }
    }

    /**
     * 查询设备预置位响应
     * @param requestEvent requestEvent
     */
    public void messageResponsePresetquery(RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        RequestEventExt evt = (RequestEventExt)requestEvent;
        FromHeader header = (FromHeader) request.getHeader(FromHeader.NAME);
        Address address = header.getAddress();
        SipUri uri = (SipUri) address.getURI();
        String deviceId = uri.getUser();
        try{
            responseAck(evt, Response.OK);
            GbDevice device = gb28181Service.getById(deviceId);
            if (device==null){
                logger.warn("设备不存在:{}",deviceId);
                return;
            }
            Element rootElement = getRootElement(requestEvent, device.getCharset());
            String channelId = getText(rootElement, "DeviceID");
            Document document = cn.hutool.core.util.XmlUtil.readXML(new ByteArrayInputStream(request.getRawContent()));
            Map<String, Object> map = cn.hutool.core.util.XmlUtil.xmlToMap(document);
            String key = DeferredResultHandle.CALLBACK_CMD_PRESET_QUERY  + channelId;
            CommonResult<Map<String,Object>> commonResult = CommonResult.<Map<String,Object>>builder()
                    .code(CommonResultConstants.SUCCESS)
                    .data(map)
                    .build();
            deferredResultHandle.invokeAllResult(key,commonResult);
        }catch (SipException | InvalidArgumentException | ParseException | DocumentException e){
            logger.error("message preset query has error {} | {} ",deviceId,e.getMessage(),e);
        }
    }

    /**
     * 上级平台查询设备信息
     * @param requestEvent requestEvent
     */
    public void messageQueryDeviceInfo(RequestEvent requestEvent) {
        FromHeader fromHeader = (FromHeader) requestEvent.getRequest().getHeader(FromHeader.NAME);
        Address address = fromHeader.getAddress();
        SipUri uri = (SipUri) address.getURI();
        String deviceId = uri.getUser();
        try {
            // 回复200 OK
            responseAck(requestEvent, Response.OK);
            ParentPlatform parentPlatform = platformService.queryParentPlatByServerGbId(deviceId);
            if (parentPlatform==null){
                logger.warn("上级平台不存在:{}",deviceId);
                return;
            }
            Element rootElement = getRootElement(requestEvent, parentPlatform.getCharacterSet());
            String sn = rootElement.element("SN").getText();
            sipCommander4Platform.deviceInfoResponse(parentPlatform,sn,fromHeader.getTag());
        } catch (SipException | InvalidArgumentException | ParseException | DocumentException e) {
            logger.error("message query device info has error {} | {}",e.getMessage(),e);
        }
    }

    /**
     * 上级平台查询catalog信息
     * @param requestEvent requestEvent
     */
    public void messageQueryCatalog(RequestEvent requestEvent) {
        logger.info("request:\r\n{}",requestEvent.getRequest());
        FromHeader fromHeader = (FromHeader) requestEvent.getRequest().getHeader(FromHeader.NAME);
        String userId = SipUtils.getUserIdFromFromHeader(requestEvent.getRequest());
        try {
            responseAck(requestEvent, Response.OK);
            ParentPlatform parentPlatform = platformService.queryParentPlatByServerGbId(userId);
            if (parentPlatform==null){
                logger.warn("上级平台不存在:{}",userId);
                return;
            }
            //查询给该平台共享了多少通道
            List<DeviceChannel> list = channelService.queryByPlatformId(parentPlatform.getServerGbId());

            Element rootElement = getRootElement(requestEvent, parentPlatform.getCharacterSet());
            String sn = rootElement.element("SN").getText();
            if(list.size()>0){
                sipCommander4Platform.catalogResponse(list,parentPlatform,sn,fromHeader.getTag());
            }
        } catch (SipException | InvalidArgumentException | ParseException | DocumentException e) {
            logger.error("message query catalog has error {} | {}",e.getMessage(),e);
        }
    }

    /**
     * 平台点播
     * @param requestEvent requestEvent
     */
    public void messageInvite(RequestEvent requestEvent) {
        try {
            Request request = requestEvent.getRequest();
            logger.info("request:\r\n{}",request);
            SipURI sipUri = (SipURI) request.getRequestURI();
            //从subject读取channelId,不再从request-line读取。 有些平台request-line是平台国标编码，不是设备国标编码。
            String channelId = SipUtils.getChannelIdFromHeader(request);
            String userId = SipUtils.getUserIdFromFromHeader(request);
            CallIdHeader callIdHeader = (CallIdHeader)request.getHeader(CallIdHeader.NAME);
            if (userId == null || channelId == null) {
                logger.warn("无法从FromHeader的Address中获取到平台id，返回400");
                // 参数不全， 发400，请求错误
                responseAck(requestEvent, Response.BAD_REQUEST);
            }
            ParentPlatform parentPlatform = platformService.queryParentPlatByServerGbId(userId);
            if(parentPlatform==null){
                // 平台不存在，需要判断是否是设备语音请求
                inviteFromDeviceHandle(requestEvent,userId);
            }else {
                DeviceChannel channel = platformChannelService.queryChannelByPlatformIdAndChannelId(userId,channelId);
                if (channel==null){
                    logger.warn("通道不存在，返回404 | {}-{}",userId, channelId);
                    // 通道不存在，发404，资源不存在
                    responseAck(requestEvent, Response.NOT_FOUND);
                    return;
                }
                //判断通道是否在线
                Boolean aBoolean = redisTemplate.hasKey(VideoManagerConstant.DEVICE_ONLINE + channel.getDeviceId());
                if (aBoolean==null||!aBoolean){
                    logger.warn("通道离线，返回400 | {}-{}",userId, channelId);
                    responseAck(requestEvent, Response.BAD_REQUEST, "channel [" + channel.getChannelId() + "] offline");
                    return;
                }
                // 通道存在，发181，呼叫转接中
                responseAck(requestEvent, Response.CALL_IS_BEING_FORWARDED);
                // 解析sdp消息, 使用jainsip 自带的sdp解析方式
                String contentString = new String(request.getRawContent());

                // jainSip不支持y=字段， 移除以解析。
                int ssrcIndex = contentString.indexOf("y=");
                // 检查是否有y字段
                String ssrcDefault = "0000000000";
                String ssrc;
                SessionDescription sdp;
                if (ssrcIndex >= 0) {
                    //ssrc规定长度为10字节，不取余下长度以避免后续还有“f=”字段
                    ssrc = contentString.substring(ssrcIndex + 2, ssrcIndex + 12);
                    String substring = contentString.substring(0, contentString.indexOf("y="));
                    sdp = SdpFactory.getInstance().createSessionDescription(substring);
                }else {
                    ssrc = ssrcDefault;
                    sdp = SdpFactory.getInstance().createSessionDescription(contentString);
                }
                String sessionName = sdp.getSessionName().getValue();

                Long startTime = null;
                Long stopTime = null;
                Date start = null;
                Date end = null;
                if (sdp.getTimeDescriptions(false) != null && sdp.getTimeDescriptions(false).size() > 0) {
                    TimeDescriptionImpl timeDescription = (TimeDescriptionImpl)(sdp.getTimeDescriptions(false).get(0));
                    TimeField startTimeFiled = (TimeField)timeDescription.getTime();
                    startTime = startTimeFiled.getStartTime();
                    stopTime = startTimeFiled.getStopTime();

                    start = new Date(startTime*1000);
                    end = new Date(stopTime*1000);
                }
                //  获取支持的格式
                Vector mediaDescriptions = sdp.getMediaDescriptions(true);
                // 查看是否支持PS 负载96
                //String ip = null;
                int port = -1;
                boolean mediaTransmissionTCP = false;
                Boolean tcpActive = null;
                for (Object description : mediaDescriptions) {
                    MediaDescription mediaDescription = (MediaDescription) description;
                    Media media = mediaDescription.getMedia();

                    Vector mediaFormats = media.getMediaFormats(false);
                    if (mediaFormats.contains("96")) {
                        port = media.getMediaPort();
                        String protocol = media.getProtocol();

                        // 区分TCP发流还是udp， 当前默认udp
                        if ("TCP/RTP/AVP".equals(protocol)) {
                            String setup = mediaDescription.getAttribute("setup");
                            if (setup != null) {
                                mediaTransmissionTCP = true;
                                if ("active".equals(setup)) {
                                    tcpActive = true;
                                    // 不支持tcp主动
                                    // 目录不支持点播
                                    responseAck(requestEvent, Response.NOT_IMPLEMENTED, "tcp active not support");
                                    return;
                                } else if ("passive".equals(setup)) {
                                    tcpActive = false;
                                }
                            }
                        }
                        break;
                    }
                }
                if (port == -1) {
                    logger.info("不支持的媒体格式，返回415");
                    // 回复不支持的格式
                    responseAck(requestEvent, Response.UNSUPPORTED_MEDIA_TYPE); // 不支持的格式，发415
                    return;
                }
                logger.info("");
            }


        } catch (SipException | InvalidArgumentException | ParseException | SdpException e) {
            logger.error("[ sip client ] 推流失败:{}",e.getMessage(),e);
        }
    }

    public void inviteFromDeviceHandle(RequestEvent evt, String requesterId) throws InvalidArgumentException, ParseException, SipException, SdpException {

        // 非上级平台请求，查询是否设备请求（通常为接收语音广播的设备）
        //Device device = redisCatchStorage.getDevice(requesterId);
        GbDevice device = gb28181Service.getById(requesterId);
        Request request = evt.getRequest();
        if (device != null) {
            logger.info("收到设备" + requesterId + "的语音广播Invite请求");
            responseAck(evt, Response.TRYING);

            String contentString = new String(request.getRawContent());
            // jainSip不支持y=字段， 移除移除以解析。
            String substring = contentString;
            String ssrc = "0000000404";
            int ssrcIndex = contentString.indexOf("y=");
            if (ssrcIndex > 0) {
                substring = contentString.substring(0, ssrcIndex);
                ssrc = contentString.substring(ssrcIndex + 2, ssrcIndex + 12);
            }
            ssrcIndex = substring.indexOf("f=");
            if (ssrcIndex > 0) {
                substring = contentString.substring(0, ssrcIndex);
            }
            SessionDescription sdp = SdpFactory.getInstance().createSessionDescription(substring);

            //  获取支持的格式
            Vector mediaDescriptions = sdp.getMediaDescriptions(true);
            // 查看是否支持PS 负载96
            int port = -1;
            //boolean recvonly = false;
            boolean mediaTransmissionTCP = false;
            Boolean tcpActive = null;
            for (int i = 0; i < mediaDescriptions.size(); i++) {
                MediaDescription mediaDescription = (MediaDescription)mediaDescriptions.get(i);
                Media media = mediaDescription.getMedia();

                Vector mediaFormats = media.getMediaFormats(false);
                if (mediaFormats.contains("8")) {
                    port = media.getMediaPort();
                    String protocol = media.getProtocol();
                    // 区分TCP发流还是udp， 当前默认udp
                    if ("TCP/RTP/AVP".equals(protocol)) {
                        String setup = mediaDescription.getAttribute("setup");
                        if (setup != null) {
                            mediaTransmissionTCP = true;
                            if ("active".equals(setup)) {
                                tcpActive = true;
                            } else if ("passive".equals(setup)) {
                                tcpActive = false;
                            }
                        }
                    }
                    break;
                }
            }
            if (port == -1) {
                logger.info("不支持的媒体格式，返回415");
                // 回复不支持的格式
                responseAck(evt, Response.UNSUPPORTED_MEDIA_TYPE); // 不支持的格式，发415
                return;
            }
            String username = sdp.getOrigin().getUsername();
            String addressStr = sdp.getOrigin().getAddress();
            logger.info("设备{}请求语音流，地址：{}:{}，ssrc：{}", username, addressStr, port, ssrc);

        } else {
            logger.warn("来自无效设备/平台的请求");
            responseAck(evt, Response.BAD_REQUEST);
        }
    }


    /**
     * 构建gps信息
     * @param rootElement xml数据
     * @param device 设备信息
     * @return SysLocation gps信息
     */
    private SysLocation buildSysLocation(Element rootElement,GbDevice device){
        SysLocation location = new SysLocation();
        location.setImei(device.getGbId());
        String time = getText(rootElement, "Time").replace("T"," ");
        location.setTime(LocalDateTime.parse(time, formatter));
        location.setLongitude(Double.parseDouble(getText(rootElement, "Longitude")));
        location.setLatitude(Double.parseDouble(getText(rootElement, "Latitude")));
        String speed = getText(rootElement, "Speed");
        boolean numeric = StrUtil.isNumeric(speed);
        if (numeric) {
            location.setSpeed(Float.parseFloat(speed));
        } else {
            location.setSpeed(0F);
        }
        String direction = getText(rootElement, "Direction");
        numeric = StrUtil.isNumeric(direction);
        if (numeric) {
            location.setBearing(Float.parseFloat(direction));
        } else {
            location.setBearing(0F);
        }
        String altitude = getText(rootElement, "Altitude");
        numeric = StrUtil.isNumeric(altitude);
        if (numeric) {
            location.setAltitude(Float.parseFloat(altitude));
        } else {
            location.setAltitude(0F);
        }
        location.setUploadTime(LocalDateTime.now());
        return location;
    }



}




