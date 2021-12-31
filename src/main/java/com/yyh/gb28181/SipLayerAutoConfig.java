package com.yyh.gb28181;

import com.yyh.gb28181.config.SipServerProperties;
import com.yyh.gb28181.transmit.ISipProcessorObserver;
import gov.nist.javax.sip.SipProviderImpl;
import gov.nist.javax.sip.SipStackImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.sip.*;
import java.util.Properties;
import java.util.TooManyListenersException;

/**
 * @author: yyh
 * @date: 2021-11-23 14:19
 * @description: SipLayerAutoConfig
 **/
@Component
@ConditionalOnProperty(name = "sip.enabled", matchIfMissing = true)
public class SipLayerAutoConfig {

    private final static Logger logger = LoggerFactory.getLogger(SipLayerAutoConfig.class);

    private final SipServerProperties sipConfig;

    private final ISipProcessorObserver sipProcessorObserver;

    private SipStackImpl sipStack;

    private SipFactory sipFactory;

    public SipLayerAutoConfig(SipServerProperties sipConfig, ISipProcessorObserver sipProcessorObserver) {
        this.sipConfig = sipConfig;
        this.sipProcessorObserver = sipProcessorObserver;
    }

    @Bean("sipFactory")
    private SipFactory createSipFactory() {
        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        return sipFactory;
    }

    @Bean("sipStack")
    @DependsOn({"sipFactory"})
    private SipStack createSipStack() throws PeerUnavailableException {
        Properties properties = new Properties();
        properties.setProperty("javax.sip.STACK_NAME", "GB28181_SIP");
        properties.setProperty("javax.sip.IP_ADDRESS", sipConfig.getMonitorIp());
        properties.setProperty("gov.nist.javax.sip.LOG_MESSAGE_CONTENT", "false");
        /**
         * sip_server_log.log 和 sip_debug_log.log public static final int TRACE_NONE =
         * 0; public static final int TRACE_MESSAGES = 16; public static final int
         * TRACE_EXCEPTION = 17; public static final int TRACE_DEBUG = 32;
         */
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "0");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG", "sip_server_log");
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG", "sip_debug_log");
        sipStack = (SipStackImpl) sipFactory.createSipStack(properties);
        return sipStack;
    }


    @Bean(name = "tcpSipProvider")
    @DependsOn("sipStack")
    private SipProviderImpl startTcpListener() {
        ListeningPoint tcpListeningPoint = null;
        SipProviderImpl tcpSipProvider  = null;
        try {
            tcpListeningPoint = sipStack.createListeningPoint(sipConfig.getMonitorIp(), sipConfig.getPort(), "TCP");
            tcpSipProvider = (SipProviderImpl)sipStack.createSipProvider(tcpListeningPoint);
            tcpSipProvider.addSipListener(sipProcessorObserver);
            logger.info("Sip Server TCP Start port {" + sipConfig.getMonitorIp() + ":" + sipConfig.getPort() + "}");
        } catch (TransportNotSupportedException e) {
            logger.error("Sip Server Start For TCP Has Error TransportNotSupportedException -> {}",e.getMessage());
        } catch (InvalidArgumentException e) {
            logger.error("无法使用 [ {}:{} ]作为SIP[ TCP ]服务，可排查: 1. sip.monitor-ip 是否为本机网卡IP; 2. sip.port 是否已被占用"
                    , sipConfig.getMonitorIp(), sipConfig.getPort());
        } catch (TooManyListenersException e) {
            logger.error("Sip Server Start For TCP Has Error TooManyListenersException -> {}",e.getMessage());
        } catch (ObjectInUseException e) {
            logger.error("Sip Server Start For TCP Has Error ObjectInUseException -> {}",e.getMessage());
        }
        return tcpSipProvider;
    }


    @Bean(name = "udpSipProvider")
    @DependsOn("sipStack")
    private SipProviderImpl startUdpListener() {
        ListeningPoint udpListeningPoint = null;
        SipProviderImpl udpSipProvider = null;
        try {
            udpListeningPoint = sipStack.createListeningPoint(sipConfig.getMonitorIp(), sipConfig.getPort(), "UDP");
            udpSipProvider = (SipProviderImpl)sipStack.createSipProvider(udpListeningPoint);
            udpSipProvider.addSipListener(sipProcessorObserver);
            logger.info("Sip Server UDP Start port [" + sipConfig.getMonitorIp() + ":" + sipConfig.getPort() + "]");
        } catch (TransportNotSupportedException e) {
            logger.error("Sip Server Start For UDP Has Error TransportNotSupportedException -> {}",e.getMessage());
        } catch (InvalidArgumentException e) {
            logger.error("无法使用 [ {}:{} ]作为SIP[ UDP ]服务，可排查: 1. sip.monitor-ip 是否为本机网卡IP; 2. sip.port 是否已被占用"
                    , sipConfig.getMonitorIp(), sipConfig.getPort());
        } catch (TooManyListenersException e) {
            logger.error("Sip Server Start For UDP Has Error TooManyListenersException -> {}",e.getMessage());
        } catch (ObjectInUseException e) {
            logger.error("Sip Server Start For UDP Has Error ObjectInUseException -> {}",e.getMessage());
        }
        return udpSipProvider;
    }
}
