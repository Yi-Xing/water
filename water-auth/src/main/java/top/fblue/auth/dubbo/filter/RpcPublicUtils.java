package top.fblue.auth.dubbo.filter;

import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import top.fblue.common.annotation.RpcPublic;

import java.lang.reflect.Method;

/**
 * 判断 Dubbo 调用的目标方法是否声明为无需用户上下文的公共 RPC 方法。
 */
public final class RpcPublicUtils {

    /** 工具类不允许实例化。 */
    private RpcPublicUtils() {
    }

    /**
     * 判断当前 Dubbo 接口方法是否标注了 {@link RpcPublic}。
     *
     * @param invoker    Dubbo 服务调用器
     * @param invocation 当前 RPC 调用信息
     * @return 标注 {@link RpcPublic} 时返回 {@code true}，方法无法解析时返回 {@code false}
     */
     static boolean isPublic(Invoker<?> invoker, Invocation invocation) {
        try {
            Method method = invoker.getInterface().getMethod(
                    invocation.getMethodName(), invocation.getParameterTypes());
            return method.isAnnotationPresent(RpcPublic.class);
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
