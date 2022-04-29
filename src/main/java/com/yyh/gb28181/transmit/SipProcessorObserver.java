package com.yyh.gb28181.transmit;

import com.yyh.gb28181.dispatcher.SipDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.sip.*;
import javax.sip.message.Request;

/**
 * @author: yyh
 * @date: 2021-11-24 09:28
 * @description: SipProcessorObserver
 **/
@Component
@RequiredArgsConstructor
public class SipProcessorObserver implements ISipProcessorObserver{

    private final static Logger logger = LoggerFactory.getLogger(SipProcessorObserver.class);

    private final SipDispatcher sipDispatcher;

    @Override
    @SneakyThrows
    public void processRequest(RequestEvent requestEvent) {
        ///logger.info("接收到sip请求:\r\n{}",requestEvent.getRequest());
        sipDispatcher.doDispatcher(requestEvent);
    }

    @Override
    @SneakyThrows
    public void processResponse(ResponseEvent responseEvent) {
        ///logger.info("接收到sip响应:{}",responseEvent.getResponse());
        sipDispatcher.doDispatcher(responseEvent);
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
    @SneakyThrows
    public void processTimeout(TimeoutEvent timeoutEvent) {
        sipDispatcher.doDispatcher(timeoutEvent);
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
    @SneakyThrows
    public void processIOException(IOExceptionEvent exceptionEvent) {
        logger.error("IOExceptionEvent:{}:{}",exceptionEvent.getHost(),exceptionEvent.getPort());
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
    @SneakyThrows
    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
        ///sipDispatcher.doDispatcher(transactionTerminatedEvent);
        if (transactionTerminatedEvent.isServerTransaction()){
            ServerTransaction serverTransaction = transactionTerminatedEvent.getServerTransaction();
            logger.debug("TransactionTerminatedEvent:{}",transactionTerminatedEvent.getServerTransaction());
            Request request = serverTransaction.getRequest();
            logger.debug("request:{}",request);
        }else {
            logger.debug("TransactionTerminatedEvent:{}",transactionTerminatedEvent.getClientTransaction());
        }
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
    @SneakyThrows
    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
        ///sipDispatcher.doDispatcher(dialogTerminatedEvent);
        logger.info("DialogTerminatedEvent:{}",dialogTerminatedEvent.getDialog());
    }
}
