package com.yyh.gb28181.entity;

import com.yyh.web.entity.ParentPlatform;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author: yyh
 * @date: 2022-04-20 16:11
 * @description: ParentPlatformCatch
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParentPlatformCache implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 心跳未回复次数
     */
    private int keepAliveReply;

    /**
     * 注册未回复次数
     */
    private int registerAliveReply;

    private String callId;

    private ParentPlatform parentPlatform;
}
