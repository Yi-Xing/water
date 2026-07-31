package top.fblue.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import top.fblue.common.exception.BusinessException;

/**
 * 定义 HTTP、业务异常和统一 API 响应共同使用的状态码。
 */
@Getter
public enum ApiCodeEnum {
    /** 请求处理成功。 */
    SUCCESS(200, "操作成功"),

    /** 请求参数或业务前置条件不合法。 */
    BAD_REQUEST(400, "请求参数错误"),

    /** 未认证或认证信息已失效。 */
    UNAUTHORIZED(401, "未认证"),

    /** 当前身份无权访问资源。 */
    FORBIDDEN(403, "无权访问"),

    /** 服务内部处理异常。 */
    INTERNAL_ERROR(500, "系统异常"),

    /** 鉴权等依赖服务暂时不可用。 */
    SERVICE_UNAVAILABLE(503, "服务暂不可用");

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
