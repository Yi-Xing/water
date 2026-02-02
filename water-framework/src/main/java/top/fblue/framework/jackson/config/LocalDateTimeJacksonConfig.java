package top.fblue.framework.jackson.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.fblue.framework.jackson.LocalDateTimeDeserializer;
import top.fblue.framework.jackson.LocalDateTimeSerializer;

import java.time.LocalDateTime;

/**
 * LocalDateTime 序列化/反序列化：格式为 yyyy-MM-dd HH:mm:ss
 *
 * @author banana
 */
@Configuration
public class LocalDateTimeJacksonConfig {

    @Bean
    public SimpleModule localDateTimeJacksonModule() {
        SimpleModule module = new SimpleModule("LocalDateTimeModule");
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
        return module;
    }
}
