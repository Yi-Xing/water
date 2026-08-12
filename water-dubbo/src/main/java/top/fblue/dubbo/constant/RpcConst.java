package top.fblue.dubbo.constant;

/**
 * RPC 上下文与 URL 参数常量。
 */
public final class RpcConst {

    /**
     * 禁止实例化常量类。
     */
    private RpcConst() {
    }

    /**
     * rpc 调用鉴权 key
     */
    public static final String SECRET_KEY = "rpc-secret";

    /**
     * 动态直连 RPC 显式指定的目标应用名参数。
     */
    public static final String TARGET_APPLICATION_KEY = "rpc.target-application";

    /**
     * dubbo url 上，被调用方（Provider）的应用名的 key
     */
    public static final String REMOTE_APPLICATION_KEY = "remote.application";
}
