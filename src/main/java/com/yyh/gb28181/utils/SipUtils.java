package com.yyh.gb28181.utils;

import gov.nist.javax.sip.address.AddressImpl;
import gov.nist.javax.sip.address.SipUri;
import gov.nist.javax.sip.header.Subject;

import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.message.Request;

/**
 * @author: yyh
 * @date: 2021-12-08 09:35
 * @description: SipUtils
 **/
public class SipUtils {
    public static String getUserIdFromFromHeader(Request request) {
        FromHeader fromHeader = (FromHeader)request.getHeader(FromHeader.NAME);
        return getUserIdFromFromHeader(fromHeader);
    }
    public static String getChannelIdFromHeader(Request request) {
        Header subject = request.getHeader("subject");
        return ((Subject) subject).getSubject().split(":")[0];
    }
    public static String getUserIdFromFromHeader(FromHeader fromHeader) {
        AddressImpl address = (AddressImpl)fromHeader.getAddress();
        SipUri uri = (SipUri) address.getURI();
        return uri.getUser();
    }
}
