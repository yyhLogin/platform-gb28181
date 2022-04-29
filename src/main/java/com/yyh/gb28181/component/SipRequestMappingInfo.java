package com.yyh.gb28181.component;

import lombok.Builder;
import lombok.Data;

/**
 * @author: yyh
 * @date: 2021-11-25 19:01
 * @description: SipRequestMappingInfo
 **/
@Data
@Builder
public class SipRequestMappingInfo {
    public final String type;
    public SipRequestMappingInfo(String type) {
        this.type = type;
    }
}
