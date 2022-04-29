package com.yyh.gb28181.handle.request.processor;

import com.yyh.gb28181.annotation.SipMapping;
import com.yyh.gb28181.annotation.SipProcess;
import com.yyh.gb28181.constant.SipRequestConstant;
import com.yyh.gb28181.handle.request.service.SipRequestProcessorService;
import com.yyh.gb28181.handle.request.service.message.SipMessageRequestProcessor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.sip.PeerUnavailableException;
import javax.sip.RequestEvent;

/**
 * @author: yyh
 * @date: 2021-11-24 15:16
 * @description: RegisterRequestProcessor
 **/
@Slf4j
@SipMapping(SipRequestConstant.REQUEST)
public class RequestProcessor{

    @Resource
    private SipRequestProcessorService sipService;

    @Resource
    private SipMessageRequestProcessor messageService;

    /**
     * 注册
     * @param requestEvent requestEvent
     */
    @SipProcess(method = SipRequestConstant.REGISTER)
    public void register(RequestEvent requestEvent){
        if (requestEvent==null){
            return;
        }
        sipService.register(requestEvent);
    }

    /**
     * keepalive 心跳
     * @param requestEvent requestEvent
     */
    @SipProcess(method = SipRequestConstant.MESSAGE_NOTIFY_KEEPALIVE)
    public void messageKeepalive(RequestEvent requestEvent){
        sipService.messageKeepalive(requestEvent);
    }

    /**
     * device info 设备信息
     * @param requestEvent requestEvent
     */
    @SipProcess(method = SipRequestConstant.MESSAGE_RESPONSE_DEVICEINFO)
    public void messageDeviceInfo(RequestEvent requestEvent){
        sipService.messageResponseDeviceInfo(requestEvent);
    }

    /**
     * 录像查询
     * @param requestEvent requestEvent
     */
    @SipProcess(method = SipRequestConstant.MESSAGE_RESPONSE_RECORDINFO)
    public void messageRecordInfo(RequestEvent requestEvent){
        sipService.messageResponseRecordInfo(requestEvent);
    }

    /**
     * Catalog 设备通道信息
     * @param requestEvent requestEvent
     */
    @SipProcess(method = SipRequestConstant.MESSAGE_RESPONSE_CATALOG)
    public void messageCatalog(RequestEvent requestEvent){
        messageService.messageResponseCatalog(requestEvent);
    }

    /**
     * 查询设备状态信息响应
     * @param requestEvent requestEvent
     */
    @SipProcess(method = SipRequestConstant.MESSAGE_RESPONSE_DEVICESTATUS)
    public void messageResponseDeviceStatus(RequestEvent requestEvent){
        messageService.messageResponseDeviceStatus(requestEvent);
    }

    /**
     * 设备上报位置信息
     * @param requestEvent requestEvent
     */
    @SipProcess(method = SipRequestConstant.MESSAGE_NOTIFY_MOBILE_POSITION)
    public void messageMobilePosition(RequestEvent requestEvent){
        sipService.messageMobilePosition(requestEvent);
    }

    /**
     * 视音频回放完成
     * @param requestEvent requestEvent
     */
    @SipProcess(method = SipRequestConstant.MESSAGE_NOTIFY_MEDIASTATUS)
    public void messageNotifyMediaStatus(RequestEvent requestEvent){
        sipService.messageNotifyMediaStatus(requestEvent);
    }

    /**
     * 处理订阅的位置信息
     * @param requestEvent requestEvent
     */
    @SipProcess(method = SipRequestConstant.NOTIFY_MOBILEPOSITION)
    public void notifyMobilePosition(RequestEvent requestEvent){
        sipService.notifyMobilePosition(requestEvent);
    }

    /**
     * 报警通知
     * @param requestEvent requestEvent
     */
    @SipProcess(method = SipRequestConstant.NOTIFY_NOTIFY_ALARM)
    public void notifyAlarm(RequestEvent requestEvent){
        sipService.notifyAlarm(requestEvent);
    }

    /**
     * 报警查询响应
     * @param requestEvent requestEvent
     */
    @SipProcess(method = SipRequestConstant.MESSAGE_RESPONSE_ALARM)
    public void messageResponseAlarm(RequestEvent requestEvent){
        sipService.messageResponseAlarm(requestEvent);
    }

    /**
     * 设备配置查询响应
     * @param requestEvent requestEvent
     */
    @SipProcess(method = SipRequestConstant.MESSAGE_RESPONSE_CONFIGDOWNLOAD)
    public void messageResponseConfigDownload(RequestEvent requestEvent){
        sipService.messageResponseConfigDownload(requestEvent);
    }

    /**
     * 设备配置设定响应
     * @param requestEvent requestEvent
     */
    @SipProcess(method = SipRequestConstant.MESSAGE_RESPONSE_DEVICECONFIG)
    public void messageResponseDeviceConfig(RequestEvent requestEvent){
        sipService.messageResponseDeviceConfig(requestEvent);
    }

    /**
     * 设备控制响应
     * @param requestEvent requestEvent
     */
    @SipProcess(method = SipRequestConstant.MESSAGE_RESPONSE_DEVICECONTROL)
    public void messageResponseDeviceControl(RequestEvent requestEvent){
        sipService.messageResponseDeviceControl(requestEvent);
    }


    /**
     * 设备预置位查询响应
     * @param requestEvent requestEvent
     */
    @SipProcess(method = SipRequestConstant.MESSAGE_RESPONSE_PRESETQUERY)
    public void messageResponsePresetquery(RequestEvent requestEvent){
        sipService.messageResponsePresetquery(requestEvent);
    }

    //***************************和上级平台交互*****************************

    /**
     * 上级平台查询设备信息
     * @param requestEvent requestEvent
     */
    @SipProcess(method = SipRequestConstant.MESSAGE_QUERY_DEVICEINFO)
    public void messageQueryDeviceInfo(RequestEvent requestEvent){
        sipService.messageQueryDeviceInfo(requestEvent);
    }

    /**
     * 上级平台查询catalog信息
     * @param requestEvent requestEvent
     */
    @SipProcess(method = SipRequestConstant.MESSAGE_QUERY_CATALOG)
    public void messageQueryCatalog(RequestEvent requestEvent){
        sipService.messageQueryCatalog(requestEvent);
    }

    /**
     * 平台点播
     * @param requestEvent requestEvent
     */
    @SipProcess(method = SipRequestConstant.INVITE)
    public void messageInvite(RequestEvent requestEvent){
        sipService.messageInvite(requestEvent);
    }
}
