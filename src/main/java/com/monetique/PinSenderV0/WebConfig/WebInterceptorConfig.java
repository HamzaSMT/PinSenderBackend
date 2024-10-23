package com.monetique.PinSenderV0.WebConfig;

import com.monetique.PinSenderV0.tracking.ApiRequestTrackingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebInterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private ApiRequestTrackingInterceptor apiRequestInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiRequestInterceptor)
                .addPathPatterns("/api/**")  // Intercept all API requests under /api/
                .excludePathPatterns("/api/monitor/**")
                .excludePathPatterns("/api/auth/**");
        // Exclude /api/monitor/** from being intercepted
    }
}