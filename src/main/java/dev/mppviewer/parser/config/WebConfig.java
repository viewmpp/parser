package dev.mppviewer.parser.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final BodySizeInterceptor bodySizeInterceptor;

    public WebConfig(BodySizeInterceptor bodySizeInterceptor) {
        this.bodySizeInterceptor = bodySizeInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(bodySizeInterceptor);
    }
}
