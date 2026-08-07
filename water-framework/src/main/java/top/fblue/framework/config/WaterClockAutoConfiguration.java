package top.fblue.framework.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

/**
 * Water 统一时钟自动配置。
 *
 * <p>默认使用当前应用服务器的系统时间和系统时区；业务项目或测试可通过声明自己的
 * {@link Clock} Bean 覆盖默认实现。</p>
 */
@AutoConfiguration
public class WaterClockAutoConfiguration {

    /**
     * 提供应用内统一使用的服务器系统时钟。
     *
     * @return 当前服务器默认时区时钟
     */
    @Bean
    @ConditionalOnMissingBean(Clock.class)
    public Clock systemClock() {
        return Clock.systemDefaultZone();
    }
}
