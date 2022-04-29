package com.yyh.media.session;

import gov.nist.javax.sip.stack.SIPDialog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: yyh
 * @date: 2022-01-20 15:03
 * @description: StreamSessionManager
 * 媒体流session管理
 **/
@Slf4j
@Component
public class StreamSessionManager {

    private static final Map<String,SsrcTransaction> MAP = new ConcurrentHashMap<>();

    public void put(String deviceId, String channelId ,String ssrc, String streamId, String mediaServerId, ClientTransaction transaction){
        SsrcTransaction ssrcTransaction = new SsrcTransaction();
        ssrcTransaction.setDeviceId(deviceId);
        ssrcTransaction.setChannelId(channelId);
        ssrcTransaction.setStreamId(streamId);
        ssrcTransaction.setTransaction(transaction);
        ssrcTransaction.setSsrc(ssrc);
        ssrcTransaction.setMediaServerId(mediaServerId);
        MAP.put(deviceId+"_"+channelId,ssrcTransaction);
        log.info("stream session size -> {}",MAP.size());
    }

    public void put(String deviceId, String channelId , Dialog dialog){
        SsrcTransaction ssrcTransaction = getSsrcTransaction(deviceId, channelId);
        if (ssrcTransaction != null) {
            ssrcTransaction.setDialog(dialog);
        }
        MAP.put(deviceId+"_"+channelId, ssrcTransaction);
        log.info("stream session size -> {}",MAP.size());
    }
    public ClientTransaction getTransaction(String deviceId, String channelId){
        SsrcTransaction ssrcTransaction = getSsrcTransaction(deviceId, channelId);
        if (ssrcTransaction == null) {
            return null;
        }
        return ssrcTransaction.getTransaction();
    }

    public SIPDialog getDialog(String deviceId, String channelId){
        SsrcTransaction ssrcTransaction = getSsrcTransaction(deviceId, channelId);
        if (ssrcTransaction == null) {
            return null;
        }
        return (SIPDialog) ssrcTransaction.getDialog();
    }

    public SsrcTransaction getSsrcTransaction(String deviceId, String channelId){
        return MAP.get(deviceId+"_"+channelId);
    }

    public String getStreamId(String deviceId, String channelId){
        SsrcTransaction ssrcTransaction = getSsrcTransaction(deviceId, channelId);
        if (ssrcTransaction == null) {
            return null;
        }
        return ssrcTransaction.getStreamId();
    }
    public String getMediaServerId(String deviceId, String channelId){
        SsrcTransaction ssrcTransaction = getSsrcTransaction(deviceId, channelId);
        if (ssrcTransaction == null) {
            return null;
        }
        return ssrcTransaction.getMediaServerId();
    }

    public String getSSRC(String deviceId, String channelId){
        SsrcTransaction ssrcTransaction = getSsrcTransaction(deviceId, channelId);
        if (ssrcTransaction == null) {
            return null;
        }
        return ssrcTransaction.getSsrc();
    }

    public void remove(String deviceId, String channelId) {
        SsrcTransaction ssrcTransaction = getSsrcTransaction(deviceId, channelId);
        if (ssrcTransaction == null) {
            return;
        }
        MAP.remove(deviceId+"_"+channelId);
        log.info("stream session size -> {}",MAP.size());
    }

//    public List<SsrcTransaction> getAllSsrc() {
//        List<Object> ssrcTransactionKeys = redisUtil.scan(String.format("%s_*_*", VideoManagerConstants.MEDIA_TRANSACTION_USED_PREFIX+ userSetup.getServerId() + "_" ));
//        List<SsrcTransaction> result= new ArrayList<>();
//        for (int i = 0; i < ssrcTransactionKeys.size(); i++) {
//            String key = (String)ssrcTransactionKeys.get(i);
//            SsrcTransaction ssrcTransaction = (SsrcTransaction)redisUtil.get(key);
//            result.add(ssrcTransaction);
//        }
//        return result;
//    }
}
