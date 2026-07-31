package top.fblue.auth.dubbo.filter;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import top.fblue.auth.common.SsoConstants;
import top.fblue.auth.context.SsoHttpContext;
import top.fblue.auth.context.SsoPrincipal;

/**
 * 在 Dubbo Consumer 侧将当前 HTTP 的用户身份写入调用附件传递给下游 Dubbo 服务，并在调用结束后清理附件。
 *
 */
@Activate(group = CommonConstants.CONSUMER, order = 100)
public class DubboUserConsumerFilter implements Filter {

    /**
     * 在受保护的 Dubbo 调用发出前，将当前 HTTP SSO 用户写入 Consumer Attachment。
     *
     * @param invoker    Dubbo 服务调用器
     * @param invocation 当前 RPC 调用信息
     * @return 下游 Dubbo 调用结果
     */
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) {
        if (RpcPublicUtils.isPublic(invoker, invocation)) {
            return invoker.invoke(invocation);
        }
        SsoPrincipal principal = SsoHttpContext.getCurrentUserInfo();
        var attachments = RpcContext.getClientAttachment();
        attachments.setAttachment(SsoConstants.RPC_USER_ID, String.valueOf(principal.getUserId()));
        attachments.setAttachment(SsoConstants.RPC_SUBJECT, principal.getSubject());
        attachments.setAttachment(SsoConstants.RPC_SID, principal.getSid());
        attachments.setAttachment(SsoConstants.RPC_JTI, principal.getJti());
        attachments.setAttachment(SsoConstants.RPC_CLIENT_ID, principal.getClientId());
        try {
            return invoker.invoke(invocation);
        } finally {
            attachments.removeAttachment(SsoConstants.RPC_USER_ID);
            attachments.removeAttachment(SsoConstants.RPC_SUBJECT);
            attachments.removeAttachment(SsoConstants.RPC_SID);
            attachments.removeAttachment(SsoConstants.RPC_JTI);
            attachments.removeAttachment(SsoConstants.RPC_CLIENT_ID);
        }
    }
}
