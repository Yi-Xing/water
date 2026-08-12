package top.fblue.auth.context;

import top.fblue.auth.exception.SsoAuthException;
import top.fblue.common.enums.ApiCodeEnum;

/**
 * 管理当前 Dubbo Provider 调用线程绑定的 SSO 用户信息。
 */
public final class SsoRpcContext {

    /** 当前 Dubbo Provider 调用线程关联的 SSO 用户上下文。 */
    private static final ThreadLocal<SsoPrincipal> CURRENT = new ThreadLocal<>();

    /** 工具类不允许实例化。 */
    private SsoRpcContext() {
    }

    /**
     * 将上游传递的 SSO 用户绑定到当前 Dubbo Provider 线程。
     *
     * @param principal 上游传递的 SSO 用户身份
     */
    public static void bind(SsoPrincipal principal) {
        CURRENT.set(principal);
    }

    /**
     * 获取当前 Dubbo Provider 调用的 SSO 用户，上下文缺失时抛出鉴权异常。
     *
     * @return 当前 SSO 用户身份
     */
    public static SsoPrincipal getCurrentUserInfo() {
        SsoPrincipal principal = CURRENT.get();
        if (principal == null) {
            throw new SsoAuthException(ApiCodeEnum.UNAUTHORIZED, "RPC 用户上下文不存在");
        }
        return principal;
    }

    /**
     * 获取当前 Dubbo Provider 调用的可空 SSO 用户。
     *
     * @return 当前 SSO 用户身份，未绑定时返回 {@code null}
     */
    public static SsoPrincipal getNullable() {
        return CURRENT.get();
    }

    /**
     * 获取当前 Dubbo Provider 调用的必登录用户 ID。
     *
     * @return 当前用户 ID
     */
    public static Long getCurrentUserId() {
        return getCurrentUserInfo().getUserId();
    }

    /** 清理当前 Dubbo Provider 线程的 SSO 用户上下文，防止线程复用导致身份泄漏。 */
    public static void clear() {
        CURRENT.remove();
    }
}
