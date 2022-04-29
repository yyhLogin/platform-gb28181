package com.yyh.media.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;

/**
 * @author: yyh
 * @date: 2022-04-12 10:52
 * @description: SnowflakeUtil
 **/
public class SnowflakeUtil {

    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake(1, 1);

    /**
     * 获取雪花算法id
     * @return long
     */
    public static long getSnowflake(){
        return SNOWFLAKE.nextId();
    }
}
