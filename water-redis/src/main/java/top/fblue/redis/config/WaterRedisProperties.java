package top.fblue.redis.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Water Redis 客户端配置。
 */
@ConfigurationProperties(prefix = "water.redis")
public class WaterRedisProperties {

    /**
     * 是否让 Lettuce 使用 JVM 系统 DNS 解析器。
     */
    private boolean useSystemDns;

    public boolean isUseSystemDns() {
        return useSystemDns;
    }

    public void setUseSystemDns(boolean useSystemDns) {
        this.useSystemDns = useSystemDns;
    }
}
