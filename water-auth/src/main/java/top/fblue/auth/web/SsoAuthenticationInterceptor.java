package top.fblue.auth.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import top.fblue.auth.annotation.SsoPublic;
import top.fblue.auth.common.SsoConstants;
import top.fblue.auth.context.SsoHttpContext;
import top.fblue.auth.context.SsoPrincipal;
import top.fblue.auth.exception.SsoAuthException;
import top.fblue.auth.jwt.JwtTokenService;
import top.fblue.auth.repository.TokenRevocationRepository;
import top.fblue.common.enums.ApiCodeEnum;

/**
 * 校验受保护 HTTP 请求的 Bearer Token 和撤销状态，并维护请求线程中的用户上下文。
 */
public class SsoAuthenticationInterceptor implements AsyncHandlerInterceptor {

    /** 从 HTTP Authorization 请求头提取 Bearer Token。 */
    private final BearerTokenResolver tokenResolver;

    /** 校验 JWT 并恢复 SSO 用户身份。 */
    private final JwtTokenService jwtTokenService;

    /** 查询 SSO 会话和单个 Token 的撤销状态。 */
    private final TokenRevocationRepository revocationRepository;

    /**
     * @param tokenResolver        Bearer Token 解析器
     * @param jwtTokenService      JWT 验证服务
     * @param revocationRepository 会话和 Token 撤销存储
     */
    public SsoAuthenticationInterceptor(BearerTokenResolver tokenResolver,
                                        JwtTokenService jwtTokenService,
                                        TokenRevocationRepository revocationRepository) {
        this.tokenResolver = tokenResolver;
        this.jwtTokenService = jwtTokenService;
        this.revocationRepository = revocationRepository;
    }

    /**
     * 在 Controller 执行前校验 Bearer Token 和撤销状态，并绑定当前 SSO 用户上下文。
     *
     * @return 校验通过时返回 {@code true}，失败时抛出统一 SSO 鉴权异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        SsoHttpContext.clear();
        if (HttpMethod.OPTIONS.matches(request.getMethod()) || isPublic(handler)) {
            return true;
        }
        SsoPrincipal principal = jwtTokenService.parseAndValidate(tokenResolver.resolve(request));
        validateNotRevoked(principal);
        request.setAttribute(SsoConstants.CURRENT_USER_ATTRIBUTE, principal);
        SsoHttpContext.bind(principal);
        return true;
    }

    /** 在 HTTP 请求完成后清理当前线程的 SSO 用户上下文。 */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        SsoHttpContext.clear();
    }

    /** 在请求转入异步处理前清理原请求线程中的 SSO 用户上下文。 */
    @Override
    public void afterConcurrentHandlingStarted(HttpServletRequest request, HttpServletResponse response,
                                               Object handler) {
        SsoHttpContext.clear();
    }

    /**
     * 判断当前 HTTP 处理器是否允许匿名访问。
     *
     * @param handler Spring MVC 处理器
     * @return 非 Controller 方法或标注 {@link SsoPublic} 时返回 {@code true}
     */
    private boolean isPublic(Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        return handlerMethod.hasMethodAnnotation(SsoPublic.class)
                || handlerMethod.getBeanType().isAnnotationPresent(SsoPublic.class);
    }

    /**
     * 校验当前用户的 SSO 会话和 Token 是否已撤销。
     * Redis 访问异常会转换为可对外返回的服务不可用异常。
     *
     * @param principal 已通过 JWT 校验的 SSO 用户身份
     */
    private void validateNotRevoked(SsoPrincipal principal) {
        try {
            if (revocationRepository.isSidRevoked(principal.getSid())
                    || revocationRepository.isJtiRevoked(principal.getJti())) {
                throw new SsoAuthException(ApiCodeEnum.UNAUTHORIZED, "登录会话已退出");
            }
        } catch (DataAccessException exception) {
            throw new SsoAuthException(ApiCodeEnum.SERVICE_UNAVAILABLE, "鉴权服务暂不可用", exception);
        }
    }
}
