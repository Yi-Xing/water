package top.fblue.auth.web;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import top.fblue.auth.config.SsoAuthProperties;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

/**
 * 生成 SSO 登录 state 随机值，并负责对应 HttpOnly Cookie 的读、写和清除。
 */
public class SsoStateCookieService {

    /** 生成不可预测 SSO state 值的密码安全随机源。 */
    private final SecureRandom secureRandom = new SecureRandom();

    /** SSO state Cookie 的名称、安全属性、路径和有效期配置。 */
    private final SsoAuthProperties.StateCookie properties;

    /**
     * @param properties SSO 鉴权配置
     */
    public SsoStateCookieService(SsoAuthProperties properties) {
        this.properties = properties.getStateCookie();
    }

    /**
     * 生成用于防止登录请求伪造的 256 位 URL 安全 state 值。
     *
     * @return 不含填充符的 Base64 URL 安全 state 字符串
     */
    public String generateState() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * 将 SSO state 写入 HttpOnly Cookie。
     *
     * @param response HTTP 响应
     * @param state    待保存的 state 值
     */
    public void write(HttpServletResponse response, String state) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie(state, properties.getMaxAgeSeconds()).toString());
    }

    /**
     * 通过写入过期 Cookie 清除客户端保存的 SSO state。
     *
     * @param response HTTP 响应
     */
    public void clear(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie("", 0).toString());
    }

    /**
     * 从 HTTP 请求中读取 SSO state Cookie。
     *
     * @param request HTTP 请求
     * @return state 值，Cookie 不存在时返回 {@code null}
     */
    public String read(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (properties.getName().equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /**
     * 按当前安全配置构建 HttpOnly SSO state Cookie。
     *
     * @param value         Cookie 值
     * @param maxAgeSeconds Cookie 存活时间，单位秒
     * @return 构建完成的响应 Cookie
     */
    private ResponseCookie cookie(String value, int maxAgeSeconds) {
        return ResponseCookie.from(properties.getName(), value)
                .httpOnly(true)
                .secure(properties.isSecure())
                .sameSite(properties.getSameSite())
                .path(properties.getPath())
                .maxAge(Duration.ofSeconds(maxAgeSeconds))
                .build();
    }
}
