package top.fblue.dubbo.context;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RPC 秘钥鉴权配置
 * 用于 Dubbo 服务端校验调用方传递的秘钥。
 * 启动后通过 getInstance() 可获取本单例，供 SPI 加载的 Filter 等非 Spring 组件读取配置。
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "rpc")
public class DubboSecretConfig {

    private static volatile DubboSecretConfig instance;
    /**
     * 服务名是 dubbo.application.name
     * 各服务名 -> 秘钥。配置示例：rpc.secret.banana=123, rpc.secret.watermelon=333
     * 新增服务只需在配置文件增加 rpc.secret.<服务名>=<秘钥>，无需改代码
     */
    private Map<String, String> secret = new HashMap<>();
    /**
     * 是否启用 RPC 秘钥鉴权
     */
    private boolean authEnabled = true;
    /**
     * 当前应用的 dubbo.application.name（从配置注入）
     */
    @Value("${dubbo.application.name}")
    private String applicationName;

    @PostConstruct
    void registerInstance() {
        instance = this;
    }

    /** 供 SPI 加载的 Filter 等获取 Spring 管理的配置单例 */
    public static DubboSecretConfig getInstance() {
        return instance;
    }

    /**
     * 获取当前应用的秘钥
     */
    public static String getApplicationSecret() {
        return instance.getSecret().get(instance.getApplicationName());
    }
}
