package top.fblue.framework.config;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.fblue.framework.interceptor.TraceInterceptor;

/**
 * Web MVC配置类
 * 配置拦截器链：Token认证 → 权限验证
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {


    @Resource
    private TraceInterceptor traceInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 设置 TraceContext
        registry.addInterceptor(traceInterceptor)
                .addPathPatterns("/api/**")
                .order(1);
    }
}
