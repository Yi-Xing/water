package top.fblue.auth.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import top.fblue.auth.common.SsoConstants;
import top.fblue.auth.exception.SsoAuthException;
import top.fblue.common.enums.ApiCodeEnum;

/**
 * 从 HTTP Authorization 请求头中解析并校验 Bearer 访问令牌。
 */
public class BearerTokenResolver {

    /**
     * 从 Authorization 请求头解析非空 Bearer Token。
     *
     * @param request HTTP 请求
     * @return 去除 Bearer 前缀和首尾空白后的 Token
     */
    public String resolve(HttpServletRequest request) {
        String authorization = request.getHeader(SsoConstants.AUTHORIZATION_HEADER);
        if (!StringUtils.hasText(authorization)
                || !authorization.regionMatches(true, 0, SsoConstants.BEARER_PREFIX,
                0, SsoConstants.BEARER_PREFIX.length())) {
            throw new SsoAuthException(ApiCodeEnum.UNAUTHORIZED, "缺少 Bearer 访问令牌");
        }
        String token = authorization.substring(SsoConstants.BEARER_PREFIX.length()).trim();
        if (!StringUtils.hasText(token)) {
            throw new SsoAuthException(ApiCodeEnum.UNAUTHORIZED, "访问令牌为空");
        }
        return token;
    }
}
