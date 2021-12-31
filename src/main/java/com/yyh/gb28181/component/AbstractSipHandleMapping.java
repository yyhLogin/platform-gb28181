package com.yyh.gb28181.component;

import com.yyh.gb28181.handle.SipHandleMapping;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.PathMatcher;


/**
 * @author: yyh
 * @date: 2021-11-25 18:56
 * @description: AbstractSipHandleMapping
 **/
public abstract class AbstractSipHandleMapping extends ApplicationObjectSupport implements SipHandleMapping, Ordered, BeanNameAware {



    @Nullable
    private String beanName;

    @Nullable
    private Object defaultHandler;

    private PathMatcher pathMatcher = new AntPathMatcher();

    /**
     * Set the default handler for this handler mapping.
     * This handler will be returned if no specific mapping was found.
     * <p>Default is {@code null}, indicating no default handler.
     */
    public void setDefaultHandler(@Nullable Object defaultHandler) {
        this.defaultHandler = defaultHandler;
    }

    /**
     * Return the default handler for this handler mapping,
     * or {@code null} if none.
     */
    @Nullable
    public Object getDefaultHandler() {
        return this.defaultHandler;
    }

    @Override
    public void setBeanName(@Nullable String name) {
        this.beanName = name;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    protected String formatMappingName() {
        return this.beanName != null ? "'" + this.beanName + "'" : getClass().getName();
    }

    /**
     * Configure the PathMatcher to use.
     * <p><strong>Note:</strong> This property is mutually exclusive with and
     * <p>By default this is {@link AntPathMatcher}.
     * @see org.springframework.util.AntPathMatcher
     */
    public void setPathMatcher(PathMatcher pathMatcher) {
        Assert.notNull(pathMatcher, "PathMatcher must not be null");
        this.pathMatcher = pathMatcher;
//        if (this.corsConfigurationSource instanceof UrlBasedCorsConfigurationSource) {
//            ((UrlBasedCorsConfigurationSource) this.corsConfigurationSource).setPathMatcher(pathMatcher);
//        }
    }

    /**
     * Return the {@link #setPathMatcher configured} {@code PathMatcher}.
     */
    public PathMatcher getPathMatcher() {
        return this.pathMatcher;
    }
}
