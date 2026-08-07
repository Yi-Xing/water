package top.fblue.auth.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.fblue.auth.jwt.JwtTokenService;
import top.fblue.auth.repository.impl.RedisTokenRevocationRepositoryImpl;
import top.fblue.auth.repository.TokenRevocationRepository;
import top.fblue.auth.web.BearerTokenResolver;
import top.fblue.auth.web.SsoAuthenticationInterceptor;
import top.fblue.auth.web.SsoExceptionHandler;
import top.fblue.auth.web.SsoStateCookieService;

import java.time.Clock;

/**
 * 在启用 SSO 时注册 JWT、令牌撤销、异常响应和 Web MVC 鉴权拦截器等基础组件。
 */
@AutoConfiguration
@EnableConfigurationProperties(SsoAuthProperties.class)
@ConditionalOnProperty(prefix = "sso", name = "enabled", havingValue = "true")
public class SsoWebMvcAutoConfiguration {

    /**
     * @param properties SSO 鉴权配置
     * @param clock      Water 提供的应用统一时钟
     * @return JWT 签发与验证服务
     */
    @Bean
    @ConditionalOnMissingBean
    JwtTokenService jwtTokenService(SsoAuthProperties properties, Clock clock) {
        return new JwtTokenService(properties, clock);
    }

    /**
     * @param redisTemplate Redis 字符串操作模板
     * @param properties    SSO 鉴权配置
     * @return 基于 Redis 的会话和 Token 撤销存储
     */
    @Bean
    @ConditionalOnMissingBean
    TokenRevocationRepository tokenRevocationRepository(StringRedisTemplate redisTemplate,
                                                         SsoAuthProperties properties) {
        return new RedisTokenRevocationRepositoryImpl(redisTemplate, properties);
    }

    /**
     * @return HTTP Bearer Token 解析器
     */
    @Bean
    @ConditionalOnMissingBean
    BearerTokenResolver bearerTokenResolver() {
        return new BearerTokenResolver();
    }

    /**
     * @param properties SSO 鉴权配置
     * @return SSO 登录 state Cookie 服务
     */
    @Bean
    @ConditionalOnMissingBean
    SsoStateCookieService ssoStateCookieService(SsoAuthProperties properties) {
        return new SsoStateCookieService(properties);
    }

    /**
     * @param properties SSO 鉴权配置
     * @return SSO 鉴权异常处理器
     */
    @Bean
    @ConditionalOnMissingBean
    SsoExceptionHandler ssoExceptionHandler(SsoAuthProperties properties) {
        return new SsoExceptionHandler(properties);
    }

    /**
     * @param tokenResolver   Bearer Token 解析器
     * @param jwtTokenService JWT 验证服务
     * @param repository      会话和 Token 撤销存储
     * @return SSO HTTP 鉴权拦截器
     */
    @Bean
    @ConditionalOnMissingBean
    SsoAuthenticationInterceptor ssoAuthenticationInterceptor(BearerTokenResolver tokenResolver,
                                                               JwtTokenService jwtTokenService,
                                                               TokenRevocationRepository repository) {
        return new SsoAuthenticationInterceptor(tokenResolver, jwtTokenService, repository);
    }

    /**
     * 将 SSO 鉴权拦截器注册到配置的 HTTP 路径，并应用排除路径。
     *
     * @param interceptor SSO HTTP 鉴权拦截器
     * @param properties  SSO 路径配置
     * @return Web MVC 拦截器配置器
     */
    @Bean
    WebMvcConfigurer ssoWebMvcConfigurer(SsoAuthenticationInterceptor interceptor,
                                         SsoAuthProperties properties) {
        return new WebMvcConfigurer() {
            /** {@inheritDoc} */
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                var registration = registry.addInterceptor(interceptor)
                        .addPathPatterns(properties.getPathPatterns())
                        .order(10);
                if (!properties.getExcludePaths().isEmpty()) {
                    registration.excludePathPatterns(properties.getExcludePaths());
                }
            }
        };
    }
}
