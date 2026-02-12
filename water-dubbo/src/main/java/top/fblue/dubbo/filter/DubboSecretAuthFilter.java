package top.fblue.dubbo.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.*;
import top.fblue.common.exception.BusinessException;
import top.fblue.dubbo.context.DubboSecretConfig;
import top.fblue.dubbo.constant.RpcConst;

import java.util.Objects;

import static org.apache.dubbo.common.constants.CommonConstants.PROVIDER;

/**
 * Dubbo RPC 秘钥鉴权 Filter（服务端）
 * 校验调用方在 RpcContext 中传递的 rpc-secret 与配置的秘钥是否一致。
 * order = 2，在 DubboExceptionFilter(1) 之后执行，鉴权异常由 ExceptionFilter 统一捕获。
 */
@Slf4j
@Activate(group = PROVIDER, order = 2)
public class DubboSecretAuthFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        DubboSecretConfig dubboSecretConfig = DubboSecretConfig.getInstance();
        if (dubboSecretConfig == null || !dubboSecretConfig.isAuthEnabled()) {
            return invoker.invoke(invocation);
        }
        String expectedSecret = DubboSecretConfig.getApplicationSecret();
        // 从调用方传递的 attachment 中获取秘钥（消费端通过 RpcContext.getClientAttachment().setAttachment("rpc-secret", secret) 传递）
        String clientSecret = RpcContext.getServerAttachment().getAttachment(RpcConst.SECRET_KEY);
        try {
            if (!Objects.equals(expectedSecret, clientSecret)) {
                log.warn("RPC 鉴权失败 clientSecret={}", clientSecret);
                throw new RpcException("RPC 鉴权失败：秘钥无效或未传递");
            }
            return invoker.invoke(invocation);
        } finally {
            // 鉴权后移除，避免后续链路中继续携带或被打日志
            RpcContext.getServerAttachment().removeAttachment(RpcConst.SECRET_KEY);
        }
    }
}
