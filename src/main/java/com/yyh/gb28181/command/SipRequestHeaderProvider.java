package com.yyh.gb28181.command;

import com.yyh.gb28181.config.SipServerProperties;
import com.yyh.media.constants.MediaConstant;
import com.yyh.web.entity.GbDevice;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.sip.InvalidArgumentException;
import javax.sip.PeerUnavailableException;
import javax.sip.SipFactory;
import javax.sip.address.Address;
import javax.sip.address.SipURI;
import javax.sip.header.*;
import javax.sip.message.Request;
import java.text.ParseException;
import java.util.ArrayList;

/**
 * @author: yyh
 * @date: 2021-12-07 10:04
 * @description: SIPRequestHeaderProvider
 **/
@Component
@RequiredArgsConstructor
public class SipRequestHeaderProvider {

    private final SipServerProperties sipConfig;
    private final SipFactory sipFactory;
    private final RedisTemplate<String,String> redisTemplate;

    public Request createMessageRequest(GbDevice device, String content, String viaTag, String fromTag, String toTag, CallIdHeader callIdHeader) throws ParseException, InvalidArgumentException, PeerUnavailableException {
        Request request = null;
        // sip uri
        SipURI requestUri = sipFactory.createAddressFactory().createSipURI(device.getGbId(), device.getHostAddress());
        // via
        ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
        ViaHeader viaHeader = sipFactory.createHeaderFactory().createViaHeader(sipConfig.getIp(), sipConfig.getPort(), device.getTransport(), viaTag);
        viaHeader.setRPort();
        viaHeaders.add(viaHeader);
        // from
        SipURI fromSipUri = sipFactory.createAddressFactory().createSipURI(sipConfig.getId(),
                sipConfig.getIp() + ":" + sipConfig.getPort());
        Address fromAddress = sipFactory.createAddressFactory().createAddress(fromSipUri);
        FromHeader fromHeader = sipFactory.createHeaderFactory().createFromHeader(fromAddress, fromTag);
        // to
        SipURI toSipUri = sipFactory.createAddressFactory().createSipURI(device.getGbId(), sipConfig.getDomain());
        Address toAddress = sipFactory.createAddressFactory().createAddress(toSipUri);
        ToHeader toHeader = sipFactory.createHeaderFactory().createToHeader(toAddress, toTag);

        // Forwards
        MaxForwardsHeader maxForwards = sipFactory.createHeaderFactory().createMaxForwardsHeader(70);
        // ceq
        CSeqHeader cSeqHeader = sipFactory.createHeaderFactory().createCSeqHeader(1L, Request.MESSAGE);

        request = sipFactory.createMessageFactory().createRequest(requestUri, Request.MESSAGE, callIdHeader, cSeqHeader, fromHeader,
                toHeader, viaHeaders, maxForwards);
        ContentTypeHeader contentTypeHeader = sipFactory.createHeaderFactory().createContentTypeHeader("APPLICATION", "MANSCDP+xml");
        request.setContent(content, contentTypeHeader);
        return request;
    }

    public Request createInviteRequest(GbDevice device, String channelId, String content, String viaTag, String fromTag, String toTag, String ssrc, CallIdHeader callIdHeader) throws ParseException, InvalidArgumentException, PeerUnavailableException {
        Request request = null;
        //请求行
        SipURI requestLine = sipFactory.createAddressFactory().createSipURI(channelId, device.getHostAddress());
        //via
        ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
        ViaHeader viaHeader = sipFactory.createHeaderFactory().createViaHeader(sipConfig.getIp(), sipConfig.getPort(), device.getTransport(), viaTag);
        viaHeader.setRPort();
        viaHeaders.add(viaHeader);

        //from
        SipURI fromSipURI = sipFactory.createAddressFactory().createSipURI(sipConfig.getId(),sipConfig.getDomain());
        Address fromAddress = sipFactory.createAddressFactory().createAddress(fromSipURI);
        FromHeader fromHeader = sipFactory.createHeaderFactory().createFromHeader(fromAddress, fromTag); //必须要有标记，否则无法创建会话，无法回应ack
        //to
        SipURI toSipURI = sipFactory.createAddressFactory().createSipURI(channelId,sipConfig.getDomain());
        Address toAddress = sipFactory.createAddressFactory().createAddress(toSipURI);
        ToHeader toHeader = sipFactory.createHeaderFactory().createToHeader(toAddress,null);

        //Forwards
        MaxForwardsHeader maxForwards = sipFactory.createHeaderFactory().createMaxForwardsHeader(70);

        //ceq
        CSeqHeader cSeqHeader = sipFactory.createHeaderFactory().createCSeqHeader(this.getCSeq(Request.INVITE), Request.INVITE);
        request = sipFactory.createMessageFactory().createRequest(requestLine, Request.INVITE, callIdHeader, cSeqHeader,fromHeader, toHeader, viaHeaders, maxForwards);

        Address concatAddress = sipFactory.createAddressFactory().createAddress(sipFactory.createAddressFactory().createSipURI(sipConfig.getId(), sipConfig.getIp()+":"+sipConfig.getPort()));
        // Address concatAddress = sipFactory.createAddressFactory().createAddress(sipFactory.createAddressFactory().createSipURI(sipConfig.getId(), device.getHost().getIp()+":"+device.getHost().getPort()));
        request.addHeader(sipFactory.createHeaderFactory().createContactHeader(concatAddress));
        // Subject
        SubjectHeader subjectHeader = sipFactory.createHeaderFactory().createSubjectHeader(String.format("%s:%s,%s:%s", channelId, ssrc, sipConfig.getId(), 0));
        request.addHeader(subjectHeader);
        ContentTypeHeader contentTypeHeader = sipFactory.createHeaderFactory().createContentTypeHeader("APPLICATION", "SDP");
        request.setContent(content, contentTypeHeader);
        return request;
    }

    /**
     * 生成 CSeq
     * @param method method
     * @return Long
     */
    private Long getCSeq(String method){
        String key = MediaConstant.SEQ_SERVER +  method;
        long result =  redisTemplate.opsForValue().increment(key, 1L);
        if (result > Integer.MAX_VALUE) {
            redisTemplate.opsForValue().increment(key, 1L);
            result = 1;
        }
        return result;
    }
}


