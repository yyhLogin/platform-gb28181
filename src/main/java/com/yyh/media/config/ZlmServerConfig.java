package com.yyh.media.config;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author: yyh
 * @date: 2021-12-10 11:34
 * @description: ZlmServerConfig
 **/
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class ZlmServerConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    @JsonProperty(value = "api.apiDebug")
    private String apiDebug;

    @JsonProperty(value = "api.secret")
    private String apiSecret;

    @JsonProperty(value = "api.defaultSnap")
    private String apiDefaultSnap;

    @JsonProperty(value = "api.snapRoot")
    private String apiSnapRoot;

    @JsonProperty(value = "ffmpeg.bin")
    private String ffmpegBin;

    @JsonProperty(value = "ffmpeg.cmd")
    private String ffmpegCmd;

    @JsonProperty(value = "ffmpeg.log")
    private String ffmpegLog;

    @JsonProperty(value = "general.addMuteAudio")
    private String generalAddMuteAudio;

    @JsonProperty(value = "general.enableVhost")
    private String generalEnableVhost;

    @JsonProperty(value = "general.enable_audio")
    private String generalEnableAudio;

    @JsonProperty(value = "general.flowThreshold")
    private String generalFlowThreshold;

    @JsonProperty(value = "general.fmp4_demand")
    private String generalFmp4Demand;

    @JsonProperty(value = "general.hls_demand")
    private String generalHlsDemand;

    @JsonProperty(value = "general.maxStreamWaitMS")
    private String generalMaxStreamWaitMs;

    @JsonProperty(value = "general.mediaServerId")
    private String generalMediaServerId;

    /**
     * 该字段配置文件不存在,为服务器ip
     */
    @JsonProperty(value = "general.mediaServerIp")
    private String generalMediaServerIp;

    @JsonProperty(value = "general.mergeWriteMS")
    private String generalMergeWriteMs;

    @JsonProperty(value = "general.modifyStamp")
    private String generalModifyStamp;

    @JsonProperty(value = "general.publishToHls")
    private String generalPublishToHls;

    @JsonProperty(value = "general.publishToMP4")
    private String generalPublishToMp4;

    @JsonProperty(value = "general.resetWhenRePlay")
    private String generalResetWhenRePlay;

    @JsonProperty(value = "general.rtmp_demand")
    private String generalRtmpDemand;

    @JsonProperty(value = "general.rtsp_demand")
    private String generalRtspDemand;

    @JsonProperty(value = "general.streamNoneReaderDelayMS")
    private String generalStreamNoneReaderDelayMs;

    @JsonProperty(value = "general.ts_demand")
    private String generalTsDemand;

    @JsonProperty(value = "hls.broadcastRecordTs")
    private String hlsBroadcastRecordTs;

    @JsonProperty(value = "hls.deleteDelaySec")
    private String hlsDeleteDelaySec;

    @JsonProperty(value = "hls.fileBufSize")
    private String hlsFileBufSize;

    @JsonProperty(value = "hls.filePath")
    private String hlsFilePath;

    @JsonProperty(value = "hls.segDur")
    private String hlsSegDur;

    @JsonProperty(value = "hls.segNum")
    private String hlsSegNum;

    @JsonProperty(value = "hls.segRetain")
    private String hlsSegRetain;

    @JsonProperty(value = "hook.admin_params")
    private String hookAdminParams;

    @JsonProperty(value = "hook.alive_interval")
    private String hookAliveInterval;

    @JsonProperty(value = "hook.enable")
    private String hookEnable;

    @JsonProperty(value = "hook.on_flow_report")
    private String hookOnFlowReport;

    @JsonProperty(value = "hook.on_http_access")
    private String hookOnHttpAccess;

    @JsonProperty(value = "hook.on_play")
    private String hookOnPlay;

    @JsonProperty(value = "hook.on_publish")
    private String hookOnPublish;

    @JsonProperty(value = "hook.on_record_mp4")
    private String hookOnRecordMp4;

    @JsonProperty(value = "hook.on_record_ts")
    private String hookOnRecordTs;

    @JsonProperty(value = "hook.on_rtsp_auth")
    private String hookOnRtspAuth;

    @JsonProperty(value = "hook.on_rtsp_realm")
    private String hookOnRtspRealm;

    @JsonProperty(value = "hook.on_server_keepalive")
    private String hookOnServerKeepalive;

    @JsonProperty(value = "hook.on_server_started")
    private String hookOnServerStarted;

    @JsonProperty(value = "hook.on_shell_login")
    private String hookOnShellLogin;

    @JsonProperty(value = "hook.on_stream_changed")
    private String hookOnStreamChanged;

    @JsonProperty(value = "hook.on_stream_none_reader")
    private String hookOnStreamNoneReader;

    @JsonProperty(value = "hook.on_stream_not_found")
    private String hookOnStreamNotFound;

    @JsonProperty(value = "hook.timeoutSec")
    private String hookTimeoutSec;

    @JsonProperty(value = "http.charSet")
    private String httpCharSet;

    @JsonProperty(value = "http.dirMenu")
    private String httpDirMenu;

    @JsonProperty(value = "http.keepAliveSecond")
    private String httpKeepAliveSecond;

    @JsonProperty(value = "http.maxReqSize")
    private String httpMaxReqSize;

    @JsonProperty(value = "http.notFound")
    private String httpNotFound;

    @JsonProperty(value = "http.port")
    private int httpPort;

    @JsonProperty(value = "http.rootPath")
    private String httpRootPath;

    @JsonProperty(value = "http.sendBufSize")
    private String httpSendBufSize;

    @JsonProperty(value = "http.sslport")
    private int httpSslPort;

    @JsonProperty(value = "http.virtualPath")
    private String httpVirtualPath;

    @JsonProperty(value = "multicast.addrMax")
    private String multicastAddrMax;

    @JsonProperty(value = "multicast.addrMin")
    private String multicastAddrMin;

    @JsonProperty(value = "multicast.udpTTL")
    private String multicastUdpTtl;

    @JsonProperty(value = "record.appName")
    private String recordAppName;

    @JsonProperty(value = "record.fastStart")
    private String recordFastStart;

    @JsonProperty(value = "record.fileBufSize")
    private String recordFileBufSize;

    @JsonProperty(value = "record.filePath")
    private String recordFilePath;

    @JsonProperty(value = "record.fileRepeat")
    private String recordFileRepeat;

    @JsonProperty(value = "record.fileSecond")
    private String recordFileSecond;

    @JsonProperty(value = "record.sampleMS")
    private String recordFileSampleMs;

    @JsonProperty(value = "rtmp.handshakeSecond")
    private String rtmpHandshakeSecond;

    @JsonProperty(value = "rtmp.keepAliveSecond")
    private String rtmpKeepAliveSecond;

    @JsonProperty(value = "rtmp.modifyStamp")
    private String rtmpModifyStamp;

    @JsonProperty(value = "rtmp.port")
    private int rtmpPort;

    @JsonProperty(value = "rtmp.sslport")
    private int rtmpSslPort;

    @JsonProperty(value = "rtp.audioMtuSize")
    private String rtpAudioMtuSize;

    @JsonProperty(value = "rtp.rtpMaxSize")
    private String rtpMaxRtpSize;

    @JsonProperty(value = "rtp.videoMtuSize")
    private String rtpVideoMtuSize;

    @JsonProperty(value = "rtp_proxy.dumpDir")
    private String rtpProxyDumpDir;

    @JsonProperty(value = "rtp_proxy.port")
    private int rtpProxyPort;

    @JsonProperty(value = "rtp_proxy.timeoutSec")
    private String rtpProxyTimeoutSec;

    @JsonProperty(value = "rtsp.authBasic")
    private String rtspAuthBasic;

    @JsonProperty(value = "rtsp.directProxy")
    private String rtspDirectProxy;

    @JsonProperty(value = "rtsp.handshakeSecond")
    private String rtspHandshakeSecond;

    @JsonProperty(value = "rtsp.keepAliveSecond")
    private String rtspKeepAliveSecond;

    @JsonProperty(value = "rtsp.port")
    private int rtspPort;

    @JsonProperty(value = "rtsp.sslport")
    private int rtspSslPort;

    @JsonProperty(value = "shell.maxReqSize")
    private String shellMaxReqSize;

    @JsonProperty(value = "shell.port")
    private String shellPort;
}
