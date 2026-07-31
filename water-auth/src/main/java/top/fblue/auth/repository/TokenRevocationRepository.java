package top.fblue.auth.repository;

/**
 * 定义按会话 ID（sid）或令牌 ID（jti）查询和登记撤销状态的存储接口。
 */
public interface TokenRevocationRepository {

    /**
     * 判断指定 SSO 会话是否已被撤销。
     *
     * @param sid SSO 会话标识
     * @return {@code true} 表示会话已撤销，否则返回 {@code false}
     */
    boolean isSidRevoked(String sid);

    /**
     * 判断指定 JWT 是否已被撤销。
     *
     * @param jti JWT 唯一标识
     * @return {@code true} 表示 Token 已撤销，否则返回 {@code false}
     */
    boolean isJtiRevoked(String jti);

    /**
     * 登记 SSO 会话撤销记录，使该会话下的 Token 失效。
     *
     * @param sid        SSO 会话标识
     * @param reason     撤销原因，可为 {@code null}
     * @param ttlSeconds 撤销记录的有效期，单位秒
     */
    void revokeSid(String sid, String reason, long ttlSeconds);

    /**
     * 登记单个 JWT 撤销记录。
     *
     * @param jti        JWT 唯一标识
     * @param reason     撤销原因，可为 {@code null}
     * @param ttlSeconds 撤销记录的有效期，单位秒
     */
    void revokeJti(String jti, String reason, long ttlSeconds);
}
