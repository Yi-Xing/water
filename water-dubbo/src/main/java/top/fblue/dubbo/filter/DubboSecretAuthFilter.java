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

    /**
     * 校验调用方密钥，并在执行服务逻辑前清除服务端密钥附件。
     *
     * @param invoker RPC 调用器
     * @param invocation RPC 调用信息
     * @return RPC 调用结果
     * @throws RpcException 调用方密钥校验失败时抛出
     */
    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String clientSecret = RpcContext.getServerAttachment().getAttachment(RpcConst.SECRET_KEY);
        // 执行业务逻辑前清除，避免嵌套 RPC 自动透传上一跳密钥。
        // 嵌套 RPC 调用理论会覆盖上一跳密钥，如果没有生成时，应该报错没有秘钥，而不是因为透传导致报错秘钥不正确
        RpcContext.getServerAttachment().removeAttachment(RpcConst.SECRET_KEY);

        DubboSecretConfig dubboSecretConfig = DubboSecretConfig.getInstance();
        if (dubboSecretConfig == null || !dubboSecretConfig.isAuthEnabled()) {
            return invoker.invoke(invocation);
        }
        String expectedSecret = DubboSecretConfig.getApplicationSecret();
        if (!Objects.equals(expectedSecret, clientSecret)) {
            log.warn("RPC 鉴权失败：调用方秘钥无效或未传递");
            throw new RpcException("RPC 鉴权失败：秘钥无效或未传递");
        }
        return invoker.invoke(invocation);
    }
}
