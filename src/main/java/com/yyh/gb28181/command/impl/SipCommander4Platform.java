package com.yyh.gb28181.command.impl;

import com.yyh.gb28181.callback.SipCallback;
import com.yyh.gb28181.callback.SipSubscribe;
import com.yyh.gb28181.command.ISipCommander4Platform;
import com.yyh.gb28181.command.SipRequestHeaderProvider4Platform;
import com.yyh.gb28181.constant.SipRequestConstant;
import com.yyh.gb28181.service.IRedisCacheStorage;
import com.yyh.gb28181.service.RedisService;
import com.yyh.web.entity.DeviceChannel;
import com.yyh.web.entity.ParentPlatform;
import gov.nist.javax.sip.SipProviderImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import javax.sip.header.CallIdHeader;
import javax.sip.header.WWWAuthenticateHeader;
import javax.sip.message.Request;
import java.text.ParseException;
import java.util.List;
import java.util.UUID;

/**
 * @author: yyh
 * @date: 2022-04-19 16:22
 * @description: SipCommander4Platform
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class SipCommander4Platform implements ISipCommander4Platform {

    private final SipProviderImpl tcpSipProvider;
    private final SipProviderImpl udpSipProvider;

    private final SipRequestHeaderProvider4Platform headerProvider4Platform;

    private final RedisService redisService;

    private final SipSubscribe sipSubscribe;

    private final IRedisCacheStorage redisCacheStorage;

    /**
     * 注册上级平台
     *
     * @param platform 平台参数
     * @param error    错误回调
     * @param ok       正确回调
     * @return boolean
     */
    @Override
    public boolean register(ParentPlatform platform, SipCallback error, SipCallback ok) {
        return register(platform,null,null,error,ok,false);
    }

    /**
     * 注册上级平台
     *
     * @param platform      上级平台参数
     * @param callId        callId
     * @param www           WWW
     * @param error         错误回调
     * @param ok            正确回调
     * @param registerAgain 再次注册
     * @return boolean
     */
    @Override
    public boolean register(ParentPlatform platform, String callId, WWWAuthenticateHeader www, SipCallback error, SipCallback ok, boolean registerAgain) {
        try {
            Request request;
            String tm = Long.toString(System.currentTimeMillis());
            if (!registerAgain ) {
                //callId
                CallIdHeader callIdHeader = null;
                if(platform.getTransport().equals(SipRequestConstant.TCP)) {
                    callIdHeader = tcpSipProvider.getNewCallId();
                }
                if(platform.getTransport().equals(SipRequestConstant.UDP)) {
                    callIdHeader = udpSipProvider.getNewCallId();
                }
                request = headerProvider4Platform.createRegisterRequest(platform,
                        redisService.increment(Request.REGISTER), "FromRegister" + tm,
                        "z9hG4bK-" + UUID.randomUUID().toString().replace("-", ""), callIdHeader);
                // 将 callId 写入缓存， 等注册成功可以更新状态
                assert callIdHeader != null;
                String callIdFromHeader = callIdHeader.getCallId();
                redisCacheStorage.updatePlatformRegisterInfo(callIdFromHeader, platform.getServerGbId());
                sipSubscribe.addErrorSubscribe(callIdHeader.getCallId(), (event)->{
                    if (event != null) {
                        log.info("向上级平台 [ {} ] 注册发生错误： {} ",platform.getServerGbId(),event.msg);
                    }
                    redisCacheStorage.delPlatformRegisterInfo(callIdFromHeader);
                    if (error != null ) {
                        error.response(event);
                    }
                });

            }else {
                CallIdHeader callIdHeader = platform.getTransport().equals(SipRequestConstant.TCP) ? tcpSipProvider.getNewCallId()
                        : udpSipProvider.getNewCallId();
                request = headerProvider4Platform.createRegisterRequest(platform, "FromRegister" + tm, null, callId, www, callIdHeader);
            }
            transmitRequest(platform, request, null, ok);
            return true;
        } catch (ParseException | InvalidArgumentException | SipException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 响应上级平台查询设备信息指令
     *
     * @param platform 上级平台信息
     * @param sn       sn
     * @param tag      tag
     * @return boolean
     */
    @Override
    public boolean deviceInfoResponse(ParentPlatform platform, String sn, String tag) {
        if (platform == null) {
            return false;
        }
        try {
            String characterSet = platform.getCharacterSet();
            String deviceInfoXml = "<?xml version=\"1.0\" encoding=\"" + characterSet + "\"?>\r\n" +
                    "<Response>\r\n" +
                    "<CmdType>DeviceInfo</CmdType>\r\n" +
                    "<SN>" + sn + "</SN>\r\n" +
                    "<DeviceID>" + platform.getDeviceGbId() + "</DeviceID>\r\n" +
                    "<DeviceName>" + platform.getName() + "</DeviceName>\r\n" +
                    "<Manufacturer>jiu_jia</Manufacturer>\r\n" +
                    "<Model>gb28181</Model>\r\n" +
                    "<Firmware>1.0.0</Firmware>\r\n" +
                    "<Result>OK</Result>\r\n" +
                    "</Response>\r\n";
            CallIdHeader callIdHeader = platform.getTransport().equals(SipRequestConstant.TCP) ? tcpSipProvider.getNewCallId()
                    : udpSipProvider.getNewCallId();
            Request request = headerProvider4Platform.createMessageRequest(platform, deviceInfoXml, tag, callIdHeader);
            transmitRequest(platform, request);
        } catch (SipException | ParseException | InvalidArgumentException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 向上级平台发送心跳
     *
     * @param parentPlatform 上级平台信息
     * @param ok 成功回调
     * @return String
     */
    @Override
    public String keepalive(ParentPlatform parentPlatform,SipCallback ok) {
        String callId = null;
        try {
            String characterSet = parentPlatform.getCharacterSet();
            String keepaliveXml = "<?xml version=\"1.0\" encoding=\"" + characterSet + "\"?>\r\n" +
                    "<Notify>\r\n" +
                    "<CmdType>Keepalive</CmdType>\r\n" +
                    "<SN>" + (int) ((Math.random() * 9 + 1) * 100000) + "</SN>\r\n" +
                    "<DeviceID>" + parentPlatform.getDeviceGbId() + "</DeviceID>\r\n" +
                    "<Status>OK</Status>\r\n" +
                    "</Notify>\r\n";

            CallIdHeader callIdHeader = parentPlatform.getTransport().equals(SipRequestConstant.TCP) ? tcpSipProvider.getNewCallId()
                    : udpSipProvider.getNewCallId();

            Request request = headerProvider4Platform.createKeepaliveMessageRequest(
                    parentPlatform,
                    keepaliveXml,
                    "z9hG4bK-" + UUID.randomUUID().toString().replace("-", ""),
                    UUID.randomUUID().toString().replace("-", ""),
                    null,
                    callIdHeader);
            transmitRequest(parentPlatform, request,null,ok);
            callId = callIdHeader.getCallId();
        } catch (ParseException | InvalidArgumentException | SipException e) {
            e.printStackTrace();
        }
        return callId;
    }

    /**
     * 响应上级平台catalog指令
     *
     * @param list           catalog 集合
     * @param parentPlatform 平台信息
     * @param sn             sn
     * @param tag            tag
     * @return boolean
     */
    @Override
    public boolean catalogResponse(List<DeviceChannel> list, ParentPlatform parentPlatform, String sn, String tag) {
        sendCatalogResponse(list,parentPlatform,sn,tag,0);
        return true;
    }

    private void sendCatalogResponse(List<DeviceChannel> channels, ParentPlatform parentPlatform, String sn, String fromTag, int index) {
        if (index >= channels.size()) {
            return;
        }
        try {
            List<DeviceChannel> deviceChannels;
            if (index + parentPlatform.getCatalogGroup() < channels.size()) {
                deviceChannels = channels.subList(index, index + parentPlatform.getCatalogGroup());
            }else {
                deviceChannels = channels.subList(index, channels.size());
            }
            String catalogXml = getCatalogXml(deviceChannels, sn, parentPlatform, channels.size());
            // callid
            CallIdHeader callIdHeader = parentPlatform.getTransport().equals(SipRequestConstant.TCP) ? tcpSipProvider.getNewCallId()
                    : udpSipProvider.getNewCallId();

            Request request = headerProvider4Platform.createMessageRequest(parentPlatform, catalogXml, fromTag, callIdHeader);
            transmitRequest(parentPlatform, request, null, eventResult -> {
                int indexNext = index + parentPlatform.getCatalogGroup();
                sendCatalogResponse(channels, parentPlatform, sn, fromTag, indexNext);
            });
        } catch (SipException | ParseException | InvalidArgumentException e) {
            e.printStackTrace();
        }
    }
    private String getCatalogXml(List<DeviceChannel> channels, String sn, ParentPlatform parentPlatform, int size) {
        String characterSet = parentPlatform.getCharacterSet();
        StringBuilder catalogXml = new StringBuilder(600);
        catalogXml.append("<?xml version=\"1.0\" encoding=\"").append(characterSet).append("\"?>\r\n");
        catalogXml.append("<Response>\r\n");
        catalogXml.append("<CmdType>Catalog</CmdType>\r\n");
        catalogXml.append("<SN>").append(sn).append("</SN>\r\n");
        catalogXml.append("<DeviceID>").append(parentPlatform.getDeviceGbId()).append("</DeviceID>\r\n");
        catalogXml.append("<SumNum>").append(size).append("</SumNum>\r\n");
        catalogXml.append("<DeviceList Num=\"").append(channels.size()).append("\">\r\n");
        if (channels.size() > 0) {
            for (DeviceChannel channel : channels) {
                catalogXml.append("<Item>\r\n");
                catalogXml.append("<DeviceID>").append(channel.getChannelId()).append("</DeviceID>\r\n");
                catalogXml.append("<Name>").append(channel.getName()).append("</Name>\r\n");
                catalogXml.append("<Manufacturer>").append(channel.getManufacture()).append("</Manufacturer>\r\n");
                catalogXml.append("<Parental>").append(channel.getParental()).append("</Parental>\r\n");
                if (channel.getParentId() != null) {
                    catalogXml.append("<ParentID>").append(channel.getParentId()).append("</ParentID>\r\n");
                }
                catalogXml.append("<RegisterWay>").append(channel.getRegisterWay()).append("</RegisterWay>\r\n");
                catalogXml.append("<Status>").append(channel.getStatus() == 0 ? "OFF" : "ON").append("</Status>\r\n");
                catalogXml.append("<Secrecy>").append(channel.getSecrecy()).append("</Secrecy>\r\n");
                if (channel.getChannelType() != 2) { // 业务分组/虚拟组织/行政区划 不设置以下字段
                    catalogXml.append("<Model>").append(channel.getModel()).append("</Model>\r\n");
                    catalogXml.append("<Owner>").append(channel.getOwner()).append("</Owner>\r\n");
                    catalogXml.append("<CivilCode>").append(channel.getCivilCode()).append("</CivilCode>\r\n");
                    catalogXml.append("<Address>").append(channel.getAddress()).append("</Address>\r\n");
                    catalogXml.append("<Longitude>").append(channel.getLongitude()).append("</Longitude>\r\n");
                    catalogXml.append("<Latitude>").append(channel.getLatitude()).append("</Latitude>\r\n");
                    catalogXml.append("<IPAddress>").append(channel.getIpAddress()).append("</IPAddress>\r\n");
                    catalogXml.append("<Port>").append(channel.getPort()).append("</Port>\r\n");
                    catalogXml.append("<Info>\r\n");
                    catalogXml.append("<PTZType>").append(channel.getPtzType()).append("</PTZType>\r\n");
                    catalogXml.append("</Info>\r\n");
                }

                catalogXml.append("</Item>\r\n");
            }
        }

        catalogXml.append("</DeviceList>\r\n");
        catalogXml.append("</Response>\r\n");
        return catalogXml.toString();
    }

    private void transmitRequest(ParentPlatform parentPlatform, Request request) throws SipException {
        transmitRequest(parentPlatform, request, null, null);
    }

    private void transmitRequest(ParentPlatform parentPlatform, Request request, SipCallback errorEvent) throws SipException {
        transmitRequest(parentPlatform, request, errorEvent, null);
    }

    private void transmitRequest(ParentPlatform parentPlatform, Request request, SipCallback errorEvent , SipCallback okEvent) throws SipException {
        log.debug("\n发送消息：\n{}", request);
        if(SipRequestConstant.TCP.equals(parentPlatform.getTransport())) {
            tcpSipProvider.sendRequest(request);
        } else if(SipRequestConstant.UDP.equals(parentPlatform.getTransport())) {
            udpSipProvider.sendRequest(request);
        }
        CallIdHeader callIdHeader = (CallIdHeader)request.getHeader(CallIdHeader.NAME);
        // 添加错误订阅
        if (errorEvent != null) {
            sipSubscribe.addErrorSubscribe(callIdHeader.getCallId(), errorEvent);
        }
        // 添加订阅
        if (okEvent != null) {
            sipSubscribe.addOkSubscribe(callIdHeader.getCallId(), okEvent);
        }
    }
}
