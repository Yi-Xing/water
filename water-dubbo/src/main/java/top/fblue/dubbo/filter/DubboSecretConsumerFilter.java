package top.fblue.dubbo.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import top.fblue.dubbo.constant.RpcConst;
import top.fblue.dubbo.context.DubboSecretConfig;

import static org.apache.dubbo.common.constants.CommonConstants.CONSUMER;
import static top.fblue.dubbo.constant.RpcConst.REMOTE_APPLICATION_KEY;

/**
 * Dubbo RPC 消费端密钥注入过滤器。
 */
@Slf4j
@Activate(group = CONSUMER, order = 1)
public class DubboSecretConsumerFilter implements Filter {

    /**
     * 根据目标应用注入 RPC 密钥并在调用结束后清理。
     *
     * @param invoker RPC 调用器
     * @param invocation RPC 调用信息
     * @return RPC 调用结果
     * @throws RpcException 目标应用或密钥配置无效时抛出
     */
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        RpcContext.getClientAttachment().removeAttachment(RpcConst.SECRET_KEY);
        String secret = resolveSecret(invoker);
        RpcContext.getClientAttachment().setAttachment(RpcConst.SECRET_KEY, secret);
        try {
            return invoker.invoke(invocation);
        } finally {
            // 防止线程池复用导致 attachment 泄漏
            RpcContext.getClientAttachment().removeAttachment(RpcConst.SECRET_KEY);
        }
    }

    /**
     * 获取目标 Provider 的鉴权秘钥。
     *
     * @param invoker RPC 调用器
     * @return 目标 Provider 的鉴权秘钥
     */
    private String resolveSecret(Invoker<?> invoker) {
        URL url = invoker.getUrl();
        String providerAppName = url.getParameter(RpcConst.TARGET_APPLICATION_KEY);
        if (providerAppName == null || providerAppName.isBlank()) {
            providerAppName = url.getParameter(REMOTE_APPLICATION_KEY);
        }
        if (providerAppName == null || providerAppName.isBlank()) {
            throw new RpcException("RPC 鉴权失败：无法识别目标应用");
        }

        String secret = DubboSecretConfig.getInstance().getSecret().get(providerAppName);
        if (secret == null || secret.isBlank()) {
            throw new RpcException("RPC 鉴权失败：未配置目标应用 " + providerAppName + " 的秘钥");
        }
        return secret;
    }
}
