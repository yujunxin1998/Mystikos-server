# PRD 对照与模块取舍

来源：`F:\javaproj\Mystikos\prd\Mystikos_Backend_PRD_v1.0.docx`（草案，2026-08-21，待架构/合规评审）。

本文档只做一件事：把 PRD 定义的 9 个服务边界（S1-S9）对到我们已经在建的 12 个限界上下文，找出哪些模块 PRD 里不必要、哪些和别的模块重复，以及对照出的真实缺口。**不改变已确定的技术栈和 DDD/模块化单体架构**——PRD"技术选型建议"一节明确写"技术栈本身无强制约束……供架构评审时参考"，是非绑定建议，不是变更指令。

## 1. PRD 服务 ↔ 限界上下文映射

| PRD 服务 | 职责概要 | 对应限界上下文 | 备注 |
|---|---|---|---|
| S1 账号与认证 | 注册登录、实名认证、Token 鉴权、角色区分 | `mystikos-identity` | **已实现**：手机号/邮箱二选一注册、密码或验证码登录、自签发 JWT + 可吊销 Refresh Token、第三方登录契约占位。实名认证/未成年人标记还没建模，见第 3 节 |
| S2 用户资料 | 老板/陪玩资料、陪玩认证审核、隐私设置 | 老板侧**已实现**（`mystikos-identity` 的 `ProfileController`：昵称、匿名上榜） + 陪玩侧待建（`mystikos-provider-catalog`） | 陪玩认证审核也牵涉 `mystikos-trust-safety` |
| S3 排行榜与等级 | 陪玩榜/老板榜、贵宾等级升降级 | `mystikos-leaderboard` + `mystikos-membership` | **已实现**。PRD 当成一个服务，我们拆成两个限界上下文——**不是重复**，是更细的 DDD 边界，两者仍在同一个部署单元（`mystikos-app`）里，粒度不同不影响一起发布。排行榜实时计算（不是 PRD 暗示的"每周一更新"冻结快照）；会员等级梯度是占位值 |
| S4 亲密度与礼物成就 | 互动/赠礼记录、亲密度计算、成就解锁 | `mystikos-relationship` + `mystikos-gifting` | **已实现**。同 S3，PRD 一个服务对应我们两个上下文，理由同上。成就解锁只做了 `CUMULATIVE_COUNT`/`CUMULATIVE_SPEND` 两种规则的自动评估，`LEADERBOARD_RANK`/`INTIMACY_STAGE`/`CONSECUTIVE_DAYS` 会形成循环模块依赖，暂不评估，见 [领域模型](domain-model.md) |
| S5 商城与订单 | 商品、陪玩推荐关联、购物车、订单、库存 | `mystikos-commerce` | **已实现，但推荐关联工作流本轮未做**；`companion_product_endorsement`（陪玩需确认授权才能被关联推荐）是我们域模型目前没写清楚的细节，见第 3 节 |
| S6 支付与钱包 | 微信/支付宝下单回调、平台钱包、赠礼扣款、陪玩提现 | `mystikos-payment` | PRD 的"钱包/提现"比我们目前 `PaymentIntent + LedgerEntry` 的设计更宽，见第 3 节 |
| S7 内容审核与风控 | 内容审核、异常消费/刷榜检测、黑白名单 | `mystikos-trust-safety` | 吻合；刷榜检测要能给 `mystikos-leaderboard` 降权信号，是跨上下文事件，不是新模块 |
| S8 消息通知 | 站内信、短信、Push | `mystikos-notification` | 吻合 |
| S9 运营管理后台 | 陪玩审核、商品上下架、榜单干预、风控工单、数据看板 | **没有对应的独立限界上下文，也不该有** | 见第 2 节 |

`mystikos-review`（评价）在 PRD 的"适用范围"清单（账号认证、用户资料、排行榜与等级、亲密度与礼物、商城与订单、支付与钱包、审核与风控、通知、运营后台）里**完全没有出现**，见第 4 节。

## 2. 确认：不需要单独的"运营后台"限界上下文

PRD 原文（4.9 节）：

> 建议复用上述各服务已有的管理态接口，通过 RBAC（基于角色的访问控制）区分运营权限，**而非另起一套重复的数据模型**。

我们当前的 12 个限界上下文列表里**本来就没有**单独的 admin/operations 上下文——运营能力（陪玩审核走 `trust-safety`、商品上下架走 `commerce`、榜单干预走 `leaderboard`、封禁走 `identity`）分散在各自的上下文里，用 `mystikos-identity` 的 `Role`（CUSTOMER_SERVICE、ASSESSOR、ADMIN）做权限区分。这正好是 PRD 建议的做法，不用改。

数据看板 PRD 建议直接接只读副本 + 现成 BI 工具（如 Metabase），不用我们自己写报表服务——也不是一个新的限界上下文，是运维侧的事，不进领域模型。

## 3. 真实缺口（不是"不必要"，是目前域模型漏掉的）

这些不需要新建限界上下文，是在已有上下文里补字段/补聚合：

1. ~~**老板资料与隐私设置**~~：已解决。`nickname`/`privacy_anonymous`/`gender`/`avatarUrl`/`birthDate`/`bio`/`regionCode`/`tagIds` 都已在 `mystikos-identity` 的 `User` 聚合里实现，见 [领域模型](domain-model.md)。`regionCode` 引用新增的 `mystikos-common-region` 模块（国家+一级行政区参考数据，种子数据覆盖欧洲）；`tagIds` 引用新增的标签目录（`TagDefinition`，后台配置，`category` 目前只有 `GAME_TYPE` 一种）。
2. **实名认证 / 未成年人标记**：PRD 8 章明确要求 `is_minor` 标记同步给支付网关做限额拦截。归属：`mystikos-identity` 新增校验记录（对应 PRD 的 `user_realname_verification`），`mystikos-payment` 消费这个标记做充值/赠礼限额——这是我们 P0 上线门已经提到但没有域模型支撑的部分。
3. **钱包与提现**：`mystikos-payment` 目前的字段级设计只有 `PaymentIntent` + `LedgerEntry`（面向单笔交易记账），没有"陪玩收益钱包余额 + 提现申请"这个聚合。PRD 的 `wallets`/`withdraw_requests` 提示我们要补 `Wallet`(userId, balance) 和 `WithdrawRequest` 到 Payment 的聚合列表。
4. **商品-陪玩推荐关系需要陪玩授权确认**：`mystikos-commerce` 的 `Product.recommendedBy` 目前只是个字段，PRD 强调这是需要陪玩确认授权的关系，不是运营单方面写的标签——建模时要加一个"待确认/已确认"状态，防止未经同意就把陪玩关联到商品上。

## 4. 建议重新评估：`mystikos-review`

PRD 的适用范围清单里没有"评价/评论"这个服务；S2 的 `companion_profiles.rating` 只是一个字段，PRD 没有描述评分从哪来、要不要审核、要不要展示评论内容。这和我们之前从 UI 原型（★4.9 星级）反推出来的 `Review` 限界上下文对不上——原型上有星级展示，但 PRD 没把"怎么产生这个星级"列入当前后端建设范围。

**建议**：`mystikos-review` 模块骨架先保留（删掉一个已建的 Maven 模块收益也不大，成本上不值得只为了"更干净"去删），但在里程碑排期上明确降级——不在第一/二阶段实现，等 PRD 后续版本或产品侧明确评价功能的规则（谁能评、审核流程、要不要允许陪玩回复）再排期，不要在规则不明时先把表结构和状态机定下来。

## 5. 非阻塞的表述差异（已确认维持现状，不用改）

- **技术栈**：PRD 建议 Node.js + NestJS，是"供架构评审时参考"的非绑定建议；我们已经明确决定 JDK17 + Spring Boot + MyBatis-Plus + PostgreSQL（见 [技术选型](tech-stack.md)），不切换。
- **错误码分段**：PRD 举例"如 1xxx 账号相关、2xxx 支付相关"是示意性写法，不是强制表格。我们已经实现并跑通了自己的分段方案（Identity 2000-2999、Payment 9000-9999，见 [统一响应与全局异常处理](exception-handling.md)），维持不变，重新编号纯粹是无意义的返工。

## 6. 值得单独跟进、但这次不顺手改的

- ~~API 版本前缀~~：已解决。做 S1/S2 时顺手把 `BookingController`/`UserController` 也统一加上了 `/api/v1` 前缀，全部 Controller 现在统一走 `/api/v1/*`。
- **限流**：PRD 要求基于 Redis 滑动窗口对登录/支付/赠礼做限流，我们目前完全没有 Redis 依赖，`/api/v1/auth/login`、`/api/v1/auth/verification-codes` 这两个接口现在没有任何频率限制，验证码可以被无限次请求发送、登录可以被无限次撞库——这是留到下一步要处理的真实风险，不是可以一直拖的小事。
