package top.fblue.auth.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.fblue.auth.exception.SsoAuthException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** 统一 SSO 用户上下文测试。 */
class SsoUserContextTest {

    /** 清理测试线程中的全部用户上下文。 */
    @AfterEach
    void clearContexts() {
        SsoHttpContext.clear();
        SsoRpcContext.clear();
        SsoUserContext.clearScheduledTaskUser();
        RequestContextHolder.resetRequestAttributes();
    }

    /** HTTP 请求应严格读取 HTTP 用户并优先于 RPC 用户。 */
    @Test
    void shouldPreferHttpUser() {
        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(new MockHttpServletRequest()));
        SsoHttpContext.bind(SsoPrincipal.builder().userId(9L).build());
        SsoRpcContext.bind(SsoPrincipal.builder().userId(8L).build());

        assertEquals(9L, SsoUserContext.getCurrentUserId());
    }

    /** HTTP 请求缺少认证用户时不应降级使用 RPC 用户。 */
    @Test
    void shouldRejectUnauthenticatedHttpRequest() {
        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(new MockHttpServletRequest()));
        SsoRpcContext.bind(SsoPrincipal.builder().userId(8L).build());

        assertThrows(SsoAuthException.class, SsoUserContext::getCurrentUserId);
    }

    /** 非 HTTP 的 RPC 调用应读取上游传递的用户。 */
    @Test
    void shouldReadRpcUser() {
        SsoRpcContext.bind(SsoPrincipal.builder().userId(8L).build());

        assertEquals(8L, SsoUserContext.getCurrentUserId());
    }

    /** 定时任务应绑定指定用户，并在异常结束后清理用户上下文。 */
    @Test
    void shouldClearScheduledTaskUserAfterFailure() {
        assertThrows(IllegalStateException.class, () ->
                SsoUserContext.runAsScheduledTaskUser(2L, () -> {
                    SsoPrincipal principal = SsoUserContext.getCurrentUserInfo();
                    assertEquals(2L, principal.getUserId());
                    assertEquals("scheduled-task:2", principal.getSubject());
                    assertEquals("scheduled-task:2", principal.getSid());
                    assertNotNull(principal.getJti());
                    assertEquals("scheduler", principal.getClientId());
                    throw new IllegalStateException("测试异常");
                }));

        assertThrows(SsoAuthException.class, SsoUserContext::getCurrentUserId);
    }

    /** HTTP、RPC 和定时任务用户均不存在时应抛出鉴权异常。 */
    @Test
    void shouldRejectMissingUserContext() {
        assertThrows(SsoAuthException.class, SsoUserContext::getCurrentUserId);
    }
}
