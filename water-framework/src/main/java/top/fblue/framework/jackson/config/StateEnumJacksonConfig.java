package top.fblue.framework.jackson.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.fblue.framework.common.enums.StateEnum;
import top.fblue.framework.jackson.StateEnumDeserializer;
import top.fblue.framework.jackson.StateEnumSerializer;

/**
 * StateEnum 序列化/反序列化：与 Jackson 绑定，请求/响应中均按数字 code 处理。
 *
 * @author banana
 */
@Configuration
public class StateEnumJacksonConfig {

    @Bean
    public SimpleModule stateEnumJacksonModule() {
        SimpleModule module = new SimpleModule("StateEnumModule");
        module.addSerializer(StateEnum.class, new StateEnumSerializer());
        module.addDeserializer(StateEnum.class, new StateEnumDeserializer());
        return module;
    }
}
