package top.fblue.auth.exception;

import top.fblue.common.enums.ApiCodeEnum;
import top.fblue.common.exception.BusinessException;

/**
 * SSO 鉴权异常，对外响应码由统一 API 枚举定义。
 */
public class SsoAuthException extends BusinessException {

    /**
     * @param code    响应体中的统一 API 错误码
     * @param message 可对外展示的错误信息
     */
    public SsoAuthException(ApiCodeEnum code, String message) {
        super(code, message);
    }

    /**
     * @param code    响应体中的统一 API 错误码
     * @param message 可对外展示的错误信息
     * @param cause   仅用于服务端诊断的原始异常
     */
    public SsoAuthException(ApiCodeEnum code, String message, Throwable cause) {
        super(code, message, cause);
    }
}
