package top.fblue.auth.common;

/**
 * 集中定义 SSO 鉴权在 HTTP、JWT 和 Dubbo 调用链中使用的公共常量。
 */
public final class SsoConstants {

    /** HTTP Bearer Token 使用的标准请求头名称。 */
    public static final String AUTHORIZATION_HEADER = "Authorization";

    /** Authorization 请求头中 Bearer 方案的标准前缀。 */
    public static final String BEARER_PREFIX = "Bearer ";

    /** Token 响应中返回给前端的 Bearer 类型名称。 */
    public static final String BEARER_TOKEN_TYPE = "Bearer";

    /** 用户主动退出时写入撤销记录的标准原因。 */
    public static final String USER_LOGOUT_REASON = "USER_LOGOUT";

    /** HTTP 请求中存放当前 {@code SsoPrincipal} 的 attribute 名称。 */
    public static final String CURRENT_USER_ATTRIBUTE = "ssoPrincipal";

    /** 未登录响应中返回 SSO 登录入口地址的字段名称。 */
    public static final String LOGIN_START_URL_FIELD = "loginStartUrl";

    /** JWT 中表示全局登录会话 ID 的 claim 名称。 */
    public static final String SID_CLAIM = "sid";

    /** JWT 中表示用户数字 ID 的自定义 claim 名称。 */
    public static final String USER_ID_CLAIM = "userId";

    /** JWT 中表示用户名的可选 claim 名称。 */
    public static final String USERNAME_CLAIM = "username";

    /** Redis 中 SSO 会话撤销记录的 Key 前缀。 */
    public static final String REVOKED_SID_KEY_PREFIX = "revoked:sid:";

    /** Redis 中单个 JWT 撤销记录的 Key 前缀。 */
    public static final String REVOKED_JTI_KEY_PREFIX = "revoked:jti:";

    /** Dubbo attachment 中传递用户 ID 的 key。 */
    public static final String RPC_USER_ID = "sso-user-id";

    /** Dubbo attachment 中传递 JWT subject 的 key。 */
    public static final String RPC_SUBJECT = "sso-subject";

    /** Dubbo attachment 中传递全局会话 ID 的 key。 */
    public static final String RPC_SID = "sso-sid";

    /** Dubbo attachment 中传递当前 Token ID 的 key。 */
    public static final String RPC_JTI = "sso-jti";

    /** Dubbo attachment 中传递当前 SSO Client ID 的 key。 */
    public static final String RPC_CLIENT_ID = "sso-client-id";

    /** 常量类不允许实例化。 */
    private SsoConstants() {
    }
}
