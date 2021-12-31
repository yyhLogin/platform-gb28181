package com.yyh.gb28181.command.impl;

import com.yyh.gb28181.callback.SipCallback;
import com.yyh.gb28181.callback.SipSubscribe;
import com.yyh.gb28181.command.ISipCommander;
import com.yyh.gb28181.command.SipRequestHeaderProvider;
import com.yyh.gb28181.config.SipServerProperties;
import com.yyh.gb28181.constant.SipRequestConstant;
import com.yyh.gb28181.constant.VideoManagerConstant;
import com.yyh.media.config.SsrcConfig;
import com.yyh.media.config.ZlmServerConfig;
import com.yyh.media.entity.SsrcInfo;
import com.yyh.properties.GlobalConfigProperties;
import com.yyh.web.entity.GbDevice;
import com.yyh.web.entity.MediaServer;
import gov.nist.javax.sip.SipProviderImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sip.ClientTransaction;
import javax.sip.InvalidArgumentException;
import javax.sip.ResponseEvent;
import javax.sip.SipException;
import javax.sip.header.CallIdHeader;
import javax.sip.message.Request;
import java.text.ParseException;

/**
 * @author: yyh
 * @date: 2021-12-07 09:49
 * @description: SipCommander
 **/
@Slf4j
@Component
@RequiredArgsConstructor
public class SipCommander implements ISipCommander {

    private final SipProviderImpl tcpSipProvider;
    private final SipProviderImpl udpSipProvider;
    private final SipSubscribe sipSubscribe;
    private final SipRequestHeaderProvider headerProvider;
    private final GlobalConfigProperties properties;
    private final SipServerProperties sipServerProperties;

    /**
     * 查询设备信息
     *
     * @param device 设备
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
            log.error("[ sip ]:查询设备信息指令出现异常:{}",e.getMessage(),e);
            return false;
        }
        return true;
    }

    /**
     * 查询设备通道指令
     *
     * @param device device
     * @param errorEvent 错误回调
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
            log.error("[ sip ]:查询设备通道指令出现异常:{}",e.getMessage(),e);
            return false;
        }
        return true;
    }

    /**
     * 请求预览视频流
     *
     * @param device        设备信息
     * @param ssrcInfo      ssrc
     * @param channelId     通道信息
     * @param server        媒体服务信息
     * @param okCallback    成功回调
     * @param errorCallback 失败回调
     */
    @Override
    public void playRealtimeStream(GbDevice device,
                                   SsrcInfo ssrcInfo,
                                   String channelId,
                                   MediaServer server,
                                   SipCallback okCallback,
                                   SipCallback errorCallback) {
        String streamMode = device.getStreamMode().toUpperCase();
        StringBuilder sb = new StringBuilder();
        try {
            sb.append("v=0\r\n");
            sb.append("o=").append(sipServerProperties.getId()).append(" 0 0 IN IP4 ").append(server.getSdpIp()).append("\r\n");
            sb.append("s=Play\r\n");
            sb.append("c=IN IP4 ").append(server.getSdpIp()).append("\r\n");
            sb.append("t=0 0\r\n");
            if (properties.getSeniorSdp()) {
                switch (streamMode) {
                    // tcp被动模式
                    case SipRequestConstant.TCP_PASSIVE:
                        // tcp主动模式
                    case SipRequestConstant.TCP_ACTIVE:
                        sb.append("m=video ").append(ssrcInfo.getPort()).append(" TCP/RTP/AVP 96 126 125 99 34 98 97\r\n");
                        break;
                    case SipRequestConstant.UDP:
                        sb.append("m=video ").append(ssrcInfo.getPort()).append(" RTP/AVP 96 126 125 99 34 98 97\r\n");
                        break;
                    default:
                        return;
                }
                sb.append("a=recvonly\r\n");
                sb.append("a=rtpmap:96 PS/90000\r\n");
                sb.append("a=fmtp:126 profile-level-id=42e01e\r\n");
                sb.append("a=rtpmap:126 H264/90000\r\n");
                sb.append("a=rtpmap:125 H264S/90000\r\n");
                sb.append("a=fmtp:125 profile-level-id=42e01e\r\n");
                sb.append("a=rtpmap:99 MP4V-ES/90000\r\n");
                sb.append("a=fmtp:99 profile-level-id=3\r\n");
            }else {
                switch (streamMode) {
                    case SipRequestConstant.TCP_PASSIVE:
                    case SipRequestConstant.TCP_ACTIVE:
                        sb.append("m=video ").append(ssrcInfo.getPort()).append(" TCP/RTP/AVP 96 98 97\r\n");
                        break;
                    case SipRequestConstant.UDP:
                        sb.append("m=video ").append(ssrcInfo.getPort()).append(" RTP/AVP 96 98 97\r\n");
                        break;
                    default:
                        return;
                }
                sb.append("a=recvonly\r\n");
                sb.append("a=rtpmap:96 PS/90000\r\n");
            }
            sb.append("a=rtpmap:98 H264/90000\r\n");
            sb.append("a=rtpmap:97 MPEG4/90000\r\n");
            if(SipRequestConstant.TCP_PASSIVE.equals(streamMode)){
                // tcp被动模式
                sb.append("a=setup:passive\r\n");
                sb.append("a=connection:new\r\n");
            }else if (SipRequestConstant.TCP_ACTIVE.equals(streamMode)) {
                // tcp主动模式
                sb.append("a=setup:active\r\n");
                sb.append("a=connection:new\r\n");
            }
            //ssrc
            sb.append("y=").append(ssrcInfo.getSsrc()).append("\r\n");
            String tm = Long.toString(System.currentTimeMillis());

            CallIdHeader callIdHeader = device.getTransport().equals("TCP") ? tcpSipProvider.getNewCallId()
                    : udpSipProvider.getNewCallId();

            Request request = headerProvider.createInviteRequest(device, channelId, sb.toString(), null, "FromInvt" + tm, null, ssrcInfo.getSsrc(), callIdHeader);
            String finalStreamId = ssrcInfo.getStreamId();
            transmitRequest(device, request, (error -> {
//            streamSession.remove(device.getDeviceId(), channelId);
//            mediaServerService.releaseSsrc(mediaServerItem, ssrcInfo.getSsrc());
//            errorEvent.response(e);
            }), (ok ->{
//            streamSession.put(device.getDeviceId(), channelId ,ssrcInfo.getSsrc(), finalStreamId, mediaServerItem.getId(), ((ResponseEvent)e.event).getClientTransaction());
//            streamSession.put(device.getDeviceId(), channelId , e.dialog);
            }));
        }catch (Exception ex){
            log.error("");
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
        // 添加错误订阅
        if (errorCallback != null) {
            sipSubscribe.addErrorSubscribe(callIdHeader.getCallId(), (eventResult -> {
                errorCallback.response(eventResult);
                sipSubscribe.removeErrorSubscribe(eventResult.callId);
            }));
        }
        // 添加订阅
        if (okCallback != null) {
            sipSubscribe.addOkSubscribe(callIdHeader.getCallId(), eventResult ->{
                okCallback.response(eventResult);
                sipSubscribe.removeOkSubscribe(eventResult.callId);
            });
        }
        clientTransaction.sendRequest();
        return clientTransaction;
    }
}
