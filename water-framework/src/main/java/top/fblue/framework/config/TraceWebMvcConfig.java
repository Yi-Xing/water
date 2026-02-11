package top.fblue.framework.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置类
 * 配置 Trace 拦截器链
 */
@Configuration
public class TraceWebMvcConfig implements WebMvcConfigurer {

    private final HandlerInterceptor traceInterceptor;

    public TraceWebMvcConfig(HandlerInterceptor traceInterceptor) {
        this.traceInterceptor = traceInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(traceInterceptor)
                .addPathPatterns("/api/**")
                .order(0);
    }
}
