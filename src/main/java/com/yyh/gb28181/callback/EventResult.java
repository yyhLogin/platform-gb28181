package com.yyh.gb28181.callback;

import javax.sip.*;
import javax.sip.header.CallIdHeader;
import javax.sip.message.Response;

/**
 * @author: yyh
 * @date: 2021-12-08 16:28
 * @description: EventResult
 **/
public class EventResult<EventObject> {
    public int statusCode;
    public String type;
    public String msg;
    public String callId;
    public Dialog dialog;
    public EventObject event;

    public EventResult() {
    }

    public EventResult(EventObject event) {
        this.event = event;
        if (event instanceof ResponseEvent) {
            ResponseEvent responseEvent = (ResponseEvent)event;
            Response response = responseEvent.getResponse();
            this.dialog = responseEvent.getDialog();
            this.type = "response";
            if (response != null) {
                this.msg = response.getReasonPhrase();
                this.statusCode = response.getStatusCode();
            }
            assert response != null;
            this.callId = ((CallIdHeader)response.getHeader(CallIdHeader.NAME)).getCallId();

        }else if (event instanceof TimeoutEvent) {
            TimeoutEvent timeoutEvent = (TimeoutEvent)event;
            this.type = "timeout";
            this.msg = "消息超时未回复";
            this.statusCode = -1024;
            this.callId = timeoutEvent.getClientTransaction().getDialog().getCallId().getCallId();
            this.dialog = timeoutEvent.getClientTransaction().getDialog();
        }else if (event instanceof TransactionTerminatedEvent) {
            TransactionTerminatedEvent transactionTerminatedEvent = (TransactionTerminatedEvent)event;
            this.type = "transactionTerminated";
            this.msg = "事务已结束";
            this.statusCode = -1024;
            this.callId = transactionTerminatedEvent.getClientTransaction().getDialog().getCallId().getCallId();
            this.dialog = transactionTerminatedEvent.getClientTransaction().getDialog();
        }else if (event instanceof DialogTerminatedEvent) {
            DialogTerminatedEvent dialogTerminatedEvent = (DialogTerminatedEvent)event;
            this.type = "dialogTerminated";
            this.msg = "会话已结束";
            this.statusCode = -1024;
            this.callId = dialogTerminatedEvent.getDialog().getCallId().getCallId();
            this.dialog = dialogTerminatedEvent.getDialog();
        }
    }
}
