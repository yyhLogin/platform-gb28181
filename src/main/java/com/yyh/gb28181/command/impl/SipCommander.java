package com.yyh.gb28181.command.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yyh.config.ConverterType;
import com.yyh.gb28181.callback.EventResult;
import com.yyh.gb28181.callback.SipCallback;
import com.yyh.gb28181.callback.SipSubscribe;
import com.yyh.gb28181.command.ISipCommander;
import com.yyh.gb28181.command.SipRequestHeaderProvider;
import com.yyh.gb28181.config.SipServerProperties;
import com.yyh.gb28181.constant.SipRequestConstant;
import com.yyh.gb28181.utils.SipContentHelper;
import com.yyh.media.component.SsrcManagement;
import com.yyh.media.constants.MediaConstant;
import com.yyh.media.entity.SsrcInfo;
import com.yyh.media.session.SsrcTransaction;
import com.yyh.media.session.StreamSessionManager;
import com.yyh.media.subscribe.HookEvent;
import com.yyh.media.subscribe.PlayHookSubscribe;
import com.yyh.properties.GlobalConfigProperties;
import com.yyh.web.entity.GbDevice;
import com.yyh.web.entity.MediaServer;
import com.yyh.web.service.IMediaServerService;
import gov.nist.javax.sip.SipProviderImpl;
import gov.nist.javax.sip.SipStackImpl;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.stack.SIPDialog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.sip.*;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.Header;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;

/**
 * @author: yyh
 * @date: 2021-12-07 09:49
 * @description: SipCommander
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class SipCommander implements ISipCommander {


    public static final String SESSION_NAME_PLAY = "Play";
    public static final String SESSION_NAME_DOWNLOAD = "Download";
    public static final String SESSION_NAME_PLAY_BACK = "Playback";
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private final DateTimeFormatter dtf1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final SipProviderImpl tcpSipProvider;
    private final SipProviderImpl udpSipProvider;
    private final SipSubscribe sipSubscribe;
    private final SipRequestHeaderProvider headerProvider;
    private final GlobalConfigProperties properties;
    private final SipServerProperties sipServerProperties;
    private final PlayHookSubscribe playHookSubscribe;
    private final StreamSessionManager sessionManager;
    private final IMediaServerService mediaServerService;
    private final RedisTemplate<String,String> redisTemplate;
    private final ObjectMapper mapper;
    private final SsrcManagement ssrcManagement;

    /**
     * ??????????????????
     *
     * @param device ??????
     * @return Boolean
     */
    @Override
    public Boolean deviceInfoQuery(GbDevice device) {
        try {
            String tm = Long.toString(System.currentTimeMillis());
            CallIdHeader callIdHeader = SipRequestConstant.TCP.equals(device.getTransport()) ? tcpSipProvider.getNewCallId()
                    : udpSipProvider.getNewCallId();
            String catalogXml = "<?xml version=\"1.0\" encoding=\"GB2312\"?>\r\n" +
                    "<Query>\r\n" +
                    "<CmdType>DeviceInfo</CmdType>\r\n" +
                    "<SN>" + (int) ((Math.random() * 9 + 1) * 100000) + "</SN>\r\n" +
                    "<DeviceID>" + device.getGbId() + "</DeviceID>\r\n" +
                    "</Query>\r\n";
            Request request = headerProvider.createMessageRequest(device, catalogXml, "z9hG4bK-ViaDeviceInfo-" + tm, "FromDev" + tm, null, callIdHeader);
            transmitRequest(device, request);
        } catch (SipException | ParseException | InvalidArgumentException e) {
            log.error("[ sip ]:????????????????????????????????????:{}",e.getMessage(),e);
            return false;
        }
        return true;
    }

    /**
     * ????????????????????????
     *
     * @param device device
     * @param errorEvent ????????????
     * @return Boolean
     */
    @Override
    public Boolean deviceCatalogQuery(GbDevice device, SipCallback errorEvent) {
        try {
            String tm = Long.toString(System.currentTimeMillis());
            CallIdHeader callIdHeader = device.getTransport().equals(SipRequestConstant.TCP) ? tcpSipProvider.getNewCallId()
                    : udpSipProvider.getNewCallId();
            String catalogXml = "<?xml version=\"1.0\" encoding=\"GB2312\"?>\r\n" +
                    "<Query>\r\n" +
                    "<CmdType>Catalog</CmdType>\r\n" +
                    "<SN>" + (int) ((Math.random() * 9 + 1) * 100000) + "</SN>\r\n" +
                    "<DeviceID>" + device.getGbId() + "</DeviceID>\r\n" +
                    "</Query>\r\n";
            Request request = headerProvider.createMessageRequest(device, catalogXml, "z9hG4bK-ViaCatalog-" + tm, "FromCat" + tm, null, callIdHeader);
            transmitRequest(device, request, errorEvent);
        } catch (SipException | ParseException | InvalidArgumentException e) {
            log.error("[ sip ]:????????????????????????????????????:{}",e.getMessage(),e);
            return false;
        }
        return true;
    }

    /**
     * ?????????????????????
     *
     * @param device        ????????????
     * @param ssrcInfo      ssrc
     * @param channelId     ????????????
     * @param server        ??????????????????
     * @param event hook??????
     * @param errorCallback ????????????
     */
    @Override
    public void playRealtimeStream(GbDevice device,
                                   SsrcInfo ssrcInfo,
                                   String channelId,
                                   MediaServer server,
                                   HookEvent event,
                                   SipCallback errorCallback) {
        String streamMode = device.getStreamMode().toUpperCase();
        String sb = SipContentHelper.generateRealTimeMediaStreamInviteContent(
                sipServerProperties.getId(),
                server.getSdpIp(),
                ssrcInfo.getPort(),
                properties.getSeniorSdp(),
                streamMode,
                SESSION_NAME_PLAY,
                ssrcInfo.getSsrc());
        if (event!=null){
            //??????????????????
            playHookSubscribe.subscribe(device.getGbId()+"_"+channelId,event);
        }
        try {
            String tm = Long.toString(System.currentTimeMillis());
            CallIdHeader callIdHeader = device.getTransport().equals(SipRequestConstant.TCP) ? tcpSipProvider.getNewCallId()
                    : udpSipProvider.getNewCallId();
            Request request = headerProvider.createInviteRequest(device, channelId, sb.toString(), null, "FromInvt" + tm, null, ssrcInfo.getSsrc(), callIdHeader);
            String finalStreamId = ssrcInfo.getStreamId();
            transmitRequest(device, request, (error -> {
                log.warn("??????????????????????????????:{}",error.msg);
                sessionManager.remove(device.getGbId(), channelId);
//            mediaServerService.releaseSsrc(mediaServerItem, ssrcInfo.getSsrc());
//            errorEvent.response(e);
            }), (ok -> {
                /// ????????????,??????session
            sessionManager.put(device.getGbId(), channelId ,ssrcInfo.getSsrc(), finalStreamId, server.getServerId(), ((ResponseEvent)ok.event).getClientTransaction());
            sessionManager.put(device.getGbId(), channelId , ok.dialog);
            }));
        }catch (Exception ex){
            log.error("????????????????????????:{}->{}{}",device.getGbId(),channelId,ex.getMessage(),ex);
        }
    }

    /**
     * ??????????????????
     *
     * @param device        ????????????
     * @param ssrcInfo      ssrc
     * @param channelId     ????????????
     * @param server        ??????????????????
     * @param bTime         ????????????
     * @param eTime         ????????????
     * @param event         hook ??????
     * @param errorCallback ????????????
     */
    @Override
    public void playbackStream(GbDevice device,
                               SsrcInfo ssrcInfo,
                               String channelId,
                               MediaServer server,
                               Long bTime,
                               Long eTime,
                               HookEvent event,
                               SipCallback errorCallback) {
        String streamMode = device.getStreamMode().toUpperCase();
        String sb = SipContentHelper.generatePlaybackMediaStreamInviteContent(sipServerProperties.getId(),
                server.getSdpIp(),
                channelId,
                ssrcInfo.getPort(),
                properties.getSeniorSdp(),
                streamMode,
                ssrcInfo.getSsrc(), bTime, eTime);
        if (event!=null){
            //??????????????????
            playHookSubscribe.subscribe(device.getGbId()+"_"+channelId,event);
        }
        try {
            String tm = Long.toString(System.currentTimeMillis());
            CallIdHeader callIdHeader = device.getTransport().equals(SipRequestConstant.TCP) ? tcpSipProvider.getNewCallId()
                    : udpSipProvider.getNewCallId();
            Request request = headerProvider.createInviteRequest(device, channelId, sb.toString(), null, "fromplybck" + tm, null, ssrcInfo.getSsrc(), callIdHeader);
            String finalStreamId = ssrcInfo.getStreamId();
            transmitRequest(device, request, (error -> {
                log.warn("????????????????????????:{}",error.msg);
                sessionManager.remove(device.getGbId(), channelId);
//            mediaServerService.releaseSsrc(mediaServerItem, ssrcInfo.getSsrc());
//            errorEvent.response(e);
            }), (ok -> {
                /// ????????????,??????session
                log.warn("??????????????????:{}",ok.msg);
                sessionManager.put(device.getGbId(), channelId ,ssrcInfo.getSsrc(), finalStreamId, server.getServerId(), ((ResponseEvent)ok.event).getClientTransaction());
                sessionManager.put(device.getGbId(), channelId , ok.dialog);
            }));
        }catch (Exception ex){
            log.error("????????????????????????:{}->{}{}",device.getGbId(),channelId,ex.getMessage(),ex);
        }
    }

    /**
     * ??????????????????
     *
     * @param device        ????????????
     * @param ssrcInfo      ssrc
     * @param channelId     ??????id
     * @param server        ??????????????????
     * @param bTime         ????????????
     * @param eTime         ????????????
     * @param downloadSpeed ????????????
     * @param event         hook ??????
     * @param errorCallback sip????????????
     */
    @Override
    public void downloadStream(GbDevice device,
                               SsrcInfo ssrcInfo,
                               String channelId,
                               MediaServer server,
                               Long bTime, Long eTime,
                               String downloadSpeed,
                               HookEvent event,
                               SipCallback errorCallback) {
        String streamMode = device.getStreamMode().toUpperCase();
        String sb = SipContentHelper.generateDownloadMediaStreamInviteContent(sipServerProperties.getId(),
                server.getSdpIp(),
                channelId,
                ssrcInfo.getPort(),
                properties.getSeniorSdp(),
                streamMode,
                ssrcInfo.getSsrc(), bTime, eTime, downloadSpeed);
        if (event!=null){
            //??????????????????
            playHookSubscribe.subscribe(device.getGbId()+"_"+channelId,event);
        }
        try {
            String tm = Long.toString(System.currentTimeMillis());
            CallIdHeader callIdHeader = device.getTransport().equals(SipRequestConstant.TCP) ? tcpSipProvider.getNewCallId()
                    : udpSipProvider.getNewCallId();
            Request request = headerProvider.createInviteRequest(device, channelId, sb, null, "fromplybck" + tm, null, ssrcInfo.getSsrc(), callIdHeader);
            ClientTransaction transaction = transmitRequest(device, request, errorCallback);
            sessionManager.put(device.getGbId(), channelId, ssrcInfo.getSsrc(), ssrcInfo.getStreamId(), server.getId(), transaction);
        }catch (Exception ex){
            log.error("????????????????????????:{}->{}{}",device.getGbId(),channelId,ex.getMessage(),ex);
        }
    }

    /**
     * ????????????
     *
     * @param deviceId  deviceId
     * @param channelId channelId
     */
    @Override
    public void streamByeCmd(String deviceId, String channelId) {
        streamByeCmd(deviceId,channelId,null);
    }

    /**
     * ????????????
     *
     * @param deviceId   deviceId
     * @param channelId  deviceId
     * @param okCallback okCallback
     */
    @Override
    public void streamByeCmd(String deviceId, String channelId, SipCallback okCallback) {
        try{
            ClientTransaction transaction = sessionManager.getTransaction(deviceId, channelId);
            if (transaction == null) {
                log.warn("[ {} -> {}]?????????????????????????????????????????????", deviceId, channelId);
                EventResult<EventObject> eventResult = new EventResult<>();
                if (okCallback != null) {
                    okCallback.response(eventResult);
                }
                return;
            }
            SIPDialog dialog = sessionManager.getDialog(deviceId, channelId);
            if (dialog == null) {
                log.warn("[ {} -> {}]?????????????????????????????????????????????", deviceId, channelId);
                return;
            }
            SipStack sipStack = udpSipProvider.getSipStack();
            SIPDialog sipDialog = ((SipStackImpl) sipStack).putDialog(dialog);
            if (dialog != sipDialog) {
                dialog = sipDialog;
            }else {
                dialog.setSipProvider(udpSipProvider);
                try {
                    Field sipStackField = SIPDialog.class.getDeclaredField("sipStack");
                    sipStackField.setAccessible(true);
                    sipStackField.set(dialog, sipStack);
                    Field eventListenersField = SIPDialog.class.getDeclaredField("eventListeners");
                    eventListenersField.setAccessible(true);
                    eventListenersField.set(dialog, new HashSet<>());
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            Request byeRequest = dialog.createRequest(Request.BYE);
            SipURI byeUri = (SipURI) byeRequest.getRequestURI();
            SIPRequest request = (SIPRequest)transaction.getRequest();
            byeUri.setHost(request.getRemoteAddress().getHostName());
            byeUri.setPort(request.getRemotePort());
            ViaHeader viaHeader = (ViaHeader) byeRequest.getHeader(ViaHeader.NAME);
            String protocol = viaHeader.getTransport().toUpperCase();
            ClientTransaction clientTransaction = null;
            if(SipRequestConstant.TCP.equals(protocol)) {
                clientTransaction = tcpSipProvider.getNewClientTransaction(byeRequest);
            } else if(SipRequestConstant.UDP.equals(protocol)) {
                clientTransaction = udpSipProvider.getNewClientTransaction(byeRequest);
            }

            CallIdHeader callIdHeader = (CallIdHeader) byeRequest.getHeader(CallIdHeader.NAME);
            if (okCallback != null) {
                sipSubscribe.addOkSubscribe(callIdHeader.getCallId(), okCallback);
            }

            dialog.sendRequest(clientTransaction);

            SsrcTransaction ssrcTransaction = sessionManager.getSsrcTransaction(deviceId, channelId);
            if (ssrcTransaction != null) {
                String ssrc = sessionManager.getSSRC(deviceId, channelId);
                if (StrUtil.isNotBlank(ssrc)){
                    sessionManager.remove(deviceId,channelId);
                    //??????ssrc
                    String substring = ssrc.substring(ssrc.length() - 4);
//                    String key = MediaConstant.SSRC_SERVER+ssrcTransaction.getMediaServerId();
//                    Object unused = redisTemplate.opsForHash().get(key, "unused");
//                    Object used = redisTemplate.opsForHash().get(key, "used");
//                    assert unused != null;
//                    assert used != null;
//                    ArrayList<String> un = mapper.readValue(unused.toString(), ConverterType.ARRAY_LIST_STRING_TYPE);
//                    ArrayList<String> u = mapper.readValue(used.toString(), ConverterType.ARRAY_LIST_STRING_TYPE);
//                    un.add(substring);
//                    u.remove(substring);
//                    redisTemplate.opsForHash().put(key,"unused",mapper.writeValueAsString(un));
//                    redisTemplate.opsForHash().put(key,"used",mapper.writeValueAsString(u));
                    ssrcManagement.push(ssrcTransaction.getMediaServerId(),substring);
                }
                sessionManager.remove(deviceId, channelId);
            }
        } catch (SipException | ParseException e) {
            log.error("????????????????????????????????????:{}",e.getMessage(),e);
        }
    }

    /**
     * ????????????????????????
     *
     * @param device      ????????????
     * @param deviceId      ??????id
     * @param channelId     ??????id
     * @param bTime         ????????????
     * @param eTime         ????????????
     * @param sn            ?????????
     * @param errorCallback ????????????
     */
    @Override
    public void recordInfoQuery(GbDevice device,
                                String deviceId,
                                String channelId,
                                LocalDateTime bTime,
                                LocalDateTime eTime,
                                int sn,
                                SipCallback errorCallback) {
        String format1 = dtf.format(bTime);
        String format2 = dtf.format(eTime);
        try {
            String tm = Long.toString(System.currentTimeMillis());
            CallIdHeader callIdHeader = device.getTransport().equals(SipRequestConstant.TCP) ? tcpSipProvider.getNewCallId()
                    : udpSipProvider.getNewCallId();
            String recordInfoXml = "<?xml version=\"1.0\" encoding=\"GB2312\"?>\r\n" +
                    "<Query>\r\n" +
                    "<CmdType>RecordInfo</CmdType>\r\n" +
                    "<SN>" + sn + "</SN>\r\n" +
                    "<DeviceID>" + channelId + "</DeviceID>\r\n" +
                    "<StartTime>" + format1 + "</StartTime>\r\n" +
                    "<EndTime>" + format2 + "</EndTime>\r\n" +
                    "<Secrecy>0</Secrecy>\r\n" +
                    // ??????NVR??????????????????????????????all?????????????????????Type
                    "<Type>all</Type>\r\n" +
                    "</Query>\r\n";
            Request request = headerProvider.createMessageRequest(device, recordInfoXml,
                    "z9hG4bK-ViaRecordInfo-" + tm, "fromRec" + tm, null, callIdHeader);
            transmitRequest(device, request, errorCallback);
        }catch (Exception ex){
            log.error("record info query has error :{}",ex.getMessage(),ex);
        }
    }

    /**
     * ??????????????????
     *
     * @param device   ????????????
     * @param expires  ??????????????????
     * @param interval ??????????????????
     * @return Boolean
     */
    @Override
    public Boolean mobilePositionSubscribe(GbDevice device,
                                           Integer expires,
                                           Integer interval) {
        try {
            StringBuilder sb = new StringBuilder(200);
            sb.append("<?xml version=\"1.0\" encoding=\"GB2312\"?>\r\n");
            sb.append("<Query>\r\n");
            sb.append("<CmdType>MobilePosition</CmdType>\r\n");
            sb.append("<SN>").append((int) ((Math.random() * 9 + 1) * 100000)).append("</SN>\r\n");
            sb.append("<DeviceID>").append(device.getGbId()).append("</DeviceID>\r\n");
            if (expires > 0) {
                sb.append("<Interval>").append(interval).append("</Interval>\r\n");
            }
            sb.append("</Query>\r\n");
            String tm = Long.toString(System.currentTimeMillis());
            CallIdHeader callIdHeader = device.getTransport().equals(SipRequestConstant.TCP) ? tcpSipProvider.getNewCallId()
                    : udpSipProvider.getNewCallId();
            //Position;id=" + tm.substring(tm.length() - 4));
            Request request = headerProvider.createSubscribeRequest(device, sb.toString(), "z9hG4bK-viaPos-" + tm, "fromTagPos" + tm, null, expires, "presence" ,callIdHeader);
            transmitRequest(device, request);
            return true;
        } catch ( NumberFormatException | ParseException | InvalidArgumentException	| SipException e) {
            log.error("mobile position subscribe has error :{}",e.getMessage(),e);
            return false;
        }
    }

    /**
     * ??????????????????
     *
     * @param device        ????????????
     * @param startPriority ??????????????????????????????
     * @param endPriority   ??????????????????????????????
     * @param alarmMethod   ??????????????????????????????
     * @param alarmType     ????????????
     * @param startTime     ????????????????????????????????????
     * @param endTime       ????????????????????????????????????
     * @param sn            sn???
     * @param errorCallback ????????????
     * @return true = ??????????????????
     */
    @Override
    public Boolean alarmInfoQuery(GbDevice device,
                                  String startPriority,
                                  String endPriority,
                                  String alarmMethod,
                                  String alarmType,
                                  LocalDateTime startTime,
                                  LocalDateTime endTime,
                                  Integer sn,
                                  SipCallback errorCallback) {
        try {
            StringBuilder cmdXml = new StringBuilder(200);
            cmdXml.append("<?xml version=\"1.0\" ?>\r\n");
            cmdXml.append("<Query>\r\n");
            cmdXml.append("<CmdType>Alarm</CmdType>\r\n");
            cmdXml.append("<SN>").append(sn).append("</SN>\r\n");
            cmdXml.append("<DeviceID>").append(device.getGbId()).append("</DeviceID>\r\n");
            if (StrUtil.isNotBlank(startPriority)) {
                cmdXml.append("<StartAlarmPriority>").append(startPriority).append("</StartAlarmPriority>\r\n");
            }
            if (StrUtil.isNotBlank(endPriority)) {
                cmdXml.append("<EndAlarmPriority>").append(endPriority).append("</EndAlarmPriority>\r\n");
            }
            if (StrUtil.isNotBlank(alarmMethod)) {
                cmdXml.append("<AlarmMethod>").append(alarmMethod).append("</AlarmMethod>\r\n");
            }
            if (StrUtil.isNotBlank(alarmType)) {
                cmdXml.append("<AlarmType>").append(alarmType).append("</AlarmType>\r\n");
            }
            if (startTime!=null) {
                cmdXml.append("<StartAlarmTime>").append(dtf1.format(startTime)).append("</StartAlarmTime>\r\n");
            }
            if (endTime!=null) {
                cmdXml.append("<EndAlarmTime>").append(dtf1.format(endTime)).append("</EndAlarmTime>\r\n");
            }
            cmdXml.append("</Query>\r\n");


            String tm = Long.toString(System.currentTimeMillis());

            CallIdHeader callIdHeader = device.getTransport().equals(SipRequestConstant.TCP) ? tcpSipProvider.getNewCallId()
                    : udpSipProvider.getNewCallId();

            Request request = headerProvider.createMessageRequest(device, cmdXml.toString(), null, "FromAlarm" + tm, null, callIdHeader);
            transmitRequest(device, request, errorCallback);
            return true;
        } catch (SipException | ParseException | InvalidArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * ??????????????????
     *
     * @param device        ????????????
     * @param sn            sn???
     * @param errorCallback ????????????
     * @return
     */
    @Override
    public Boolean deviceStatusQuery(GbDevice device, String sn, SipCallback errorCallback) {
        try {
            String tm = Long.toString(System.currentTimeMillis());
            CallIdHeader callIdHeader = device.getTransport().equals(SipRequestConstant.TCP) ? tcpSipProvider.getNewCallId()
                    : udpSipProvider.getNewCallId();
            String catalogXml = "<?xml version=\"1.0\" encoding=\"GB2312\"?>\r\n" +
                    "<Query>\r\n" +
                    "<CmdType>DeviceStatus</CmdType>\r\n" +
                    "<SN>" + sn + "</SN>\r\n" +
                    "<DeviceID>" + device.getGbId() + "</DeviceID>\r\n" +
                    "</Query>\r\n";
            Request request = headerProvider.createMessageRequest(device, catalogXml, null, "FromStatus" + tm, null, callIdHeader);
            transmitRequest(device, request, errorCallback);
            return true;
        } catch (SipException | ParseException | InvalidArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * ??????????????????
     *
     * @param device        ??????id
     * @param channelId     ??????id
     * @param configType    ????????????
     * @param errorCallback ????????????
     * @return Boolean
     */
    @Override
    public Boolean deviceConfigQuery(GbDevice device, String channelId, String configType, SipCallback errorCallback) {
        try {
            StringBuilder cmdXml = new StringBuilder(200);
            cmdXml.append("<?xml version=\"1.0\" ?>\r\n");
            cmdXml.append("<Query>\r\n");
            cmdXml.append("<CmdType>ConfigDownload</CmdType>\r\n");
            cmdXml.append("<SN>").append((int)((Math.random()*9+1)*100000)).append("</SN>\r\n");
            if (StrUtil.isBlank(channelId)) {
                cmdXml.append("<DeviceID>").append(device.getGbId()).append("</DeviceID>\r\n");
            } else {
                cmdXml.append("<DeviceID>").append(channelId).append("</DeviceID>\r\n");
            }
            cmdXml.append("<ConfigType>").append(configType).append("</ConfigType>\r\n");
            cmdXml.append("</Query>\r\n");

            String tm = Long.toString(System.currentTimeMillis());

            CallIdHeader callIdHeader = device.getTransport().equals(SipRequestConstant.TCP) ? tcpSipProvider.getNewCallId()
                    : udpSipProvider.getNewCallId();

            Request request = headerProvider.createMessageRequest(device, cmdXml.toString(), null, "FromConfig" + tm, null, callIdHeader);
            transmitRequest(device, request, errorCallback);
            return true;
        } catch (SipException | ParseException | InvalidArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * ????????????????????????
     *
     * @param device            ????????????
     * @param channelId         ??????id
     * @param sn                ?????????
     * @param name              ??????/????????????(??????)
     * @param expiration        ??????????????????(??????)
     * @param heartBeatInterval ??????????????????(??????)
     * @param heartBeatCount    ??????????????????(??????)
     * @param errorCallback     ????????????
     * @return Boolean
     */
    @Override
    public Boolean deviceBasicConfigCmd(GbDevice device, String channelId,int sn, String name, String expiration, String heartBeatInterval, String heartBeatCount, SipCallback errorCallback) {
        try {
            StringBuilder cmdXml = new StringBuilder(200);
            cmdXml.append("<?xml version=\"1.0\" ?>\r\n");
            cmdXml.append("<Control>\r\n");
            cmdXml.append("<CmdType>DeviceConfig</CmdType>\r\n");
            cmdXml.append("<SN>").append(sn).append("</SN>\r\n");
            if (!StringUtils.hasLength(channelId)) {
                cmdXml.append("<DeviceID>").append(device.getGbId()).append("</DeviceID>\r\n");
            } else {
                cmdXml.append("<DeviceID>").append(channelId).append("</DeviceID>\r\n");
            }
            cmdXml.append("<BasicParam>\r\n");
            if (StringUtils.hasLength(name)) {
                cmdXml.append("<Name>").append(name).append("</Name>\r\n");
            }
            if (NumberUtil.isInteger(expiration)) {
                if (Integer.parseInt(expiration) > 0) {
                    cmdXml.append("<Expiration>").append(expiration).append("</Expiration>\r\n");
                }
            }
            if (NumberUtil.isInteger(heartBeatInterval)) {
                if (Integer.parseInt(heartBeatInterval) > 0) {
                    cmdXml.append("<HeartBeatInterval>").append(heartBeatInterval).append("</HeartBeatInterval>\r\n");
                }
            }
            if (NumberUtil.isInteger(heartBeatCount)) {
                if (Integer.parseInt(heartBeatCount) > 0) {
                    cmdXml.append("<HeartBeatCount>").append(heartBeatCount).append("</HeartBeatCount>\r\n");
                }
            }
            cmdXml.append("</BasicParam>\r\n");
            cmdXml.append("</Control>\r\n");

            String tm = Long.toString(System.currentTimeMillis());
            CallIdHeader callIdHeader = device.getTransport().equals(SipRequestConstant.TCP) ? tcpSipProvider.getNewCallId()
                    : udpSipProvider.getNewCallId();
            Request request = headerProvider.createMessageRequest(device, cmdXml.toString(), null, "FromConfig" + tm, null, callIdHeader);
            transmitRequest(device, request, errorCallback);
            return true;
        } catch (SipException | ParseException | InvalidArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * ????????????????????????
     *
     * @param device ????????????
     */
    @Override
    public boolean teleBootCmd(GbDevice device) {
        try {
            String tm = Long.toString(System.currentTimeMillis());
            CallIdHeader callIdHeader = device.getTransport().equals(SipRequestConstant.TCP) ? tcpSipProvider.getNewCallId()
                    : udpSipProvider.getNewCallId();
            String cmdXml = "<?xml version=\"1.0\" ?>\r\n" +
                    "<Control>\r\n" +
                    "<CmdType>DeviceControl</CmdType>\r\n" +
                    "<SN>" + (int) ((Math.random() * 9 + 1) * 100000) + "</SN>\r\n" +
                    "<DeviceID>" + device.getGbId() + "</DeviceID>\r\n" +
                    "<TeleBoot>Boot</TeleBoot>\r\n" +
                    "</Control>\r\n";
            Request request = headerProvider.createMessageRequest(device, cmdXml, null, "FromBoot" + tm, null, callIdHeader);
            transmitRequest(device, request);
            return true;
        } catch (SipException | ParseException | InvalidArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * ?????????????????????
     *
     * @param device        ??????????????????
     * @param channelId     ??????????????????
     * @param recordCmdStr  ??????????????????
     * @param sn  ?????????
     * @param errorCallback ????????????
     * @return boolean
     */
    @Override
    public boolean recordCmd(GbDevice device,
                             String channelId,
                             String recordCmdStr,
                             int sn,
                             SipCallback errorCallback) {
        try {
            StringBuilder cmdXml = new StringBuilder(200);
            cmdXml.append("<?xml version=\"1.0\" ?>\r\n");
            cmdXml.append("<Control>\r\n");
            cmdXml.append("<CmdType>DeviceControl</CmdType>\r\n");
            cmdXml.append("<SN>").append((int) ((Math.random() * 9 + 1) * 100000)).append("</SN>\r\n");
            if (!StringUtils.hasLength(channelId)) {
                cmdXml.append("<DeviceID>").append(device.getGbId()).append("</DeviceID>\r\n");
            } else {
                cmdXml.append("<DeviceID>").append(channelId).append("</DeviceID>\r\n");
            }
            cmdXml.append("<RecordCmd>").append(recordCmdStr).append("</RecordCmd>\r\n");
            cmdXml.append("</Control>\r\n");
            String tm = Long.toString(System.currentTimeMillis());

            CallIdHeader callIdHeader = device.getTransport().equals(SipRequestConstant.TCP) ? tcpSipProvider.getNewCallId()
                    : udpSipProvider.getNewCallId();

            Request request = headerProvider.createMessageRequest(device, cmdXml.toString(), null, "FromRecord" + tm, null, callIdHeader);
            transmitRequest(device, request, errorCallback);
            return true;
        } catch (SipException | ParseException | InvalidArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * ??????/????????????
     *
     * @param device        ??????????????????
     * @param guardCmdStr   ????????????????????????
     * @param sn   ?????????
     * @param errorCallback ????????????
     * @return boolean
     */
    @Override
    public boolean guardCmd(GbDevice device, String guardCmdStr,int sn, SipCallback errorCallback) {
        try {
            String tm = Long.toString(System.currentTimeMillis());
            CallIdHeader callIdHeader = device.getTransport().equals(SipRequestConstant.TCP) ? tcpSipProvider.getNewCallId()
                    : udpSipProvider.getNewCallId();
            String cmdXml = "<?xml version=\"1.0\" ?>\r\n" +
                    "<Control>\r\n" +
                    "<CmdType>DeviceControl</CmdType>\r\n" +
                    "<SN>" + sn + "</SN>\r\n" +
                    "<DeviceID>" + device.getGbId() + "</DeviceID>\r\n" +
                    "<GuardCmd>" + guardCmdStr + "</GuardCmd>\r\n" +
                    "</Control>\r\n";
            Request request = headerProvider.createMessageRequest(device, cmdXml, null, "FromGuard" + tm, null, callIdHeader);
            transmitRequest(device, request, errorCallback);
            return true;
        } catch (SipException | ParseException | InvalidArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * ?????????????????????
     * ??????????????????????????????????????????IDR???
     * @param device        ??????????????????
     * @param channelId     ??????id
     * @param errorCallback ????????????
     * @return boolean
     */
    @Override
    public boolean iFrameCmd(GbDevice device, String channelId, SipCallback errorCallback) {
        try {
            StringBuilder cmdXml = new StringBuilder(200);
            cmdXml.append("<?xml version=\"1.0\" ?>\r\n");
            cmdXml.append("<Control>\r\n");
            cmdXml.append("<CmdType>DeviceControl</CmdType>\r\n");
            cmdXml.append("<SN>").append((int) ((Math.random() * 9 + 1) * 100000)).append("</SN>\r\n");
            if (!StringUtils.hasLength(channelId)) {
                cmdXml.append("<DeviceID>").append(device.getGbId()).append("</DeviceID>\r\n");
            } else {
                cmdXml.append("<DeviceID>").append(channelId).append("</DeviceID>\r\n");
            }
            cmdXml.append("<IFameCmd>Send</IFameCmd>\r\n");
            cmdXml.append("</Control>\r\n");

            String tm = Long.toString(System.currentTimeMillis());

            CallIdHeader callIdHeader = device.getTransport().equals(SipRequestConstant.TCP) ? tcpSipProvider.getNewCallId()
                    : udpSipProvider.getNewCallId();

            Request request = headerProvider.createMessageRequest(device, cmdXml.toString(), null, "FromBoot" + tm, null, callIdHeader);
            transmitRequest(device, request,errorCallback);
            return true;
        } catch (SipException | ParseException | InvalidArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * ?????????????????????
     *
     * @param device        ??????????????????
     * @param channelId     ??????id
     * @param enabled       ??????????????????1 = ?????????0 = ??????
     * @param resetTime     ????????????????????????????????????????????????????????????:???(s)
     * @param presetIndex   ???????????????????????????????????????????????????????????????0~255
     * @param errorCallback ????????????
     * @return boolean
     */
    @Override
    public boolean homePositionCmd(GbDevice device, String channelId, String enabled, String resetTime, String presetIndex, SipCallback errorCallback) {
        try {
            StringBuilder cmdXml = new StringBuilder(200);
            cmdXml.append("<?xml version=\"1.0\" ?>\r\n");
            cmdXml.append("<Control>\r\n");
            cmdXml.append("<CmdType>DeviceControl</CmdType>\r\n");
            cmdXml.append("<SN>").append((int) ((Math.random() * 9 + 1) * 100000)).append("</SN>\r\n");
            if (!StringUtils.hasLength(channelId)) {
                cmdXml.append("<DeviceID>").append(device.getGbId()).append("</DeviceID>\r\n");
            } else {
                cmdXml.append("<DeviceID>").append(channelId).append("</DeviceID>\r\n");
            }
            cmdXml.append("<HomePosition>\r\n");
            if (NumberUtil.isInteger(enabled) && (!enabled.equals("0"))) {
                cmdXml.append("<Enabled>1</Enabled>\r\n");
                if (NumberUtil.isInteger(resetTime)) {
                    cmdXml.append("<ResetTime>").append(resetTime).append("</ResetTime>\r\n");
                } else {
                    cmdXml.append("<ResetTime>0</ResetTime>\r\n");
                }
                if (NumberUtil.isInteger(presetIndex)) {
                    cmdXml.append("<PresetIndex>").append(presetIndex).append("</PresetIndex>\r\n");
                } else {
                    cmdXml.append("<PresetIndex>0</PresetIndex>\r\n");
                }
            } else {
                cmdXml.append("<Enabled>0</Enabled>\r\n");
            }
            cmdXml.append("</HomePosition>\r\n");
            cmdXml.append("</Control>\r\n");

            String tm = Long.toString(System.currentTimeMillis());

            CallIdHeader callIdHeader = device.getTransport().equals(SipRequestConstant.TCP) ? tcpSipProvider.getNewCallId()
                    : udpSipProvider.getNewCallId();

            Request request = headerProvider.createMessageRequest(device, cmdXml.toString(), null, "FromGuard" + tm, null, callIdHeader);
            transmitRequest(device, request, errorCallback);
            return true;
        } catch (SipException | ParseException | InvalidArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * ??????????????????
     *
     * @param device    ????????????
     * @param channelId ??????id
     * @param cmdString ?????????????????????
     * @return boolean
     */
    @Override
    public boolean dragZoomCmd(GbDevice device, String channelId, String cmdString) {
        try {
            StringBuilder dragXml = new StringBuilder(200);
            dragXml.append("<?xml version=\"1.0\" ?>\r\n");
            dragXml.append("<Control>\r\n");
            dragXml.append("<CmdType>DeviceControl</CmdType>\r\n");
            dragXml.append("<SN>").append((int) ((Math.random() * 9 + 1) * 100000)).append("</SN>\r\n");
            if (!StringUtils.hasLength(channelId)) {
                dragXml.append("<DeviceID>").append(device.getGbId()).append("</DeviceID>\r\n");
            } else {
                dragXml.append("<DeviceID>").append(channelId).append("</DeviceID>\r\n");
            }
            dragXml.append(cmdString);
            dragXml.append("</Control>\r\n");
            String tm = Long.toString(System.currentTimeMillis());
            CallIdHeader callIdHeader = device.getTransport().equals(SipRequestConstant.TCP) ? tcpSipProvider.getNewCallId()
                    : udpSipProvider.getNewCallId();
            Request request = headerProvider.createMessageRequest(device, dragXml.toString(), "z9hG4bK-ViaPtz-" + tm, "FromPtz" + tm, null, callIdHeader);
            log.debug("??????????????? " + request.toString());
            transmitRequest(device, request);
            return true;
        } catch (SipException | ParseException | InvalidArgumentException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * ?????????????????????PTZ?????????FI???????????????????????????????????????????????????????????????????????????
     *
     * @param device        ????????????
     * @param channelId     ??????id
     * @param cmdCode       ?????????
     * @param horizonSpeed  ??????1
     * @param verticalSpeed ??????2
     * @param zoomSpeed     ?????????2
     * @return boolean
     */
    @Override
    public boolean frontEndCmd(GbDevice device,
                               String channelId,
                               int cmdCode,
                               int horizonSpeed,
                               int verticalSpeed,
                               int zoomSpeed) {
        try {
            String cmdStr= frontEndCmdString(cmdCode, horizonSpeed, verticalSpeed, zoomSpeed);
            log.debug("??????????????????" + cmdStr);
            String tm = Long.toString(System.currentTimeMillis());
            CallIdHeader callIdHeader = device.getTransport().equals(SipRequestConstant.TCP) ? tcpSipProvider.getNewCallId()
                    : udpSipProvider.getNewCallId();
            String ptzXml = "<?xml version=\"1.0\" ?>\r\n" +
                    "<Control>\r\n" +
                    "<CmdType>DeviceControl</CmdType>\r\n" +
                    "<SN>" + (int) ((Math.random() * 9 + 1) * 100000) + "</SN>\r\n" +
                    "<DeviceID>" + channelId + "</DeviceID>\r\n" +
                    "<PTZCmd>" + cmdStr + "</PTZCmd>\r\n" +
                    "<Info>\r\n" +
                    "</Info>\r\n" +
                    "</Control>\r\n";
            Request request = headerProvider.createMessageRequest(device, ptzXml, "z9hG4bK-ViaPtz-" + tm, "FromPtz" + tm, null, callIdHeader);
            transmitRequest(device, request);
            return true;
        } catch (SipException | ParseException | InvalidArgumentException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * ?????????????????????
     *
     * @param device        ????????????
     * @param channelId     ??????id
     * @param snowflake     ?????????
     * @param errorCallback ????????????
     * @return boolean
     */
    @Override
    public boolean presetQuery(GbDevice device, String channelId, long snowflake, SipCallback errorCallback) {
        try {
            StringBuilder cmdXml = new StringBuilder(200);
            cmdXml.append("<?xml version=\"1.0\" ?>\r\n");
            cmdXml.append("<Query>\r\n");
            cmdXml.append("<CmdType>PresetQuery</CmdType>\r\n");
            cmdXml.append("<SN>").append(snowflake).append("</SN>\r\n");
            if (!StringUtils.hasLength(channelId)) {
                cmdXml.append("<DeviceID>").append(device.getGbId()).append("</DeviceID>\r\n");
            } else {
                cmdXml.append("<DeviceID>").append(channelId).append("</DeviceID>\r\n");
            }
            cmdXml.append("</Query>\r\n");

            String tm = Long.toString(System.currentTimeMillis());

            CallIdHeader callIdHeader = device.getTransport().equals(SipRequestConstant.TCP) ? tcpSipProvider.getNewCallId()
                    : udpSipProvider.getNewCallId();
            Request request = headerProvider.createMessageRequest(device, cmdXml.toString(), null, "FromConfig" + tm, null, callIdHeader);
            transmitRequest(device, request, errorCallback);
            return true;
        } catch (SipException | ParseException | InvalidArgumentException e) {
            e.printStackTrace();
            return false;
        }
    }

    private ClientTransaction transmitRequest(GbDevice device, Request request) throws SipException {
        return transmitRequest(device, request, null, null);
    }

    private ClientTransaction transmitRequest(GbDevice device, Request request, SipCallback errorCallback) throws SipException {
        return transmitRequest(device, request, errorCallback, null);
    }
    private ClientTransaction transmitRequest(GbDevice device,
                                              Request request,
                                              SipCallback errorCallback ,
                                              SipCallback okCallback) throws SipException {
        ClientTransaction clientTransaction = null;
        if(SipRequestConstant.TCP.equals(device.getTransport())) {
            clientTransaction = tcpSipProvider.getNewClientTransaction(request);
        } else if(SipRequestConstant.UDP.equals(device.getTransport())) {
            clientTransaction = udpSipProvider.getNewClientTransaction(request);
        }else {
            return null;
        }
        CallIdHeader callIdHeader = (CallIdHeader)request.getHeader(CallIdHeader.NAME);
        // ??????????????????
        if (errorCallback != null) {
            sipSubscribe.addErrorSubscribe(callIdHeader.getCallId(), (eventResult -> {
                errorCallback.response(eventResult);
                sipSubscribe.removeErrorSubscribe(eventResult.callId);
            }));
        }
        // ????????????
        if (okCallback != null) {
            sipSubscribe.addOkSubscribe(callIdHeader.getCallId(), eventResult ->{
                okCallback.response(eventResult);
                sipSubscribe.removeOkSubscribe(eventResult.callId);
            });
        }
        clientTransaction.sendRequest();
        return clientTransaction;
    }

    /**
     * ?????????????????????
     *
     * @param cmdCode 		?????????
     * @param parameter1	??????1
     * @param parameter2	??????2
     * @param combineCode2	?????????2
     */
    public static String frontEndCmdString(int cmdCode, int parameter1, int parameter2, int combineCode2) {
        StringBuilder builder = new StringBuilder("A50F01");
        String strTmp;
        strTmp = String.format("%02X", cmdCode);
        builder.append(strTmp, 0, 2);
        strTmp = String.format("%02X", parameter1);
        builder.append(strTmp, 0, 2);
        strTmp = String.format("%02X", parameter2);
        builder.append(strTmp, 0, 2);
        strTmp = String.format("%X", combineCode2);
        builder.append(strTmp, 0, 1).append("0");
        //???????????????
        int checkCode = (0XA5 + 0X0F + 0X01 + cmdCode + parameter1 + parameter2 + (combineCode2 & 0XF0)) % 0X100;
        strTmp = String.format("%02X", checkCode);
        builder.append(strTmp, 0, 2);
        return builder.toString();
    }
}
