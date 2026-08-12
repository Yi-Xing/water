package top.fblue.auth.context;

import org.springframework.web.context.request.RequestContextHolder;
import top.fblue.auth.exception.SsoAuthException;
import top.fblue.common.enums.ApiCodeEnum;

import java.util.Objects;
import java.util.UUID;

/**
 * 统一解析当前执行线程的 SSO 用户，支持 HTTP、RPC 和受信定时任务三种入口。
 */
public final class SsoUserContext {

    /** 定时任务跨服务透传时使用的客户端标识。 */
    private static final String SCHEDULED_TASK_CLIENT_ID = "scheduler";

    /** 当前线程绑定的定时任务用户。 */
    private static final ThreadLocal<SsoPrincipal> SCHEDULED_TASK_CURRENT = new ThreadLocal<>();

    /** 工具类不允许实例化。 */
    private SsoUserContext() {
    }

    /**
     * 获取当前执行线程的必登录用户。
     * HTTP 请求严格读取 HTTP 用户；非 HTTP 调用依次读取 RPC 用户和定时任务用户。
     *
     * @return 当前 SSO 用户身份
     */
    public static SsoPrincipal getCurrentUserInfo() {
        if (RequestContextHolder.getRequestAttributes() != null) {
            return SsoHttpContext.getCurrentUserInfo();
        }
        SsoPrincipal rpcPrincipal = SsoRpcContext.getNullable();
        if (rpcPrincipal != null) {
            return rpcPrincipal;
        }
        SsoPrincipal scheduledTaskPrincipal = SCHEDULED_TASK_CURRENT.get();
        if (scheduledTaskPrincipal != null) {
            return scheduledTaskPrincipal;
        }
        throw new SsoAuthException(ApiCodeEnum.UNAUTHORIZED, "用户上下文不存在");
    }

    /**
     * 获取当前执行线程的必登录用户 ID。
     *
     * @return 当前用户 ID
     */
    public static Long getCurrentUserId() {
        return getCurrentUserInfo().getUserId();
    }

    /**
     * 使用指定用户身份运行受信定时任务，并在任务结束后恢复原上下文。
     *
     * @param userId 定时任务使用的用户 ID
     * @param task 定时任务逻辑
     */
    public static void runAsScheduledTaskUser(Long userId, Runnable task) {
        Objects.requireNonNull(userId, "定时任务用户 ID 不能为空");
        Objects.requireNonNull(task, "定时任务逻辑不能为空");
        SsoPrincipal previousPrincipal = SCHEDULED_TASK_CURRENT.get();
        SCHEDULED_TASK_CURRENT.set(createScheduledTaskPrincipal(userId));
        try {
            task.run();
        } finally {
            restoreScheduledTaskPrincipal(previousPrincipal);
        }
    }

    /** 清理当前线程的定时任务用户，主要用于线程复用防护和测试隔离。 */
    public static void clearScheduledTaskUser() {
        SCHEDULED_TASK_CURRENT.remove();
    }

    /**
     * 创建可在受保护 RPC 调用中完整透传的定时任务用户身份。
     *
     * @param userId 定时任务使用的用户 ID
     * @return 定时任务用户身份
     */
    private static SsoPrincipal createScheduledTaskPrincipal(Long userId) {
        String taskIdentity = "scheduled-task:" + userId;
        return SsoPrincipal.builder()
                .userId(userId)
                .subject(taskIdentity)
                .sid(taskIdentity)
                .jti(UUID.randomUUID().toString())
                .clientId(SCHEDULED_TASK_CLIENT_ID)
                .build();
    }

    /**
     * 恢复进入定时任务前的用户上下文，支持同一线程嵌套执行任务。
     *
     * @param previousPrincipal 原定时任务用户身份
     */
    private static void restoreScheduledTaskPrincipal(SsoPrincipal previousPrincipal) {
        if (previousPrincipal == null) {
            SCHEDULED_TASK_CURRENT.remove();
            return;
        }
        SCHEDULED_TASK_CURRENT.set(previousPrincipal);
    }
}
