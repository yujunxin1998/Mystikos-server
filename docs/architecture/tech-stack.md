# 技术选型

## 后端

| 项 | 选择 | 备注 |
|---|---|---|
| JDK | 17 | LTS |
| 应用框架 | Spring Boot 3.x | 单体起步，不引入 Spring Cloud 全家桶 |
| ORM | MyBatis-Plus | SQL 可控性优先于全自动化 ORM |
| 认证授权 | Spring Security + 自签发 JWT | 本地账号密码/验证码登录、第三方登录成功后都由我们自己签发 JWT；不接外部 OAuth2 Authorization Server 的 issuer-uri/JWKS。已实现，见 `mystikos-common-security` |
| 邮件发信 | 通用 SMTP（`spring-boot-starter-mail`） | 不绑定具体厂商——host/port/账号密码换成任意 SMTP 中继（阿里云邮件推送/SendGrid/Mailgun/自建）都能用。`mystikos-identity` 的 `SmtpEmailSender`（`mystikos.mail.enabled=true` 时生效）/`LoggingEmailSender`（默认，本地只打日志）二选一，`@ConditionalOnProperty` 互斥 |
| 短信发信 | 待选型 | 目标市场先欧洲后美洲/澳洲，候选是 Twilio 这类全球号码覆盖的厂商，不是国内阿里云/腾讯云短信那一挂（没有通用协议，必须接具体 SDK）。`SmsSender` 接口已定义，`UnconfiguredSmsSender` 打日志占位，选型后加真实实现替换 |
| 第三方登录 | Discord OAuth2（已接入）+ 可扩展 Provider 机制 | `OAuthProviderClient` 接口，`providerCode()` 对应 `/api/v1/auth/oauth/{provider}/login` 路径段，`AuthApplicationService` 收集所有实现 Bean 按 code 路由，新增 Provider 不用改用例代码。`DiscordOAuthClient` 走标准 authorization_code 流程（`RestClient`，Spring Framework 6.1+ 自带不用加依赖），`client-id` 未配置时 Bean 不注册（`@ConditionalOnExpression` 判空，不是 `@ConditionalOnProperty`——空字符串会被后者当成"已配置"，踩过这个坑）。首次登录本地无绑定记录会经 `User.registerWithOAuth` 自动注册，默认角色 MEMBER |
| 数据库 | PostgreSQL | 事务强一致 + `tstzrange`/`EXCLUDE` 约束（预约时段防重）+ JSONB（规则配置） |
| 缓存 | Redis（`spring-boot-starter-data-redis`，Lettuce 客户端） | `mystikos-common-cache` 提供序列化配置（key 用 String，value 用 Jackson JSON）+ 一个通用 `RateLimiter`（固定窗口限流，INCR+EXPIRE 实现）。已接入的业务用法：`mystikos-identity` 的验证码存储（`RedisVerificationCodeRepositoryImpl`，靠 key TTL 过期，替代了原来的 Postgres 表）+ 登录/验证码接口限流（`AuthApplicationService` 注入 `RateLimiter`，防撞库/防轰炸）。本地 Docker 镜像选了 `redis:7.2-alpine`（最后一个 BSD 协议版本，规避 7.4+ RSAL/SSPL、8.0+ AGPL 的开源义务风险），见 `deploy/docker/README.md` |
| 对象存储 | MinIO（S3 兼容，`io.minio:minio` SDK） | `mystikos-common-storage` 定义 `ObjectStorageService` 接口（Port）+ `MinioObjectStorageService` 实现，换成阿里云 OSS/腾讯云 COS/AWS S3 时只加新实现类切 Bean，不改业务代码。提供了通用的 `/api/v1/files` 上传/预签名下载链接/删除接口。**启动时会主动连接 MinIO 检查/创建 bucket，MinIO 没起会导致应用启动失败**（和 Postgres/Flyway 一样是硬依赖，不是懒加载）。已知坑：**`io.minio:minio` 9.0.3 把 `Method` 枚举从 `io.minio.http.Method` 挪到了 `io.minio.Http.Method`**（嵌套类），旧版教程/示例代码里的 import 路径在这个版本会编译不过。本地 Docker 起法（含镜像版本对照表、MinIO 官方 2025-10 起停更免费镜像的说明）见 `deploy/docker/README.md` |
| 接口文档 | Knife4j（springdoc-openapi 封装） | 只在 `mystikos-app` 引入实际运行时；业务模块只依赖 `swagger-annotations-jakarta` 注解类。本地 `http://localhost:8080/doc.html`，上线前必须关掉。已知坑：（1）**artifactId 必须是 `knife4j-openapi3-jakarta-spring-boot-starter`**，不带 `jakarta` 的 `knife4j-openapi3-spring-boot-starter` 是 Spring Boot 2.x/javax 命名空间的老版本，两个 groupId 一样、名字只差一个词，选错启动时炸 `NoClassDefFoundError: javax/servlet/Filter`；（2）**认证方案要用 `apiKey` 类型，不要用 `HTTP bearer` 类型**——Knife4j 4.3.0 调试面板对 HTTP bearer 类型映射不可靠，会把安全方案的 name 当成请求头字段名发出去而不是标准的 `Authorization: Bearer <token>`，导致鉴权过滤器收不到 token（已踩过，见 `OpenApiConfig`）；apiKey 类型要在 Authorize 弹窗里手动填完整的 `Bearer <token>`（含前缀），不会自动加 |
| 架构风格 | DDD 限界上下文 + 模块化单体 | 详见 [领域模型](domain-model.md) 与 [模块结构](module-structure.md) |

### 为什么不用现成开源商城/管理框架（RuoYi-Cloud-Plus / mall4cloud / mall-swarm 等）

早期技术预研（`../../technical-research/github-research/deep-research-report.md`）对比过若干开源底座。最终决定不采用，原因：

1. 交易域（预约撮合、礼物解锁、亲密度、会员成长）是本项目的核心差异化逻辑，任何通用商城框架都不覆盖，最终都要自建，复用收益有限。
2. 团队希望完全掌控代码结构以支撑 DDD 建模和后续微服务拆分，现成脚手架的表结构/代码生成套路会和自定义领域模型产生耦合冲突。
3. 避免引入用不到的模块（工作流引擎、代码生成器等）带来的依赖重量和维护面。

预研报告中**与框架选型无关**的部分仍然有效，实现时应参考：
- 容量模型与压测目标（20k 注册用户量级的 RPS/SLO 假设）
- 支付、预约、后台权限的 P0 上线门（幂等、防重放、分权、双人批准等）
- Discord OAuth2/Interactions/Rate Limit 集成约束
- PostgreSQL PITR、Kubernetes probes、HPA 等运维基线

## 前端

| 项 | 选择 | 备注 |
|---|---|---|
| C 端 Web | Nuxt 4 + Vue 3 + TypeScript | 已确定；SSR/SEO 适合未来海外商品营销场景 |
| 后台管理 | 待定 | 建议 Vue3 技术栈以复用团队认知，具体方案未定 |
| App | 待定 | MVP 阶段可先用响应式 Web/PWA |

## 演进原则

- MVP 阶段：单一 Spring Boot 可执行 jar（`mystikos-app`），内部按 DDD 限界上下文划分 Maven 模块。
- 触发拆分微服务的条件（沿用预研报告 Gate B 的思路，非硬指标，需团队按实际情况判断）：后端团队规模、需要独立发布的服务数、峰值 RPS、是否有硬隔离 SLA 要求。
- 包结构从第一天就为拆分做准备：跨模块调用只经接口（Port），不共享数据库表，领域事件总线可替换底层实现。详见 [模块结构](module-structure.md)。
