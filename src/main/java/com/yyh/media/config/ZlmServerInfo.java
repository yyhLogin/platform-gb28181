package com.yyh.media.config;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author: yyh
 * @date: 2021-12-21 14:55
 * @description: ZlMediaServerInfo
 **/
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class ZlmServerInfo extends ZlmServerConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private String ip;

    private String sdpIp;

    private String streamIp;

    private String hookIp;

    private LocalDateTime updateTime;

    private LocalDateTime createTime;

//    public ZlmServerInfo(ZlmServerConfig config){
//        this.createTime = LocalDateTime.now();
//        this.ip =
//    }
}
