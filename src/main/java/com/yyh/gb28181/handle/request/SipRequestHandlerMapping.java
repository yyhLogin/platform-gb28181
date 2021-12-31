package com.yyh.gb28181.handle.request;

import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.ApplicationObjectSupport;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: yyh
 * @date: 2021-11-30 09:13
 * @description: SipRequestHandlerMapping
 **/
public class SipRequestHandlerMapping extends ApplicationObjectSupport implements InitializingBean{

    private final Map<String, SipRequestProcessor> mapping;

    public SipRequestHandlerMapping() {
        this.mapping = new ConcurrentHashMap<>();
    }


    /**
     * Invoked by the containing {@code BeanFactory} after it has set all bean properties
     * and satisfied {@link BeanFactoryAware}, {@code ApplicationContextAware} etc.
     * <p>This method allows the bean instance to perform validation of its overall
     * configuration and final initialization when all bean properties have been set.
     *
     * @throws Exception in the event of misconfiguration (such as failure to set an
     *                   essential property) or if initialization fails for any other reason
     */
    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
