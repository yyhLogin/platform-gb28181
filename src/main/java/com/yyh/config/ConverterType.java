package com.yyh.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.yyh.media.config.ZlmServerConfig;
import com.yyh.media.entity.HookResult;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author: yyh
 * @date: 2021-12-21 16:19
 * @description: ConverterType
 **/
public class ConverterType {

    public static final TypeReference<Map<String,Object>> MAP_TYPE = new TypeReference<>() {};
    public static final TypeReference<ArrayList<ZlmServerConfig>> ArrayList_TYPE = new TypeReference<>() {};
    public static final TypeReference<ArrayList<String>> ArrayList_STRING_TYPE = new TypeReference<>() {};
    public static final TypeReference<ZlmServerConfig> ZLM_SERVER_CONFIG_TYPE = new TypeReference<>() {};
    public static final TypeReference<ArrayList<Map<String,Object>>> ArrayList_MAP_TYPE = new TypeReference<>() {};
    public static final TypeReference<HookResult<ArrayList<ZlmServerConfig>>> ARRAYLIST_ZLM_SERVER_CONFIG_TYPE = new TypeReference<>() {};
}
