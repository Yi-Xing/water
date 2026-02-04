package top.fblue.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import top.fblue.common.exception.BusinessException;

/**
 * 统一 API 响应码枚举
 */
@Getter
public enum ApiCodeEnum {
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    INTERNAL_ERROR(500, "系统异常");

    private final int code;
    private final String desc;

    ApiCodeEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * JSON 序列化输出为数字 code（而不是枚举名）
     */
    @JsonValue
    public int jsonValue() {
        return code;
    }

    @JsonCreator
    public static ApiCodeEnum fromCode(int code) {
        for (ApiCodeEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        throw new BusinessException("Unknown ApiCodeEnum code: " + code);
    }
}

