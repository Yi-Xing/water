package top.fblue.dubbo.context;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import top.fblue.dubbo.constant.RpcConst;
import top.fblue.dubbo.filter.DubboSecretAuthFilter;
import top.fblue.dubbo.filter.DubboSecretConsumerFilter;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Dubbo RPC 秘钥消费端与服务端过滤器测试。
 */
class DubboSecretFilterTest {

    /** RPC 秘钥配置。 */
    private DubboSecretConfig dubboSecretConfig;

    /**
     * 初始化测试秘钥配置和 RPC 上下文。
     */
    @BeforeEach
    void setUp() {
        clearRpcContext();
        dubboSecretConfig = new DubboSecretConfig();
        dubboSecretConfig.setApplicationName("watermelon");
        dubboSecretConfig.setSecret(Map.of(
                "watermelon", "watermelon-secret",
                "banana", "banana-secret"));
        dubboSecretConfig.registerInstance();
    }

    /**
     * 清理测试线程中的 RPC 上下文。
     */
    @AfterEach
    void tearDown() {
        clearRpcContext();
    }

    /**
     * 动态直连显式目标应用应优先于 Dubbo 自动推导的远端应用。
     */
    @Test
    void shouldPreferExplicitTargetApplicationForDirectReference() {
        DubboSecretConsumerFilter filter = new DubboSecretConsumerFilter();
        Invoker<Object> invoker = mock(Invoker.class);
        Invocation invocation = mock(Invocation.class);
        Result expectedResult = mock(Result.class);
        URL url = URL.valueOf("dubbo://banana.fblue.top:20881/example.Service"
                + "?remote.application=watermelon&rpc.target-application=banana");
        when(invoker.getUrl()).thenReturn(url);
        when(invoker.invoke(invocation)).thenAnswer(ignored -> {
            assertEquals("banana-secret",
                    RpcContext.getClientAttachment().getAttachment(RpcConst.SECRET_KEY));
            return expectedResult;
        });

        Result actualResult = filter.invoke(invoker, invocation);

        assertSame(expectedResult, actualResult);
        assertNull(RpcContext.getClientAttachment().getAttachment(RpcConst.SECRET_KEY));
    }

    /**
     * 普通 RPC 调用应继续使用 Dubbo 提供的远端应用名选择秘钥。
     */
    @Test
    void shouldUseRemoteApplicationForRegularReference() {
        DubboSecretConsumerFilter filter = new DubboSecretConsumerFilter();
        Invoker<Object> invoker = mock(Invoker.class);
        Invocation invocation = mock(Invocation.class);
        Result expectedResult = mock(Result.class);
        URL url = URL.valueOf("dubbo://watermelon.fblue.top:20880/example.Service"
                + "?remote.application=watermelon");
        when(invoker.getUrl()).thenReturn(url);
        when(invoker.invoke(invocation)).thenAnswer(ignored -> {
            assertEquals("watermelon-secret",
                    RpcContext.getClientAttachment().getAttachment(RpcConst.SECRET_KEY));
            return expectedResult;
        });

        Result actualResult = filter.invoke(invoker, invocation);

        assertSame(expectedResult, actualResult);
        assertNull(RpcContext.getClientAttachment().getAttachment(RpcConst.SECRET_KEY));
    }

    /**
     * 目标应用未配置秘钥时应在消费端失败，不能继续透传上一跳秘钥。
     */
    @Test
    void shouldRejectMissingTargetSecretWithoutInvokingProvider() {
        DubboSecretConsumerFilter filter = new DubboSecretConsumerFilter();
        Invoker<Object> invoker = mock(Invoker.class);
        Invocation invocation = mock(Invocation.class);
        URL url = URL.valueOf("dubbo://unknown.fblue.top:20882/example.Service"
                + "?remote.application=unknown");
        when(invoker.getUrl()).thenReturn(url);
        RpcContext.getClientAttachment().setAttachment(RpcConst.SECRET_KEY, "stale-secret");

        RpcException exception = assertThrows(RpcException.class, () -> filter.invoke(invoker, invocation));

        assertEquals("RPC 鉴权失败：未配置目标应用 unknown 的秘钥", exception.getMessage());
        assertNull(RpcContext.getClientAttachment().getAttachment(RpcConst.SECRET_KEY));
        verify(invoker, never()).invoke(any());
    }

    /**
     * 服务端完成鉴权后应在业务逻辑执行前清除秘钥，避免嵌套调用透传。
     */
    @Test
    void shouldRemoveServerSecretBeforeInvokingBusinessLogic() {
        DubboSecretAuthFilter filter = new DubboSecretAuthFilter();
        Invoker<Object> invoker = mock(Invoker.class);
        Invocation invocation = mock(Invocation.class);
        Result expectedResult = mock(Result.class);
        RpcContext.getServerAttachment().setAttachment(RpcConst.SECRET_KEY, "watermelon-secret");
        when(invoker.invoke(invocation)).thenAnswer(ignored -> {
            assertNull(RpcContext.getServerAttachment().getAttachment(RpcConst.SECRET_KEY));
            return expectedResult;
        });

        Result actualResult = filter.invoke(invoker, invocation);

        assertSame(expectedResult, actualResult);
        assertNull(RpcContext.getServerAttachment().getAttachment(RpcConst.SECRET_KEY));
    }

    /**
     * 清除消费端和服务端 RPC 附件。
     */
    private void clearRpcContext() {
        RpcContext.getClientAttachment().clearAttachments();
        RpcContext.getServerAttachment().clearAttachments();
    }
}
