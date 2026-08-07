package top.fblue.framework.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class WaterClockAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(WaterClockAutoConfiguration.class));

    @Test
    void shouldProvideServerSystemClockByDefault() {
        contextRunner.run(context -> {
            Clock clock = context.getBean(Clock.class);

            assertThat(clock.getZone()).isEqualTo(ZoneId.systemDefault());
        });
    }

    @Test
    void shouldKeepApplicationProvidedClock() {
        contextRunner.withUserConfiguration(FixedClockConfiguration.class)
                .run(context -> {
                    Clock clock = context.getBean(Clock.class);

                    assertThat(clock).isSameAs(FixedClockConfiguration.FIXED_CLOCK);
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class FixedClockConfiguration {

        private static final Clock FIXED_CLOCK = Clock.fixed(
                Instant.parse("2026-08-07T08:00:00Z"), ZoneId.of("Asia/Shanghai"));

        @Bean
        Clock fixedClock() {
            return FIXED_CLOCK;
        }
    }
}
