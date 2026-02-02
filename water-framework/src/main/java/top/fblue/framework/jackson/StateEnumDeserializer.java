package top.fblue.framework.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import top.fblue.framework.common.enums.StateEnum;

import java.io.IOException;

/**
 * 将请求体中的 state 数字（1/2）反序列化为 StateEnum。
 * 接口入参传 "state": 1 或 "state": 2，Jackson 会调用本类完成转换。
 *
 * @author banana
 */
public class StateEnumDeserializer extends JsonDeserializer<StateEnum> {

    @Override
    public StateEnum deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.getCurrentToken() == JsonToken.VALUE_NULL) {
            return null; // 由 @NotNull 校验
        }
        int code = p.getIntValue();
        return StateEnum.fromCode(code);
    }
}
