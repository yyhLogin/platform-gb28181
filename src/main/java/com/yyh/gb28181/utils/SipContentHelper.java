package com.yyh.gb28181.utils;

import com.yyh.gb28181.constant.SipRequestConstant;

/**
 * @author: yyh
 * @date: 2022-01-19 16:45
 * @description: SipContentHelper
 **/
public class SipContentHelper {
    /**
     * 生成实时流请求content
     * @param sessionId 媒体服务域id
     * @param ip 媒体服务器ip
     * @param port 媒体服务器端口
     * @param seniorSdp 是否拓展sdp
     * @param streamMode 流传输模式
     * @param sessionName sessionName
     * @param ssrc ssrc
     * @return String
     */
    public static String generateRealTimeMediaStreamInviteContent(String sessionId,
                                                                  String ip,
                                                                  Integer port,
                                                                  Boolean seniorSdp,
                                                                  String streamMode,
                                                                  String sessionName,
                                                                  String ssrc){
        StringBuilder sb = new StringBuilder();
        sb.append("v=0\r\n");
        sb.append("o=").append(sessionId).append(" 0 0 IN IP4 ").append(ip).append("\r\n");
        sb.append("s=").append(sessionName).append("\r\n");
        sb.append("c=IN IP4 ").append(ip).append("\r\n");
        sb.append("t=0 0\r\n");
        if (seniorSdp) {
            switch (streamMode) {
                // tcp被动模式
                case SipRequestConstant.TCP_PASSIVE:
                // tcp主动模式
                case SipRequestConstant.TCP_ACTIVE:
                    sb.append("m=video ").append(port).append(" TCP/RTP/AVP 96 126 125 99 34 98 97\r\n");
                    break;
                case SipRequestConstant.UDP:
                    sb.append("m=video ").append(port).append(" RTP/AVP 96 126 125 99 34 98 97\r\n");
                    break;
                default:
                    break;
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
                    sb.append("m=video ").append(port).append(" TCP/RTP/AVP 96 98 97\r\n");
                    break;
                case SipRequestConstant.UDP:
                    sb.append("m=video ").append(port).append(" RTP/AVP 96 98 97\r\n");
                    break;
                default:
                    break;
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
        sb.append("y=").append(ssrc).append("\r\n");
        return sb.toString();
    }

    /**
     *
     * @param id 媒体服务域id
     * @param sdpIp 媒体服务器ip
     * @param channelId 通道id
     * @param port 媒体服务器端口
     * @param seniorSdp 是否拓展sdp
     * @param streamMode 流传输模式
     * @param ssrc ssrc
     * @param bTime 开始时间
     * @param eTime 结束时间
     * @return String
     */
    public static String generatePlaybackMediaStreamInviteContent(String id,
                                                                  String sdpIp,
                                                                  String channelId,
                                                                  int port,
                                                                  Boolean seniorSdp,
                                                                  String streamMode,
                                                                  String ssrc,
                                                                  Long bTime,
                                                                  Long eTime) {
        StringBuilder content = new StringBuilder(200);
        content.append("v=0\r\n");
        content.append("o=").append(id).append(" 0 0 IN IP4 ").append(sdpIp).append("\r\n");
        content.append("s=Playback\r\n");
        content.append("u=").append(channelId).append(":0\r\n");
        content.append("c=IN IP4 ").append(sdpIp).append("\r\n");
        content.append("t=").append(bTime).append(" ").append(eTime).append("\r\n");
        streamMode = streamMode.toUpperCase();
        // tcp被动模式
        // tcp主动模式
        if (seniorSdp) {
            switch (streamMode) {
                case "TCP-PASSIVE":
                case "TCP-ACTIVE":
                    content.append("m=video ").append(port).append(" TCP/RTP/AVP 96 126 125 99 34 98 97\r\n");
                    break;
                case "UDP":
                    content.append("m=video ").append(port).append(" RTP/AVP 96 126 125 99 34 98 97\r\n");
                    break;
                default:
                    break;
            }
            content.append("a=recvonly\r\n");
            content.append("a=rtpmap:96 PS/90000\r\n");
            content.append("a=fmtp:126 profile-level-id=42e01e\r\n");
            content.append("a=rtpmap:126 H264/90000\r\n");
            content.append("a=rtpmap:125 H264S/90000\r\n");
            content.append("a=fmtp:125 profile-level-id=42e01e\r\n");
            content.append("a=rtpmap:99 MP4V-ES/90000\r\n");
            content.append("a=fmtp:99 profile-level-id=3\r\n");

        }else {
            switch (streamMode) {
                case SipRequestConstant.TCP_PASSIVE:
                case SipRequestConstant.TCP_ACTIVE:
                    content.append("m=video ").append(port).append(" TCP/RTP/AVP 96 98 97\r\n");
                    break;
                case "UDP":
                    content.append("m=video ").append(port).append(" RTP/AVP 96 98 97\r\n");
                    break;
                default:
                    break;
            }
            content.append("a=recvonly\r\n");
            content.append("a=rtpmap:96 PS/90000\r\n");
        }
        content.append("a=rtpmap:98 H264/90000\r\n");
        content.append("a=rtpmap:97 MPEG4/90000\r\n");
        if(SipRequestConstant.TCP_PASSIVE.equals(streamMode)){
            // tcp被动模式
            content.append("a=setup:passive\r\n");
            content.append("a=connection:new\r\n");
        }else if (SipRequestConstant.TCP_ACTIVE.equals(streamMode)) {
            // tcp主动模式
            content.append("a=setup:active\r\n");
            content.append("a=connection:new\r\n");
        }
        //ssrc
        content.append("y=").append(ssrc).append("\r\n");
        return content.toString();
    }

    /**
     *
     * @param id sip服务器域id
     * @param sdpIp 媒体服务器ip
     * @param channelId 通道id
     * @param port  媒体服务器端口
     * @param seniorSdp 是否拓展sdp
     * @param streamMode  流传输模式
     * @param ssrc  ssrc
     * @param bTime 开始时间
     * @param eTime 结束时间
     * @param downloadSpeed 下载速率
     * @return  String 返回构造字符串
     */
    public static String generateDownloadMediaStreamInviteContent(String id,
                                                                String sdpIp,
                                                                String channelId,
                                                                int port,
                                                                Boolean seniorSdp,
                                                                String streamMode,
                                                                String ssrc,
                                                                Long bTime,
                                                                Long eTime,
                                                                String downloadSpeed) {

        StringBuilder content = new StringBuilder(200);
        content.append("v=0\r\n");
        content.append("o=").append(id).append(" 0 0 IN IP4 ").append(sdpIp).append("\r\n");
        content.append("s=Download\r\n");
        content.append("u=").append(channelId).append(":0\r\n");
        content.append("c=IN IP4 ").append(sdpIp).append("\r\n");
        content.append("t=").append(bTime).append(" ").append(eTime).append("\r\n");
        streamMode = streamMode.toUpperCase();
        // tcp被动模式
        // tcp主动模式
        if (seniorSdp) {
            switch (streamMode) {
                case "TCP-PASSIVE":
                case "TCP-ACTIVE":
                    content.append("m=video ").append(port).append(" TCP/RTP/AVP 96 126 125 99 34 98 97\r\n");
                    break;
                case "UDP":
                    content.append("m=video ").append(port).append(" RTP/AVP 96 126 125 99 34 98 97\r\n");
                    break;
                default:
                    break;
            }
            content.append("a=recvonly\r\n");
            content.append("a=rtpmap:96 PS/90000\r\n");
            content.append("a=fmtp:126 profile-level-id=42e01e\r\n");
            content.append("a=rtpmap:126 H264/90000\r\n");
            content.append("a=rtpmap:125 H264S/90000\r\n");
            content.append("a=fmtp:125 profile-level-id=42e01e\r\n");
            content.append("a=rtpmap:99 MP4V-ES/90000\r\n");
            content.append("a=fmtp:99 profile-level-id=3\r\n");

        }else {
            switch (streamMode) {
                case "TCP-PASSIVE":
                case "TCP-ACTIVE":
                    content.append("m=video ").append(port).append(" TCP/RTP/AVP 96 98 97\r\n");
                    break;
                case "UDP":
                    content.append("m=video ").append(port).append(" RTP/AVP 96 98 97\r\n");
                    break;
                default:
                    break;
            }
            content.append("a=recvonly\r\n");
            content.append("a=rtpmap:96 PS/90000\r\n");
        }
        content.append("a=rtpmap:98 H264/90000\r\n");
        content.append("a=rtpmap:97 MPEG4/90000\r\n");
        if(SipRequestConstant.TCP_PASSIVE.equals(streamMode)){
            // tcp被动模式
            content.append("a=setup:passive\r\n");
            content.append("a=connection:new\r\n");
        }else if (SipRequestConstant.TCP_ACTIVE.equals(streamMode)) {
            // tcp主动模式
            content.append("a=setup:active\r\n");
            content.append("a=connection:new\r\n");
        }
        content.append("a=downloadspeed:").append(downloadSpeed).append("\r\n");
        //ssrc
        content.append("y=").append(ssrc).append("\r\n");
        return content.toString();
    }
}
