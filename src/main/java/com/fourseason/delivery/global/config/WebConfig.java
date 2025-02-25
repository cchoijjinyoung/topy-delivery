package com.fourseason.delivery.global.config;

import com.fourseason.delivery.global.resolver.PageSizeArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final PageSizeArgumentResolver pageSizeArgumentResolver;

    public WebConfig(PageSizeArgumentResolver pageSizeArgumentResolver) {
        this.pageSizeArgumentResolver = pageSizeArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(pageSizeArgumentResolver);
    }
}
