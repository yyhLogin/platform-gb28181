package com.yyh.gb28181.handle.request;

import com.yyh.gb28181.constant.SipRequestConstant;
import com.yyh.gb28181.constant.VideoManagerConstant;
import gov.nist.javax.sip.SipProviderImpl;
import gov.nist.javax.sip.SipStackImpl;
import gov.nist.javax.sip.address.AddressImpl;
import gov.nist.javax.sip.address.SipUri;
import gov.nist.javax.sip.message.SIPRequest;
import gov.nist.javax.sip.stack.SIPServerTransaction;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import javax.annotation.Resource;
import javax.sip.*;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.io.ByteArrayInputStream;
import java.text.ParseException;

/**
 * @author: yyh
 * @date: 2021-11-30 18:44
 * @description: TT
 **/
public abstract class SipRequestProcessorParent {

    private final static Logger logger = LoggerFactory.getLogger(SipRequestProcessorParent.class);

    @Resource
    private SipProviderImpl tcpSipProvider;

    @Resource
    private SipProviderImpl udpSipProvider;

    /**
     * 根据 RequestEvent 获取 ServerTransaction
     * @param evt evt
     * @return ServerTransaction
     */
    public ServerTransaction getServerTransaction(RequestEvent evt) {
        Request request = evt.getRequest();
        ServerTransaction serverTransaction = evt.getServerTransaction();
        // 判断TCP还是UDP
        boolean isTcp = false;
        ViaHeader reqViaHeader = (ViaHeader) request.getHeader(ViaHeader.NAME);
        String transport = reqViaHeader.getTransport();
        if (transport.equals(SipRequestConstant.TCP)) {
            isTcp = true;
        }

        if (serverTransaction == null) {
            try {
                if (isTcp) {
                    SipStackImpl stack = (SipStackImpl)tcpSipProvider.getSipStack();
                    serverTransaction = (SIPServerTransaction) stack.findTransaction((SIPRequest)request, true);
                    if (serverTransaction == null) {
                        serverTransaction = tcpSipProvider.getNewServerTransaction(request);
                    }
                } else {
                    SipStackImpl stack = (SipStackImpl)udpSipProvider.getSipStack();
                    serverTransaction = (SIPServerTransaction) stack.findTransaction((SIPRequest)request, true);
                    if (serverTransaction == null) {
                        serverTransaction = udpSipProvider.getNewServerTransaction(request);
                    }
                }
            } catch (TransactionAlreadyExistsException | TransactionUnavailableException e) {
                logger.error(e.getMessage());
            }
        }
        return serverTransaction;
    }

    public AddressFactory getAddressFactory() {
        try {
            return SipFactory.getInstance().createAddressFactory();
        } catch (PeerUnavailableException e) {
            e.printStackTrace();
        }
        return null;
    }

    public HeaderFactory getHeaderFactory() {
        try {
            return SipFactory.getInstance().createHeaderFactory();
        } catch (PeerUnavailableException e) {
            e.printStackTrace();
        }
        return null;
    }

    public MessageFactory getMessageFactory() {
        try {
            return SipFactory.getInstance().createMessageFactory();
        } catch (PeerUnavailableException e) {
            e.printStackTrace();
        }
        return null;
    }

    /***
     * 回复状态码
     * 100 trying
     * 200 OK
     * 400
     * 404
     * @param evt
     * @throws SipException
     * @throws InvalidArgumentException
     * @throws ParseException
     */
    public void responseAck(RequestEvent evt, int statusCode) throws SipException, InvalidArgumentException, ParseException {
        Response response = getMessageFactory().createResponse(statusCode, evt.getRequest());
        ServerTransaction serverTransaction = getServerTransaction(evt);
        serverTransaction.sendResponse(response);
        if (statusCode >= HttpStatus.OK.value() && !SipRequestConstant.NOTIFY.equals(evt.getRequest().getMethod())) {
            if (serverTransaction.getDialog() != null) {
                serverTransaction.getDialog().delete();
            }
        }
    }

    public void responseAck(RequestEvent evt, int statusCode, String msg) throws SipException, InvalidArgumentException, ParseException {
        Response response = getMessageFactory().createResponse(statusCode, evt.getRequest());
        response.setReasonPhrase(msg);
        ServerTransaction serverTransaction = getServerTransaction(evt);
        serverTransaction.sendResponse(response);
        if (statusCode >= HttpStatus.OK.value() && !SipRequestConstant.NOTIFY.equals(evt.getRequest().getMethod())) {
            if (serverTransaction.getDialog() != null) {
                serverTransaction.getDialog().delete();
            }
        }
    }

    /**
     * 回复带sdp的200
     * @param evt
     * @param sdp
     * @throws SipException
     * @throws InvalidArgumentException
     * @throws ParseException
     */
    public void responseAck(RequestEvent evt, String sdp) throws SipException, InvalidArgumentException, ParseException {
        Response response = getMessageFactory().createResponse(Response.OK, evt.getRequest());
        SipFactory sipFactory = SipFactory.getInstance();
        ContentTypeHeader contentTypeHeader = sipFactory.createHeaderFactory().createContentTypeHeader("APPLICATION", "SDP");
        response.setContent(sdp, contentTypeHeader);

        SipURI sipURI = (SipURI)evt.getRequest().getRequestURI();

        Address concatAddress = sipFactory.createAddressFactory().createAddress(
                sipFactory.createAddressFactory().createSipURI(sipURI.getUser(),  sipURI.getHost()+":"+sipURI.getPort()
                ));
        response.addHeader(sipFactory.createHeaderFactory().createContactHeader(concatAddress));
        getServerTransaction(evt).sendResponse(response);
    }

    public Element getRootElement(RequestEvent evt) throws DocumentException {
        return getRootElement(evt, VideoManagerConstant.DEVICE_DEFAULT_CHARSET);
    }
    public Element getRootElement(RequestEvent evt, String charset) throws DocumentException {
        if (charset == null) {
            charset = VideoManagerConstant.DEVICE_DEFAULT_CHARSET;
        }
        Request request = evt.getRequest();
        SAXReader reader = new SAXReader();
        reader.setEncoding(charset);
        Document xml = reader.read(new ByteArrayInputStream(request.getRawContent()));
        return xml.getRootElement();
    }

    protected String getUserIdFromFromHeader(Request request) {
        FromHeader fromHeader = (FromHeader)request.getHeader(FromHeader.NAME);
        return getUserIdFromFromHeader(fromHeader);
    }
    protected String getUserIdFromFromHeader(FromHeader fromHeader) {
        AddressImpl address = (AddressImpl)fromHeader.getAddress();
        SipUri uri = (SipUri) address.getURI();
        return uri.getUser();
    }
}
