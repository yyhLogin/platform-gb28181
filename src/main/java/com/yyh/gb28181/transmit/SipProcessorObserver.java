package com.yyh.gb28181.transmit;

import com.yyh.gb28181.callback.EventResult;
import com.yyh.gb28181.callback.SipCallback;
import com.yyh.gb28181.callback.SipSubscribe;
import com.yyh.gb28181.dispatcher.SipRequestDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sip.*;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.message.Response;

/**
 * @author: yyh
 * @date: 2021-11-24 09:28
 * @description: SipProcessorObserver
 **/
@Component
@RequiredArgsConstructor
public class SipProcessorObserver implements ISipProcessorObserver{

    private final static Logger logger = LoggerFactory.getLogger(SipProcessorObserver.class);

    private final SipRequestDispatcher sipRequestDispatcher;
    private final SipSubscribe sipSubscribe;

//    public SipProcessorObserver(SipRequestDispatcher sipRequestDispatcher) {
//        this.sipRequestDispatcher = sipRequestDispatcher;
//    }

    @Override
    @SneakyThrows
    public void processRequest(RequestEvent requestEvent) {
        logger.info("接收到sip请求:{}",requestEvent.getRequest().getMethod());
        ///logger.info("接收到sip请求:\r\n{}",requestEvent.getRequest());
        sipRequestDispatcher.doDispatcher(requestEvent);
    }

    @Override
    public void processResponse(ResponseEvent responseEvent) {
        logger.info("接收到sip响应:{}",responseEvent.getResponse());
        Response response = responseEvent.getResponse();
        logger.debug(responseEvent.getResponse().toString());
        int status = response.getStatusCode();
        // Success!
        if (((status >= 200) && (status < 300)) || status == 401) {
///            ISIPResponseProcessor processor = processorFactory.createResponseProcessor(evt);
            CSeqHeader cseqHeader = (CSeqHeader) responseEvent.getResponse().getHeader(CSeqHeader.NAME);
            String method = cseqHeader.getMethod();
//            ISIPResponseProcessor sipRequestProcessor = responseProcessorMap.get(method);
//            if (sipRequestProcessor != null) {
//                sipRequestProcessor.process(responseEvent);
//            }
            if (responseEvent.getResponse() != null && sipSubscribe.getOkSubscribesSize() > 0 ) {
                CallIdHeader callIdHeader = (CallIdHeader)responseEvent.getResponse().getHeader(CallIdHeader.NAME);
                if (callIdHeader != null) {
                    SipCallback subscribe = sipSubscribe.getOkSubscribe(callIdHeader.getCallId());
                    if (subscribe != null) {
                        EventResult<ResponseEvent> eventResult = new EventResult<>(responseEvent);
                        //subscribe.response(eventResult);
                    }
                }
            }
        } else if ((status >= 100) && (status < 200)) {
            // 增加其它无需回复的响应，如101、180等
        } else {
            logger.warn("接收到失败的response响应！status：" + status + ",message:" + response.getReasonPhrase()/* .getContent().toString()*/);
            if (responseEvent.getResponse() != null && sipSubscribe.getErrorSubscribesSize() > 0 ) {
                CallIdHeader callIdHeader = (CallIdHeader)responseEvent.getResponse().getHeader(CallIdHeader.NAME);
                if (callIdHeader != null) {
                    SipCallback subscribe = sipSubscribe.getErrorSubscribe(callIdHeader.getCallId());
                    if (subscribe != null) {
                        EventResult<ResponseEvent> eventResult = new EventResult<>(responseEvent);
                        //subscribe.response(eventResult);
                    }
                }
            }
            if (responseEvent.getDialog() != null) {
                responseEvent.getDialog().delete();
            }
        }
    }
    /**
     * Processes a retransmit or expiration Timeout of an underlying
     * {@link Transaction}handled by this SipListener. This Event notifies the
     * application that a retransmission or transaction Timer expired in the
     * SipProvider's transaction state machine. The TimeoutEvent encapsulates
     * the specific timeout type and the transaction identifier either client or
     * server upon which the timeout occured. The type of Timeout can by
     * determined by:
     * <code>timeoutType = timeoutEvent.getTimeout().getValue();</code>
     *
     * @param timeoutEvent -
     *                     the timeoutEvent received indicating either the message
     *                     retransmit or transaction timed out.
     */
    @Override
    public void processTimeout(TimeoutEvent timeoutEvent) {

    }
    /**
     * Process an asynchronously reported IO Exception. Asynchronous IO
     * Exceptions may occur as a result of errors during retransmission of
     * requests. The transaction state machine requires to report IO Exceptions
     * to the application immediately (according to RFC 3261). This method
     * enables an implementation to propagate the asynchronous handling of IO
     * Exceptions to the application.
     *
     * @param exceptionEvent --
     * @since v1.2
     */
    @Override
    public void processIOException(IOExceptionEvent exceptionEvent) {

    }

    /**
     * Process an asynchronously reported TransactionTerminatedEvent.
     * When a transaction transitions to the Terminated state, the stack
     * keeps no further records of the transaction. This notification can be used by
     * applications to clean up any auxiliary data that is being maintained
     * for the given transaction.
     *
     * @param transactionTerminatedEvent -- an event that indicates that the
     *                                   transaction has transitioned into the terminated state.
     * @since v1.2
     */
    @Override
    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {

    }

    /**
     * Process an asynchronously reported DialogTerminatedEvent.
     * When a dialog transitions to the Terminated state, the stack
     * keeps no further records of the dialog. This notification can be used by
     * applications to clean up any auxiliary data that is being maintained
     * for the given dialog.
     *
     * @param dialogTerminatedEvent -- an event that indicates that the
     *                              dialog has transitioned into the terminated state.
     * @since v1.2
     */
    @Override
    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {

    }
}
