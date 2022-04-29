package com.yyh.gb28181.queue.listener;

import com.google.common.eventbus.Subscribe;
import com.yyh.gb28181.callback.EventResult;
import com.yyh.gb28181.callback.SipCallback;
import com.yyh.gb28181.callback.SipSubscribe;
import com.yyh.gb28181.component.SipRequestProcessorMapping;
import com.yyh.gb28181.constant.SipRequestConstant;
import com.yyh.gb28181.queue.DefaultSipEventBus;
import com.yyh.gb28181.utils.XmlUtil;
import gov.nist.javax.sip.RequestEventExt;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.springframework.web.method.HandlerMethod;

import javax.annotation.PostConstruct;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.EventObject;
import java.util.List;

import static com.yyh.gb28181.utils.XmlUtil.getRootElement;

/**
 * @author: yyh
 * @date: 2022-01-04 14:57
 * @description: DefaultSipMessageQueueListener
 **/
@Slf4j
public class DefaultSipMessageQueueListener extends AbstractSipMessageQueueListener<DefaultSipEventBus>{

    private final SipSubscribe sipSubscribe;

    public DefaultSipMessageQueueListener(DefaultSipEventBus queue,
                                          SipRequestProcessorMapping mapping,
                                          SipSubscribe sipSubscribe) {
        super(queue, mapping);
        this.sipSubscribe = sipSubscribe;
    }

    @PostConstruct
    public void init() {
        queue.register(this);
    }

    /**
     * 监听消息
     * @param eventObject sipMessage
     * @throws IOException IOException
     * @throws InterruptedException InterruptedException
     */
    @Subscribe
    public void listen(EventObject eventObject) throws IOException, InterruptedException {
        consumeMsg(eventObject);
    }

    /**
     * 处理请求 request
     *
     * @param requestEvent requestEvent
     */
    @Override
    protected void handleRequest(RequestEvent requestEvent) {
        String method =  requestEvent.getRequest().getMethod();
        try {
            if (SipRequestConstant.MESSAGE.equals(method)||SipRequestConstant.NOTIFY.equals(method)){
                RequestEventExt evt = (RequestEventExt)requestEvent;
                Element rootElement = getRootElement(evt);
                /// 根节点
                method += "/" +rootElement.getName();
                /// CmdType
                method += "/"+ XmlUtil.getText(rootElement, "CmdType");
            }
//            if (SipRequestConstant.NOTIFY.equals(method)){
//                log.info("method -> {}",requestEvent.getRequest());
//            }
        }catch (DocumentException e){
            log.error("consumeMsg error -> {}",e.getMessage(),e);
        }
        method = SipRequestConstant.REQUEST+"/"+method;
        log.info("consumer request msg -> {}",method);
        process(method,requestEvent);
    }

    /**
     * 处理response
     *
     * @param responseEvent responseEvent
     */
    @Override
    protected void handleResponse(ResponseEvent responseEvent){
        Response response = responseEvent.getResponse();
        //log.info("response:{}",response);
        int statusCode = response.getStatusCode();
        CSeqHeader cseqHeader = (CSeqHeader) responseEvent.getResponse().getHeader(CSeqHeader.NAME);
        String method = cseqHeader.getMethod();
        log.info("response method->{}",method);
        boolean b = (statusCode >= Response.OK) && (statusCode < Response.MULTIPLE_CHOICES) || statusCode == Response.UNAUTHORIZED;
        if (b) {
            /// INVITE服务需要响应
            if (Request.INVITE.equalsIgnoreCase(method)||Request.REGISTER.equalsIgnoreCase(method)){
                method = SipRequestConstant.RESPONSE+"/"+method;
                log.info("consumer response msg -> {}",method);
                process(method,responseEvent);
            }
            if (responseEvent.getResponse() != null && sipSubscribe.getOkSubscribesSize() > 0 ) {
                CallIdHeader callIdHeader = (CallIdHeader)responseEvent.getResponse().getHeader(CallIdHeader.NAME);
                if (callIdHeader != null) {
                    SipCallback okSubscribe = sipSubscribe.getOkSubscribe(callIdHeader.getCallId());
                    if (okSubscribe != null) {
                        EventResult<EventObject> eventResult = new EventResult<>(responseEvent);
                        okSubscribe.response(eventResult);
                        sipSubscribe.removeOkSubscribe(callIdHeader.getCallId());
                    }
                }
            }
        }else if ((statusCode >= Response.TRYING) && (statusCode < Response.OK)){
            // TODO 增加其它无需回复的响应，如101、180等
            log.info("");
        }else {
            //错误响应
            log.warn("接收到失败的response响应！status：{},message: {}" ,response.getStatusCode(),response.getReasonPhrase());
            if (responseEvent.getResponse() != null && sipSubscribe.getErrorSubscribesSize() > 0 ) {
                CallIdHeader callIdHeader = (CallIdHeader)responseEvent.getResponse().getHeader(CallIdHeader.NAME);
                if (callIdHeader != null) {
                    SipCallback errorSubscribe = sipSubscribe.getErrorSubscribe(callIdHeader.getCallId());
                    if (errorSubscribe != null) {
                        EventResult<EventObject> eventResult = new EventResult<>(responseEvent);
                        errorSubscribe.response(eventResult);
                        sipSubscribe.removeErrorSubscribe(callIdHeader.getCallId());
                    }
                }
            }
            if (responseEvent.getDialog() != null) {
                responseEvent.getDialog().delete();
            }
        }
    }

    /**
     * 处理请求
     * @param method method
     * @param object object
     */
    private void process(String method,Object object){
        List<HandlerMethod> handlerMethods = mapping.getHandlerMethodsForMappingName(method.toUpperCase());
        if (handlerMethods==null){
            log.info("consume msg -> {} no handle",method);
            return;
        }
        executor.execute(()->{
            try {
                HandlerMethod handlerMethod = handlerMethods.get(0);
                handlerMethod.getMethod().invoke(handlerMethod.getBean(),object);
            } catch (IllegalAccessException | InvocationTargetException e) {
                log.error("consume msg error -> {}",e.getMessage(),e);
            }
        });
    }
}
