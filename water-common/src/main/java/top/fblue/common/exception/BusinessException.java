package top.fblue.common.exception;

import top.fblue.common.enums.ApiCodeEnum;

import java.util.Objects;

/**
 * 携带统一 API 响应码的业务异常基类，供各业务异常继承。
 */
public class BusinessException extends RuntimeException {

    private final ApiCodeEnum code;

    public BusinessException() {
        this.code = ApiCodeEnum.INTERNAL_ERROR;
    }

    public BusinessException(String message) {
        super(message);
        this.code = ApiCodeEnum.INTERNAL_ERROR;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = ApiCodeEnum.INTERNAL_ERROR;
    }

    public BusinessException(ApiCodeEnum code, String message) {
        super(message);
        this.code = Objects.requireNonNull(code, "code 不能为空");
    }

    public BusinessException(ApiCodeEnum code, String message, Throwable cause) {
        super(message, cause);
        this.code = Objects.requireNonNull(code, "code 不能为空");
    }

    public ApiCodeEnum getCode() {
        return code;
    }
}
