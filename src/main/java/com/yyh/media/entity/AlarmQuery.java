package com.yyh.media.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author: yyh
 * @date: 2022-03-08 11:24
 * @description: AlarmQuery
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@ToString
@ApiModel(value = "报警信息查询参数")
public class AlarmQuery implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "报警优先级")
    private String alarmPriority;
    @ApiModelProperty(value = "报警方式")
    private String alarmMethod;
    @ApiModelProperty(value = "报警类型")
    private String alarmType;
    @ApiModelProperty(value = "开始时间")
    private LocalDateTime startTime;
    @ApiModelProperty(value = "结束时间")
    private LocalDateTime endTime;
}
