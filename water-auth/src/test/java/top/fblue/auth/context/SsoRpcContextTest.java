package top.fblue.auth.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import top.fblue.auth.exception.SsoAuthException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** RPC 用户上下文测试。 */
class SsoRpcContextTest {

    /** 清理当前测试线程绑定的 RPC 用户上下文。 */
    @AfterEach
    void clearContext() {
        SsoRpcContext.clear();
    }

    /** 未绑定用户时可空读取应返回空，严格读取仍应抛出鉴权异常。 */
    @Test
    void shouldDistinguishNullableAndRequiredReads() {
        assertNull(SsoRpcContext.getNullable());
        assertThrows(SsoAuthException.class, SsoRpcContext::getCurrentUserInfo);
    }

    /** 绑定用户后应能读取当前 RPC 用户。 */
    @Test
    void shouldReadBoundRpcUser() {
        SsoRpcContext.bind(SsoPrincipal.builder().userId(9L).build());

        assertEquals(9L, SsoRpcContext.getNullable().getUserId());
        assertEquals(9L, SsoRpcContext.getCurrentUserId());
    }
}
