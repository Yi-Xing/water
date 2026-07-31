package top.fblue.auth.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 表示通过 SSO 鉴权的当前用户及其 JWT、会话和客户端元数据。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SsoPrincipal implements Serializable {

    /** 序列化版本号，用于保持跨版本序列化兼容性。 */
    @Serial
    private static final long serialVersionUID = 1L;

    /** 当前用户在业务系统中的唯一标识。 */
    private Long userId;

    /** 当前用户的名称，对应 JWT 中的可选 username 声明。 */
    private String username;

    /** JWT subject（sub），用于标识令牌所属主体。 */
    private String subject;

    /** SSO 会话标识（sid），用于会话级撤销。 */
    private String sid;

    /** JWT 唯一标识（jti），用于单个令牌撤销。 */
    private String jti;

    /** JWT 签发方（iss）。 */
    private String issuer;

    /** JWT 接收方（aud）。 */
    private String audience;

    /** 当前接入 SSO 的客户端标识。 */
    private String clientId;

    /** JWT 签发时间（iat），Unix 秒级时间戳。 */
    private long issuedAtEpochSeconds;

    /** JWT 过期时间（exp），Unix 秒级时间戳。 */
    private long expiresAtEpochSeconds;

    /**
     * 计算 JWT 相对于指定时间的剩余有效期。
     *
     * @param nowEpochSeconds 当前 Unix 秒级时间戳
     * @return 剩余有效秒数，已过期时返回 {@code 0}
     */
    public long getRemainingSeconds(long nowEpochSeconds) {
        return Math.max(0L, expiresAtEpochSeconds - nowEpochSeconds);
    }
}
