package com.yyh.common.constant;

/**
 * @author: yyh
 * @date: 2021-12-24 10:56
 * @description: GlobalServerType
 **/
public interface GlobalServerType {
    /**
     * 信令服务器
     */
    Integer SIGNAL=1000;

    /**
     * 通用信令网关
     */
    Integer SIGNAL_SWG = 1001;

    /**
     * 国标媒体服务器
     */
    Integer MEDIA_28181 = 1004;

    /**
     * 媒体服务器
     */
    Integer MEDIA=1005;

    /**
     * MQ服务器
     */
    Integer MQ=1006;
    /**
     * REDIS 服务器
     */
    Integer REDIS=1007;
    /**
     * PTTGateWay
     */
    Integer PTTGateWay=1008;
    /**
     * PCWSGateWay
     */
    Integer PCWSGateWay=1009;
    /**
     * PCWSMGateWay
     */
    Integer PCWSMGateWay=1010;
}
