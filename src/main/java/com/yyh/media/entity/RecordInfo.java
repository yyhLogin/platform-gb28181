package com.yyh.media.entity;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.List;

/**
 * @author: yyh
 * @date: 2022-02-16 11:08
 * @description: RecordInfo
 **/
@Data
public class RecordInfo {
    private String deviceId;

    private String channelId;

    private String sn;

    private String name;

    private int sumNum;

    private List<RecordItem> recordList;
}
