# Mystikos Server 设计文档

Mystikos 是一个陪玩公会平台：老板（付费用户）在站内预约陪玩服务、参与排行榜与等级体系、和陪玩建立亲密度关系、赠送礼物，并可购买公会周边商品。后端服务于 Nuxt 4 + Vue3 + TypeScript 前端（C 端）。

## 文档索引

- [技术选型](architecture/tech-stack.md) — 后端/前端技术栈与选型理由
- [领域模型](architecture/domain-model.md) — DDD 限界上下文划分、聚合设计、领域事件
- [模块结构](architecture/module-structure.md) — Maven 多模块包结构、跨模块通信规则、数据库拆分约定
- [统一响应与全局异常处理](architecture/exception-handling.md) — APIResponse 封装、错误码分段约定、各模块通过枚举扩展错误码的方式
- [PRD 对照与模块取舍](architecture/prd-alignment.md) — PRD 9 个服务边界 ↔ 我们 12 个限界上下文的映射、确认无冗余的运营后台模块、`mystikos-review` 的降级建议、真实缺口清单

## 背景资料

- `F:\javaproj\Mystikos\prd\Mystikos_Backend_PRD_v1.0.docx` — 后端 PRD 草案（2026-08-21，待架构/合规评审），定义 9 个服务边界、核心表结构建议、非功能需求、里程碑排期。与本项目模块划分的对照见 [PRD 对照与模块取舍](architecture/prd-alignment.md)。
- `../technical-research/github-research/deep-research-report.md` — 早期技术预研报告（Discord 陪玩点单 + 海外商品营销场景，容量模型、支付/预约安全门、许可证与开源框架对比）。本项目最终未采用其中的开源商城框架方案，改为基于 Spring Boot 3.x 自建 DDD 单体，但报告中的容量假设、支付/预约安全门（P0 上线门）、Discord 集成约束仍然适用，实现时应参考。
- `../ui/index.html` — 静态 UI 原型（首页推荐/排行榜/等级体系/亲密度/礼物墙/商城预览 + 商城页），是领域模型盘点的直接依据。

## 当前状态

- 技术栈已确定：JDK 17、Spring Boot 3.x、MyBatis-Plus、Spring Security OAuth2、PostgreSQL；前端 Nuxt 4 + Vue3 + TS。
- 领域模型与包结构已完成设计（见上方文档）。
- **Maven 多模块脚手架已搭建完成，20 个模块全部编译通过**（`mvn compile`，JDK 17）：
  - `mystikos-common` / `mystikos-common-web`：统一响应封装、全局异常处理
  - `mystikos-common-event`：领域事件基类 + 发布端口（Spring ApplicationEventPublisher 实现）
  - `mystikos-common-security`：当前用户上下文工具类 + **自签发 JWT 的签发/校验/过滤器**（`JwtTokenService`/`JwtAuthenticationFilter`/`SecurityConfig`）。鉴权模型已确定：本地账号和第三方登录成功后都由我们自己签发 JWT，不接外部 OAuth2 Authorization Server 的 issuer-uri/JWKS。
  - `mystikos-common-cache`：**Redis 接入**，`RedisConfig` 提供 key=String/value=JSON 序列化的 `RedisTemplate`；新增 `RateLimiter`（固定窗口限流，INCR+EXPIRE）供业务模块直接注入用。
  - `mystikos-common-storage`：**MinIO 对象存储接入**，`ObjectStorageService` 端口 + `MinioObjectStorageService` 实现，配套通用接口 `/api/v1/files`（上传/预签名下载链接/删除，需登录）。
  - `mystikos-booking`：**五层 DDD 结构的完整打样实现**——聚合根状态机、仓储接口/实现、MyBatis-Plus 持久化、PostgreSQL 迁移脚本（含防重叠的 `EXCLUDE USING gist` 约束）、REST 端点（`/api/v1/bookings`）。已在本地 PostgreSQL 跑通（迁移版本 `V1`）。
  - `mystikos-identity`：**S1 账号与认证 + S2 用户资料（老板侧）已实现**——手机号/邮箱二选一注册、密码或验证码登录、自签发 JWT + 可吊销 Refresh Token、**Discord 第三方登录已接入**（`/api/v1/auth/oauth/discord/login`，其他 Provider 仍占位）；固定 6 角色枚举 `Role`（游客/会员用户/陪玩/客服/考核官/管理员）+ RBAC 权限表；会员等级挂点（`MembershipTier` 接口在 `mystikos-common`，梯度故意留空）；老板昵称/匿名上榜设置（`ProfileController`）。
  - `membership` / `provider-catalog` / `commerce` / `gifting` / `relationship` / `payment` / `leaderboard` / `review` / `trust-safety` / `notification`：Maven 模块骨架已建，内部五层结构留空待实现，实现时参照 `mystikos-booking` / `mystikos-identity`
  - `mystikos-app`：启动壳，聚合全部模块的单体可执行 jar，已接 Flyway（指向仓库根目录的 `deploy/sql`，见下）、Knife4j 接口文档（本地 `http://localhost:8080/doc.html`）、JWT 密钥占位配置
- **所有 SQL 迁移脚本集中在 `deploy/sql/`**（`V1`-`V6`），不再按模块拆在各自 `src/main/resources` 下；跨模块共用同一个 Flyway 版本序列，新增迁移前看一眼当前最大版本号接着编。`V4` 是内置超级管理员种子数据：`administrator@mystikos.local` / `Mystikos@123`（邮箱渠道登录，因为登录模型没有裸用户名概念），密码哈希是用项目实际依赖的 `spring-security-crypto` 生成并验证过的，不是编的字符串；上线前必须改密码。`V5` 把 `Role` 枚举镜像成 `identity_role` 表并预置 6 个角色，给 `identity_user_role`/`identity_role_permission` 补外键完整性——枚举仍是行为/展示名的权威来源，表只用于外键约束，新增角色要两边同步改。`V6` 删掉了 `identity_verification_code` 表——验证码改存 Redis 了，见下一条。
- **接口文档用 Knife4j**：运行时只在 `mystikos-app` 引入，业务模块只依赖 `swagger-annotations-jakarta` 注解类；持久化实体（PO）和请求 DTO 统一按"`@Schema` 描述 + 显式 `@TableField` + javadoc + `Serializable`/`@Serial serialVersionUID`"的风格写。已加**全局 Bearer 认证声明**（`OpenApiConfig`），Knife4j 界面右上角"Authorize"填一次 token 后所有调试请求自动带上；已按**限界上下文**分组（`SpringDocGroupConfig`，对齐 [领域模型](architecture/domain-model.md) 的 Bounded Context 划分，不是按 URL 前缀/PRD 功能编号）：`Identity & Access · 身份与访问`（合并了 S1 账号认证/S2 用户资料/运营态用户与角色管理三块 Controller）、`Booking · 预约撮合`、`通用能力 · 文件与对象存储`（不是业务上下文，单列）；新模块有真实接口后照此补分组。
- **所有 Controller 统一走 `/api/v1/*` 前缀**（PRD 要求，之前 Booking/Identity 都漏了，已补齐）。新增 `GET /api/v1/roles` 查角色列表（读枚举不查库）。
- `GlobalExceptionHandler` 加了 `NoResourceFoundException` 专门处理（比如浏览器打开 `/doc.html` 自动带的 `favicon.ico` 请求），返回正常 404，不再当系统异常打 ERROR 日志刷屏。
- **依赖服务（Postgres/Redis/MinIO）的本地 Docker 起法见 `deploy/docker/README.md`**（含镜像版本对照表和 `docker run` 命令；后续配置稳定后会合并成 `docker-compose.yaml`）。Redis 选了 `redis:7.2-alpine`（最后一个 BSD 协议版本，规避 7.4+ 的 RSAL/SSPL、8.0+ 的 AGPL 协议风险）；MinIO 选了官方停更前最后一版 `RELEASE.2025-09-07T16-13-09Z-cpuv1`（MinIO 2025 年 10 月起不再发布免费镜像，生产上线前需重新评估镜像来源）。
- **验证码存储从 Postgres 迁到了 Redis，登录/验证码接口也接上了限流**：`mystikos-identity` 的 `RedisVerificationCodeRepositoryImpl` 用 key 的 TTL 存验证码（不用清理任务，过期自动消失）；`AuthApplicationService` 用 `mystikos-common-cache` 新增的 `RateLimiter`（固定窗口限流）挡住撞库/轰炸——`/auth/verification-codes` 同一 channel+identifier 60 秒只能发一次（不分 purpose，避免绕过），`/auth/login` 同一 channel+identifier 15 分钟最多 10 次尝试，超限抛 `VERIFICATION_CODE_RATE_LIMITED`(2014)/`LOGIN_RATE_LIMITED`(2015)。
- 已验证端到端跑通：本地起了 Postgres（原生服务）+ Redis/MinIO（Docker 容器，见 `deploy/docker/`），`mystikos-app` 打包成可执行 jar 后实测登录、`/api/v1/auth/me`、文件上传/预签名下载/删除全部走通。**踩过一个坑**：`mystikos-app/pom.xml` 的 `spring-boot-maven-plugin` 之前没绑定 `repackage` 到 `package` 阶段（这个自动绑定只有用 `spring-boot-starter-parent` 做父 POM 才有，本项目父 POM 是自己的），导致 `mvn package` 产出的是不能 `java -jar` 直接跑的瘦 jar，已修复。
- **邮箱验证码 / 短信验证码 / Discord 登录三件事都接了骨架**（目标市场先欧洲、后美洲澳洲，不是国内用户为主，选型和文案都按这个前提定的）：
  - **邮件**：走通用 SMTP（`spring-boot-starter-mail`），不绑定具体厂商——`mystikos.mail.enabled=true` 时用 `SmtpEmailSender` 真实发信（配 `spring.mail.host/port/username/password`），默认 `false` 时用 `LoggingEmailSender` 只打日志，本地开发不用配任何账号就能跑。
  - **短信**：厂商还没定（候选 Twilio 这类全球覆盖的，不是国内阿里云/腾讯云那一挂），`SmsSender` 接口先立好，`UnconfiguredSmsSender` 打日志占位，选定厂商后加一个真实实现替换掉就行，`AuthApplicationService` 不用改。
  - **Discord 登录**：`OAuthProviderClient` 接口 + `DiscordOAuthClient` 真实实现（标准 authorization_code 流程，拿 code 换 token 再换用户信息）；`client-id` 没配置时这个 Bean 不注册，走"该第三方登录方式暂未开放"而不是拿空凭证瞎调 Discord API。首次登录本地找不到绑定会自动注册新用户（`User.registerWithOAuth`，默认给 MEMBER 角色），已绑定就直接登录。**需要你在 `application.yml`（或环境变量 `DISCORD_CLIENT_ID`/`DISCORD_CLIENT_SECRET`/`DISCORD_REDIRECT_URI`）里填真实凭证才能跑通**，redirect-uri 要和 Discord Developer Portal 应用配置、前端发起授权时用的 URI 三处完全一致。
  - `VerificationCodeSender` 现在按 channel 路由到 `EmailSender`/`SmsSender`（`ChannelRoutingVerificationCodeSender`），验证码/通知文案用的是英文（呼应目标市场）。
- 尚未做：短信服务商选型开账号、邮箱/Discord 真实凭证配置后的联调验证（代码已编译通过，但没有真实账号所以没法实测发信/走通 Discord 授权码交换）、微信等其他第三方登录、其余 10 个业务模块的领域代码、会员等级梯度的具体定义、实名认证与未成年人标记、上线前关闭 Knife4j、全球多语言模板（目前验证码/邮件文案是写死的英文，没有 i18n 框架）。
- 已对照后端 PRD 草案（见 [PRD 对照与模块取舍](architecture/prd-alignment.md)）：确认没有多建冗余的"运营后台"模块；`mystikos-review` 建议先保留骨架但降级排期；发现的真实缺口里 S1/S2 部分已补上，`mystikos-payment` 的钱包/提现聚合仍待做。
