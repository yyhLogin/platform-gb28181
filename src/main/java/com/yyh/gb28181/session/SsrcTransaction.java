package com.yyh.gb28181.session;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import java.io.Serializable;

/**
 * @author: yyh
 * @date: 2022-01-18 15:05
 * @description: SsrcTransaction sip 流会话session
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SsrcTransaction implements Serializable {

    private static final long serialVersionUID = 1L;

    private String deviceId;
    private String channelId;
    private String ssrc;
    private String streamId;
    private ClientTransaction transaction;
    private Dialog dialog;
    private String mediaServerId;
}
