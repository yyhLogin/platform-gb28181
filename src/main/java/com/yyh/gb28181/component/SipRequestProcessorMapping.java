package com.yyh.gb28181.component;

import com.yyh.gb28181.annotation.SipRequestMapping;
import com.yyh.gb28181.annotation.SipRequestProcess;
import com.yyh.gb28181.component.AbstractSipRequestHandlerMethodInfoMapping;
import com.yyh.gb28181.component.SipRequestMappingInfo;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.condition.AbstractRequestCondition;
import org.springframework.web.servlet.mvc.condition.CompositeRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;


/**
 * @author: yyh
 * @date: 2021-11-24 16:14
 * @description: SipRequestProcessMapping
 **/
public class SipRequestProcessorMapping extends AbstractSipRequestHandlerMethodInfoMapping {
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
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
    }


    /**
     * Whether the given type is a handler with handler methods.
     *
     * @param beanType the type of the bean being checked
     * @return "true" if this a handler type, "false" otherwise.
     */
    @Override
    protected boolean isHandler(Class<?> beanType) {
        return AnnotatedElementUtils.hasAnnotation(beanType,SipRequestMapping.class);
    }

    /**
     * Provide the mapping for a handler method. A method for which no
     * mapping can be provided is not a handler method.
     *
     * @param method      the method to provide a mapping for
     * @param handlerType the handler type, possibly a sub-type of the method's
     *                    declaring class
     * @return the mapping, or {@code null} if the method is not mapped
     */
    @Override
    protected SipRequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
//        SipRequestMappingInfo requestMappingInfo = createRequestMappingInfo(method);
        SipRequestProcess requestMapping = AnnotatedElementUtils.findMergedAnnotation(method, SipRequestProcess.class);
        return (requestMapping != null ? new SipRequestMappingInfo(requestMapping.method()) : null);
//        SipRequestProcess annotation = method.getAnnotation(SipRequestProcess.class);
//        return new SipRequestMappingInfo(annotation.method());
    }


}
