package com.yyh.gb28181.component;


import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerMethodMappingNamingStrategy;

/**
 * @author: yyh
 * @date: 2021-11-25 19:03
 * @description: AbstractSipHandlerMethodInfoMapping
 **/
public abstract class AbstractSipRequestHandlerMethodInfoMapping extends AbstractSipRequestHandlerMethodMapping<SipRequestMappingInfo> {

    public AbstractSipRequestHandlerMethodInfoMapping(){
        setHandlerMethodMappingNamingStrategy(new SipRequestMappingInfoHandlerMethodMappingNamingStrategy());
    }
    static class SipRequestMappingInfoHandlerMethodMappingNamingStrategy implements HandlerMethodMappingNamingStrategy<SipRequestMappingInfo> {

        SipRequestMappingInfoHandlerMethodMappingNamingStrategy(){

        }

        /**
         * Determine the name for the given HandlerMethod and mapping.
         *
         * @param handlerMethod the handler method
         * @param mapping       the mapping
         * @return the name
         */
        @Override
        public String getName( HandlerMethod handlerMethod, SipRequestMappingInfo mapping) {
            return mapping.getType();
        }
    }
}


