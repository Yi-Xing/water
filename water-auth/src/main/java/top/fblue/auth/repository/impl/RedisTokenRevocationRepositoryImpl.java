package top.fblue.auth.repository.impl;

import org.springframework.data.redis.core.StringRedisTemplate;
import top.fblue.auth.common.SsoConstants;
import top.fblue.auth.config.SsoAuthProperties;
import top.fblue.auth.repository.TokenRevocationRepository;

import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 保存带有效期的会话和令牌撤销记录。
 */
public class RedisTokenRevocationRepositoryImpl implements TokenRevocationRepository {

    /** 读写 Redis 撤销记录的字符串操作模板。 */
    private final StringRedisTemplate redisTemplate;

    /** 隔离 SSO 撤销数据的 Redis Key 前缀。 */
    private final String keyPrefix;

    /** 为覆盖 JWT 时钟偏差而追加到撤销记录的宽限时间，单位秒。 */
    private final long revocationGraceSeconds;

    /**
     * @param redisTemplate Redis 字符串操作模板
     * @param properties    SSO 配置，用于获取 Key 前缀和撤销宽限时间
     */
    public RedisTokenRevocationRepositoryImpl(StringRedisTemplate redisTemplate, SsoAuthProperties properties) {
        this.redisTemplate = redisTemplate;
        this.keyPrefix = properties.getRedisKeyPrefix();
        this.revocationGraceSeconds = properties.getJwt().getAllowedClockSkewSeconds();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isSidRevoked(String sid) {
        return sid != null && Boolean.TRUE.equals(
                redisTemplate.hasKey(keyPrefix + SsoConstants.REVOKED_SID_KEY_PREFIX + sid));
    }

    /** {@inheritDoc} */
    @Override
    public boolean isJtiRevoked(String jti) {
        return jti != null && Boolean.TRUE.equals(
                redisTemplate.hasKey(keyPrefix + SsoConstants.REVOKED_JTI_KEY_PREFIX + jti));
    }

    /** {@inheritDoc} */
    @Override
    public void revokeSid(String sid, String reason, long ttlSeconds) {
        if (sid != null && ttlSeconds > 0) {
            redisTemplate.opsForValue().set(keyPrefix + SsoConstants.REVOKED_SID_KEY_PREFIX + sid,
                    reason == null ? "REVOKED" : reason,
                    ttlSeconds + revocationGraceSeconds, TimeUnit.SECONDS);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void revokeJti(String jti, String reason, long ttlSeconds) {
        if (jti != null && ttlSeconds > 0) {
            redisTemplate.opsForValue().set(keyPrefix + SsoConstants.REVOKED_JTI_KEY_PREFIX + jti,
                    reason == null ? "REVOKED" : reason,
                    ttlSeconds + revocationGraceSeconds, TimeUnit.SECONDS);
        }
    }
}
