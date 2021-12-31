package com.yyh.media.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: yyh
 * @date: 2021-12-29 10:49
 * @description: SsrcInfo
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SsrcInfo {
    private int port;
    private String ssrc;
    private String streamId;
}
