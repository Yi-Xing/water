package top.fblue.redis.config;

import io.netty.resolver.DefaultAddressResolverGroup;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.ClientResourcesBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Water Redis 客户端自动配置。
 */
@AutoConfiguration
@ConditionalOnClass({ClientResourcesBuilderCustomizer.class, DefaultAddressResolverGroup.class})
@EnableConfigurationProperties(WaterRedisProperties.class)
public class WaterRedisAutoConfiguration {

    static final String SYSTEM_DNS_CUSTOMIZER_BEAN_NAME = "waterRedisSystemDnsCustomizer";

    /**
     * 使用 JVM 系统 DNS 解析器，使 Lettuce 遵循系统 hosts 与分流 DNS 配置。
     *
     * @return Lettuce 客户端资源定制器
     */
    @Bean(name = SYSTEM_DNS_CUSTOMIZER_BEAN_NAME)
    @ConditionalOnMissingBean(name = SYSTEM_DNS_CUSTOMIZER_BEAN_NAME)
    @ConditionalOnProperty(prefix = "water.redis", name = "use-system-dns", havingValue = "true")
    ClientResourcesBuilderCustomizer waterRedisSystemDnsCustomizer() {
        return builder -> builder.addressResolverGroup(DefaultAddressResolverGroup.INSTANCE);
    }
}
