package top.fblue.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记无需终端用户上下文即可调用的 RPC 方法。
 *
 * <p>该注解只放行用户身份校验，Dubbo 共享密钥等基础设施认证仍然生效。</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RpcPublic {
}
