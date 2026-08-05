# Water 项目指令

本文件适用于仓库根目录及全部子目录。

## 工作流

- 处理本仓库的 Java 开发、诊断、重构、审查、构建或测试任务前，完整读取并遵循 `.agents/skills/water-java-projects/SKILL.md`。
- 跨仓库依赖、模块边界、Maven 验证顺序和通用交付要求以 `$water-java-projects` 为准；本文只补充 Water 特有约定。

## 仓库职责

Water 是共享基础组件仓库：

- `water-common`：通用类型、异常、响应和无框架工具。
- `water-redis`：可复用的 Redis 客户端与 DNS 解析自动配置。
- `water-log`：日志与链路上下文。
- `water-framework`：Web、MyBatis、序列化和通用框架配置。
- `water-dubbo`：Dubbo 配置、过滤器、鉴权和链路透传。
- `water-auth`：可复用的 SSO/JWT 与认证自动配置。

## 项目约定

- 不引入 Watermelon 或 Banana 的业务语义；`water-common` 不依赖 Spring、数据库或 RPC 实现。
- 新增自动配置时，同步维护配置属性、条件装配和 `AutoConfiguration.imports`。
- 修改 Web/Dubbo 上下文或过滤器时，保证异常路径也能正确清理上下文，并保持消费端与提供端语义一致。
- 不提交密钥、口令、令牌、Cookie、真实连接串、私有服务地址或个人信息；配置示例只使用明显占位符。

## Git 提交

- 生成 Git commit message 时必须使用中文说明，不得使用纯英文；如采用 Conventional Commits，可保留 `feat:`、`fix:` 等类型前缀，但正文必须为中文。
