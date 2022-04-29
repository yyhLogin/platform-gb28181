package com.yyh.media.entity;

import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author: yyh
 * @date: 2022-02-16 11:09
 * @description: RecordItem
 **/
@Data
public class RecordItem  implements Comparable<RecordItem>{

    private String deviceId;

    private String name;

    private String filePath;

    private String address;

    private String startTime;

    private String endTime;

    private int secrecy;

    private String type;

    private String recorderId;

    @Override
    public int compareTo(@NotNull RecordItem recordItem) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date startTimeNow = sdf.parse(startTime);
            Date startTimeParam = sdf.parse(recordItem.getStartTime());
            if (startTimeParam.compareTo(startTimeNow) > 0) {
                return -1;
            }else {
                return 1;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
