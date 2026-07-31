package top.fblue.auth.dubbo.filter;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.springframework.util.StringUtils;
import top.fblue.auth.common.SsoConstants;
import top.fblue.auth.context.SsoPrincipal;
import top.fblue.auth.context.SsoRpcContext;

/**
 * 在 Dubbo Provider 侧解析上游传递在附件中的用户身份，并维护服务端 RPC 用户上下文。
 */
@Activate(group = CommonConstants.PROVIDER, order = 100)
public class DubboUserProviderFilter implements Filter {

    /**
     * 在受保护的 Dubbo 方法执行前验证上游用户附件，重建并绑定 Provider SSO 上下文。
     *
     * @param invoker    Dubbo 服务调用器
     * @param invocation 当前 RPC 调用信息
     * @return Provider 业务方法的调用结果
     */
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) {
        if (RpcPublicUtils.isPublic(invoker, invocation)) {
            return invoker.invoke(invocation);
        }
        var attachments = RpcContext.getServerAttachment();
        String userId = attachments.getAttachment(SsoConstants.RPC_USER_ID);
        String subject = attachments.getAttachment(SsoConstants.RPC_SUBJECT);
        String sid = attachments.getAttachment(SsoConstants.RPC_SID);
        String jti = attachments.getAttachment(SsoConstants.RPC_JTI);
        String clientId = attachments.getAttachment(SsoConstants.RPC_CLIENT_ID);
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(subject)
                || !StringUtils.hasText(sid) || !StringUtils.hasText(jti)
                || !StringUtils.hasText(clientId)) {
            throw new RpcException("RPC 用户上下文不存在");
        }
        long parsedUserId;
        try {
            parsedUserId = Long.parseLong(userId);
        } catch (NumberFormatException exception) {
            throw new RpcException("RPC 用户 ID 格式不合法", exception);
        }
        SsoPrincipal principal = SsoPrincipal.builder()
                .userId(parsedUserId)
                .subject(subject)
                .sid(sid)
                .jti(jti)
                .clientId(clientId)
                .build();
        SsoRpcContext.bind(principal);
        try {
            return invoker.invoke(invocation);
        } finally {
            SsoRpcContext.clear();
            attachments.removeAttachment(SsoConstants.RPC_USER_ID);
            attachments.removeAttachment(SsoConstants.RPC_SUBJECT);
            attachments.removeAttachment(SsoConstants.RPC_SID);
            attachments.removeAttachment(SsoConstants.RPC_JTI);
            attachments.removeAttachment(SsoConstants.RPC_CLIENT_ID);
        }
    }
}
