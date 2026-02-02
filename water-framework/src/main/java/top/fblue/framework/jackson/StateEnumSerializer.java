package top.fblue.framework.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import top.fblue.framework.common.enums.StateEnum;

import java.io.IOException;

/**
 * 将 StateEnum 序列化为数字 code。
 * 接口出参中若有 StateEnum 字段，会输出为 "state": 1 或 "state": 2。
 *
 * @author banana
 */
public class StateEnumSerializer extends JsonSerializer<StateEnum> {

    @Override
    public void serialize(StateEnum value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else {
            gen.writeNumber(value.getCode());
        }
    }
}
