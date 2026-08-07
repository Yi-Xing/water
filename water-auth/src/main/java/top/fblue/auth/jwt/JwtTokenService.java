package top.fblue.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.util.StringUtils;
import top.fblue.auth.common.SsoConstants;
import top.fblue.auth.config.SsoAuthProperties;
import top.fblue.auth.context.SsoPrincipal;
import top.fblue.auth.exception.SsoAuthException;
import top.fblue.common.enums.ApiCodeEnum;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 使用 HS512 签发 SSO JWT，并校验签名、受众、签发方及必要的会话声明。
 */
public class JwtTokenService {

    /** 允许的唯一 JWT 签名算法，防止算法降级。 */
    private static final String REQUIRED_ALGORITHM = "HS512";

    /** SSO 客户端、JWT 签发和验签所需的统一配置。 */
    private final SsoAuthProperties properties;

    /** 获取 JWT 签发和校验时间的时钟，可在测试中替换为固定时钟。 */
    private final Clock clock;

    /**
     * 使用当前服务器默认时区时钟创建 JWT 服务。
     *
     * <p>Spring 应用应优先使用接收 {@link Clock} 的构造方法，由 Water 自动配置注入统一时钟；
     * 此构造方法仅保留给手动创建服务的兼容场景。</p>
     *
     * @param properties SSO 鉴权配置
     */
    public JwtTokenService(SsoAuthProperties properties) {
        this(properties, Clock.systemDefaultZone());
    }

    /**
     * 使用可替换时钟创建 JWT 服务，便于稳定验证时间边界。
     *
     * @param properties SSO 鉴权配置
     * @param clock      JWT 签发和校验使用的时钟
     */
    public JwtTokenService(SsoAuthProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
    }

    /**
     * 使用 HS512 签发包含用户和会话声明的 SSO JWT。
     * Token 最终过期时间取会话过期时间与配置最长有效期的较早值。
     *
     * @param userId                业务用户 ID
     * @param username              可选用户名
     * @param sid                   SSO 会话标识
     * @param expiresAtEpochSeconds 会话过期时间，Unix 秒级时间戳
     * @return 已签名的紧凑 JWT 字符串
     */
    public String createToken(Long userId, String username, String sid, long expiresAtEpochSeconds) {
        validateSigningConfiguration();
        if (userId == null || !StringUtils.hasText(sid)) {
            throw new IllegalArgumentException("userId 和 sid 不能为空");
        }
        long now = clock.instant().getEpochSecond();
        long configuredExpireAt = now + properties.getJwt().getExpirationSeconds();
        long expireAt = Math.min(expiresAtEpochSeconds, configuredExpireAt);
        if (expireAt <= now) {
            throw new IllegalArgumentException("会话已过期，不能签发 token");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put(SsoConstants.USER_ID_CLAIM, userId);
        claims.put(SsoConstants.SID_CLAIM, sid);
        if (StringUtils.hasText(username)) {
            claims.put(SsoConstants.USERNAME_CLAIM, username);
        }

        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userId))
                .issuer(properties.getJwt().getIssuer())
                .audience().add(properties.getJwt().getAudience()).and()
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(Instant.ofEpochSecond(now)))
                .expiration(Date.from(Instant.ofEpochSecond(expireAt)))
                .signWith(signingKey(), Jwts.SIG.HS512)
                .compact();
    }

    /**
     * 验证 JWT 签名、算法、签发方、接收方、时间和必需声明，并恢复 SSO 用户身份。
     *
     * @param token 待校验的紧凑 JWT 字符串
     * @return 通过校验的 SSO 用户身份
     */
    public SsoPrincipal parseAndValidate(String token) {
        validateSigningConfiguration();
        if (!StringUtils.hasText(token)) {
            throw unauthorized("缺少访问令牌");
        }
        try {
            Jws<Claims> parsed = Jwts.parser()
                    .verifyWith(signingKey())
                    .clock(() -> Date.from(clock.instant()))
                    .clockSkewSeconds(properties.getJwt().getAllowedClockSkewSeconds())
                    .build()
                    .parseSignedClaims(token);
            if (!REQUIRED_ALGORITHM.equals(parsed.getHeader().getAlgorithm())) {
                throw unauthorized("JWT 签名算法不允许");
            }
            Claims claims = parsed.getPayload();
            validateRegisteredClaims(claims);
            return toPrincipal(claims);
        } catch (SsoAuthException e) {
            throw e;
        } catch (Exception e) {
            throw unauthorized("访问令牌无效或已过期");
        }
    }

    /**
     * 校验 JWT 注册声明和业务必需声明之间的一致性。
     *
     * @param claims 已通过签名验证的 JWT Claims
     */
    private void validateRegisteredClaims(Claims claims) {
        if (!properties.getJwt().getIssuer().equals(claims.getIssuer())) {
            throw unauthorized("JWT issuer 不匹配");
        }
        if (!containsAudience(claims.get("aud"), properties.getJwt().getAudience())) {
            throw unauthorized("JWT audience 不匹配");
        }
        Object rawUserId = claims.get(SsoConstants.USER_ID_CLAIM);
        if (!StringUtils.hasText(claims.getSubject())
                || !StringUtils.hasText(claims.getId())
                || !StringUtils.hasText(claims.get(SsoConstants.SID_CLAIM, String.class))
                || !(rawUserId instanceof Number)
                || claims.getExpiration() == null
                || claims.getIssuedAt() == null) {
            throw unauthorized("JWT 必要字段缺失");
        }
        long userId = ((Number) rawUserId).longValue();
        if (!claims.getSubject().equals(String.valueOf(userId))) {
            throw unauthorized("JWT sub 与 userId 不匹配");
        }
        long issuedAt = claims.getIssuedAt().toInstant().getEpochSecond();
        long expiresAt = claims.getExpiration().toInstant().getEpochSecond();
        long latestAllowedIssuedAt = clock.instant().getEpochSecond()
                + properties.getJwt().getAllowedClockSkewSeconds();
        if (issuedAt > latestAllowedIssuedAt || expiresAt <= issuedAt) {
            throw unauthorized("JWT 时间字段不合法");
        }
    }

    /**
     * 将已校验 JWT Claims 转换为业务可使用的 SSO 用户身份。
     *
     * @param claims 已通过完整校验的 JWT Claims
     * @return SSO 用户身份
     */
    private SsoPrincipal toPrincipal(Claims claims) {
        Long userId = ((Number) claims.get(SsoConstants.USER_ID_CLAIM)).longValue();
        return SsoPrincipal.builder()
                .userId(userId)
                .username(claims.get(SsoConstants.USERNAME_CLAIM, String.class))
                .subject(claims.getSubject())
                .sid(claims.get(SsoConstants.SID_CLAIM, String.class))
                .jti(claims.getId())
                .issuer(claims.getIssuer())
                .audience(properties.getJwt().getAudience())
                .clientId(properties.getClientId())
                .issuedAtEpochSeconds(claims.getIssuedAt().toInstant().getEpochSecond())
                .expiresAtEpochSeconds(claims.getExpiration().toInstant().getEpochSecond())
                .build();
    }

    /**
     * 兼容单值和集合两种 aud 声明表达，判断其是否包含预期接收方。
     *
     * @param audienceClaim   JWT aud 原始声明
     * @param expectedAudience 配置的预期接收方
     * @return aud 声明包含预期接收方时返回 {@code true}
     */
    private boolean containsAudience(Object audienceClaim, String expectedAudience) {
        if (audienceClaim instanceof String audience) {
            return expectedAudience.equals(audience);
        }
        if (audienceClaim instanceof Collection<?> audiences) {
            return audiences.stream().anyMatch(expectedAudience::equals);
        }
        return false;
    }

    /**
     * 从 UTF-8 配置密钥构建 HS512 签名和验签所需的对称密钥。
     *
     * @return HS512 对称密钥
     */
    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(properties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /** 在签发或验证 JWT 前检查密钥、签发方和接收方配置是否完整。 */
    private void validateSigningConfiguration() {
        if (!StringUtils.hasText(properties.getJwt().getSecret())
                || !StringUtils.hasText(properties.getJwt().getIssuer())
                || !StringUtils.hasText(properties.getJwt().getAudience())) {
            throw new IllegalStateException("sso.jwt.secret/issuer/audience 必须配置");
        }
    }

    /**
     * 构建一个统一未授权响应码的 SSO 鉴权异常。
     *
     * @param message 可对外展示的鉴权失败信息
     * @return SSO 未授权异常
     */
    private SsoAuthException unauthorized(String message) {
        return new SsoAuthException(ApiCodeEnum.UNAUTHORIZED, message);
    }
}
