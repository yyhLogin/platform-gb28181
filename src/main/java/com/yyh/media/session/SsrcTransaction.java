package com.yyh.media.session;

import lombok.Data;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import java.io.Serializable;

/**
 * @author: yyh
 * @date: 2022-02-08 16:16
 * @description: SsrcTransaction
 **/
@Data
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
