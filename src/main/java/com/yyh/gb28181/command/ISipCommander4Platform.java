package com.yyh.gb28181.command;

import cn.hutool.core.io.BOMInputStream;
import com.yyh.gb28181.callback.SipCallback;
import com.yyh.web.entity.DeviceChannel;
import com.yyh.web.entity.ParentPlatform;
import com.yyh.web.entity.PlatformChannel;

import javax.sip.header.WWWAuthenticateHeader;
import java.util.List;

/**
 * @author: yyh
 * @date: 2022-04-19 16:22
 * @description: ISipCommander4Platform
 **/
public interface ISipCommander4Platform {

    /**
     * 注册上级平台
     * @param platform 平台参数
     * @param error 错误回调
     * @param ok 正确回调
     * @return boolean
     */
    boolean register(ParentPlatform platform, SipCallback error, SipCallback ok);

    /**
     * 注册上级平台
     * @param platform 上级平台参数
     * @param callId callId
     * @param www WWW
     * @param error 错误回调
     * @param ok 正确回调
     * @param registerAgain 再次注册
     * @return boolean
     */
    boolean register(ParentPlatform platform, String callId, WWWAuthenticateHeader www, SipCallback error , SipCallback ok, boolean registerAgain);

    /**
     * 响应上级平台查询设备信息指令
     * @param platform 上级平台信息
     * @param sn sn
     * @param tag tag
     * @return boolean
     */
    boolean deviceInfoResponse(ParentPlatform platform, String sn, String tag);


    /**
     * 向上级平台发送心跳
     * @param parentPlatform 上级平台信息
     * @param ok 成功回调
     * @return String
     */
    String keepalive(ParentPlatform parentPlatform,SipCallback ok);

    /**
     * 响应上级平台catalog指令
     * @param list catalog 集合
     * @param parentPlatform 平台信息
     * @param sn sn
     * @param tag tag
     * @return boolean
     */
    boolean catalogResponse(List<DeviceChannel> list, ParentPlatform parentPlatform, String sn, String tag);
}
