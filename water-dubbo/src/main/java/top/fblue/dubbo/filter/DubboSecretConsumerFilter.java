package top.fblue.dubbo.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import top.fblue.dubbo.constant.RpcConst;
import top.fblue.dubbo.context.DubboSecretConfig;


import static org.apache.dubbo.common.constants.CommonConstants.CONSUMER;
import static top.fblue.dubbo.constant.RpcConst.URL_APPLICATION_NAME_KEY;

@Slf4j
@Activate(group = CONSUMER, order = 1)
public class DubboSecretConsumerFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String secret = resolveSecret(invoker, invocation);
        RpcContext.getClientAttachment().setAttachment(RpcConst.SECRET_KEY, secret);
        try {
            return invoker.invoke(invocation);
        } finally {
            // 防止线程池复用导致 attachment 泄漏
            RpcContext.getClientAttachment().removeAttachment(RpcConst.SECRET_KEY);
        }
    }

    /**
     * 获取要调用的 provider 的 secret
     */
    private String resolveSecret(Invoker<?> invoker, Invocation invocation) {
        URL url = invoker.getUrl();
        String providerAppName = url.getParameter(URL_APPLICATION_NAME_KEY);
        return DubboSecretConfig.getInstance().getSecret().get(providerAppName);
    }
}
