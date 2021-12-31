package com.yyh.gb28181.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * @author: yyh
 * @date: 2021-11-25 18:08
 * @description: MsgProcessorThreadPoolProps
 **/
@Getter
@Setter
@ToString
@Slf4j
@Component
@ConfigurationProperties(prefix = "sip.thread", ignoreInvalidFields = true)
public class SipMsgProcessorThreadPoolProps {

    private int corePoolSize = Runtime.getRuntime().availableProcessors() + 1;

    private int maximumPoolSize = 2 * corePoolSize;

    private Duration keepAliveTime = Duration.ofSeconds(60);

    private int blockingQueueSize = 100;

    private String threadNameFormat = "sip-msg-processor-%d";

}
