package com.yyh.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.yyh.media.config.ZlmServerConfig;
import com.yyh.media.entity.HookResult;
import com.yyh.media.entity.RecordItem;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author: yyh
 * @date: 2021-12-21 16:19
 * @description: ConverterType
 **/
public class ConverterType {

    public static final TypeReference<Map<String,Object>> MAP_TYPE = new TypeReference<>() {};
    public static final TypeReference<ArrayList<ZlmServerConfig>> ARRAY_LIST_TYPE = new TypeReference<>() {};
    public static final TypeReference<ArrayList<String>> ARRAY_LIST_STRING_TYPE = new TypeReference<>() {};
    public static final TypeReference<ZlmServerConfig> ZLM_SERVER_CONFIG_TYPE = new TypeReference<>() {};
    public static final TypeReference<ArrayList<Map<String,Object>>> ARRAY_LIST_MAP_TYPE = new TypeReference<>() {};
    public static final TypeReference<HookResult<ArrayList<ZlmServerConfig>>> ARRAYLIST_ZLM_SERVER_CONFIG_TYPE = new TypeReference<>() {};
    public static final TypeReference<ArrayList<RecordItem>> ARRAYLIST_RECORD_ITEM_TYPE = new TypeReference<>() {};
}
