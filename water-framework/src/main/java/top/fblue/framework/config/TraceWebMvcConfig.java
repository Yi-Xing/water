package top.fblue.framework.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.fblue.framework.interceptor.TraceInterceptor;

/**
 * Web MVC配置类
 * 配置 Trace 拦截器链
 *
 * <p>显式导入 {@link TraceInterceptor}，避免依赖业务应用的组件扫描范围。</p>
 */
@Configuration
@Import(TraceInterceptor.class)
public class TraceWebMvcConfig implements WebMvcConfigurer {

    private final TraceInterceptor traceInterceptor;

    public TraceWebMvcConfig(TraceInterceptor traceInterceptor) {
        this.traceInterceptor = traceInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(traceInterceptor)
                .addPathPatterns("/api/**")
                .order(0);
    }
}
