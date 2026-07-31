package top.fblue.auth.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

/**
 * 绑定并校验 {@code sso} 前缀下的鉴权、JWT 和状态 Cookie 配置。
 */
@Data
@Validated
@ConfigurationProperties(prefix = "sso")
public class SsoAuthProperties {

    /** 是否启用 SSO 自动配置和 HTTP 鉴权拦截器，默认关闭。 */
    private boolean enabled;

    /** 当前接入 SSO 的业务客户端唯一标识。 */
    @NotBlank
    private String clientId;

    /** 未登录响应中返回给前端的可选 SSO 登录入口地址。 */
    private String loginStartUrl;

    /** 需要执行 SSO 鉴权的 HTTP 路径模式，默认拦截 {@code /api/**}。 */
    @NotEmpty
    private List<String> pathPatterns = new ArrayList<>(List.of("/api/**"));

    /** 从 SSO 鉴权拦截范围中排除的 HTTP 路径模式。 */
    private List<String> excludePaths = new ArrayList<>();

    /** Redis 中 SSO 会话和 Token 撤销记录使用的 Key 前缀。 */
    @NotBlank
    private String redisKeyPrefix = "sso:";

    /** JWT 签发、验签和有效期配置。 */
    @Valid
    private Jwt jwt = new Jwt();

    /** SSO 登录状态校验 Cookie 配置。 */
    @Valid
    private StateCookie stateCookie = new StateCookie();

    /**
     * JWT 签发、验签和有效期相关配置。
     */
    @Data
    public static class Jwt {

        /** JWT 签发方（iss），签发和验证时必须一致。 */
        @NotBlank
        private String issuer;

        /** JWT 预期接收方（aud），用于限制 Token 的使用范围。 */
        @NotBlank
        private String audience;

        /** HS512 签名密钥，至少 64 个字符。 */
        @NotBlank
        @Size(min = 64, message = "HS512 secret 至少需要 64 个字符")
        private String secret;

        /** JWT 允许的最长有效期，单位秒，默认 30 天。 */
        @Positive
        private long expirationSeconds = 2_592_000L;

        /** JWT 时间校验允许的时钟偏差，单位秒，默认 30 秒，最大 300 秒。 */
        @PositiveOrZero
        @Max(300)
        private long allowedClockSkewSeconds = 30L;
    }

    /**
     * SSO 登录状态校验 Cookie 的安全属性和存活时间配置。
     */
    @Data
    public static class StateCookie {

        /** 保存 SSO 登录状态校验值的 Cookie 名称。 */
        @NotBlank
        private String name = "sso_state";

        /** 是否仅允许通过 HTTPS 传输 Cookie，默认启用。 */
        private boolean secure = true;

        /** Cookie SameSite 策略，可选 {@code Lax}、{@code Strict} 或 {@code None}。 */
        @NotBlank
        @Pattern(regexp = "(?i)Lax|Strict|None")
        private String sameSite = "Lax";

        /** Cookie 最长存活时间，单位秒，默认 300 秒。 */
        @Positive
        private int maxAgeSeconds = 300;

        /** Cookie 生效的 HTTP 路径，默认覆盖整个站点。 */
        @NotBlank
        private String path = "/";

        /**
         * 校验 SameSite=None 的 Cookie 是否同时启用 Secure。
         *
         * @return Cookie 安全属性组合可被现代浏览器接受时返回 {@code true}
         */
        @AssertTrue(message = "SameSite=None 时必须启用 Secure")
        public boolean isSameSiteSecurityValid() {
            return !"None".equalsIgnoreCase(sameSite) || secure;
        }
    }
}
