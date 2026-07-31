package top.fblue.auth.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.fblue.auth.common.SsoConstants;
import top.fblue.auth.config.SsoAuthProperties;
import top.fblue.auth.exception.SsoAuthException;
import top.fblue.common.enums.ApiCodeEnum;
import top.fblue.common.response.ApiResponse;

import java.util.Map;

/**
 * 将 Controller 和 SSO 应用服务抛出的鉴权异常转为统一 API 响应。
 */
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class SsoExceptionHandler {

    /** 提供未登录响应中可选的 SSO 登录入口地址。 */
    private final SsoAuthProperties properties;

    /**
     * @param properties SSO 鉴权配置
     */
    public SsoExceptionHandler(SsoAuthProperties properties) {
        this.properties = properties;
    }

    /**
     * 将 SSO 鉴权异常转换为与 API 响应码一致的 HTTP JSON 响应。
     *
     * @param exception SSO 鉴权异常
     * @return 包含统一错误体的 HTTP 响应
     */
    @ExceptionHandler(SsoAuthException.class)
    public ResponseEntity<ApiResponse<Object>> handle(SsoAuthException exception) {
        switch (exception.getCode()) {
            case INTERNAL_ERROR, SERVICE_UNAVAILABLE -> log.error("SSO 鉴权服务异常", exception);
            case BAD_REQUEST, UNAUTHORIZED, FORBIDDEN -> log.debug("SSO 鉴权失败: {}", exception.getMessage());
            case SUCCESS -> log.error("SSO 鉴权异常使用了成功响应码", exception);
            default -> log.error("SSO 鉴权异常使用了未分类响应码: {}", exception.getCode(), exception);
        }
        ApiResponse<Object> body = buildBody(exception);
        return ResponseEntity.status(exception.getCode().getCode()).body(body);
    }

    /**
     * 构建 SSO 鉴权错误体，并在未登录时附带可选的登录入口。
     *
     * @param exception SSO 鉴权异常
     * @return 统一 API 错误体
     */
    private ApiResponse<Object> buildBody(SsoAuthException exception) {
        if (exception.getCode() == ApiCodeEnum.UNAUTHORIZED
                && StringUtils.hasText(properties.getLoginStartUrl())) {
            return ApiResponse.error(exception.getCode(), exception.getMessage(),
                    Map.of(SsoConstants.LOGIN_START_URL_FIELD, properties.getLoginStartUrl()));
        }
        return ApiResponse.error(exception.getCode(), exception.getMessage());
    }
}
